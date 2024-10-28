package com.axelor.apps.businessproject.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentFinancialDiscountService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.supplychain.service.InvoicePaymentToolServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoicePaymentToolServiceBusinessProjectImpl
    extends InvoicePaymentToolServiceSupplychainImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public InvoicePaymentToolServiceBusinessProjectImpl(
      InvoiceRepository invoiceRepo,
      MoveToolService moveToolService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermPaymentService invoiceTermPaymentService,
      CurrencyService currencyService,
      PartnerSupplychainService partnerSupplychainService,
      SaleOrderComputeService saleOrderComputeService,
      PurchaseOrderService purchaseOrderService,
      AppAccountService appAccountService,
      InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermFilterService invoiceTermFilterService,
      InvoiceTermToolService invoiceTermToolService) {
    super(
        invoiceRepo,
        moveToolService,
        invoicePaymentRepo,
        invoiceTermPaymentService,
        currencyService,
        partnerSupplychainService,
        saleOrderComputeService,
        purchaseOrderService,
        appAccountService,
        invoicePaymentFinancialDiscountService,
        currencyScaleService,
        invoiceTermFilterService,
        invoiceTermToolService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateAmountPaid(Invoice invoice) throws AxelorException {

    invoice.setAmountPaid(computeAmountPaid(invoice));
    invoice.setAmountRemaining(
        invoice.getInTaxTotal().subtract(invoice.getAmountPaid()).add(invoice.getHoldBacksTotal()));
    invoice.setCompanyInTaxTotalRemaining(
        computeCompanyAmountRemaining(invoice).add(invoice.getCompanyHoldBacksTotal()));
    updateHasPendingPayments(invoice);
    updatePaymentProgress(invoice);
    invoiceRepo.save(invoice);
    log.debug("Invoice : {}, amount paid : {}", invoice.getInvoiceId(), invoice.getAmountPaid());
    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return;
    }
    SaleOrder saleOrder = invoice.getSaleOrder();
    PurchaseOrder purchaseOrder = invoice.getPurchaseOrder();
    if (saleOrder != null) {
      // compute sale order totals
      saleOrderComputeService._computeSaleOrder(saleOrder);
    }
    if (purchaseOrder != null) {
      purchaseOrderService._computePurchaseOrder(purchaseOrder);
    }
    if (invoice.getPartner().getHasBlockedAccount()
        && !invoice.getPartner().getHasManuallyBlockedAccount()) {
      partnerSupplychainService.updateBlockedAccount(invoice.getPartner());
    }
  }

  @Override
  protected boolean checkPendingPayments(Invoice invoice) {
    BigDecimal pendingAmount = BigDecimal.ZERO;

    if (invoice.getInvoicePaymentList() != null) {
      for (InvoicePayment invoicePayment : invoice.getInvoicePaymentList()) {
        if (invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_PENDING) {
          pendingAmount = pendingAmount.add(invoicePayment.getAmount());
        }
      }
    }

    pendingAmount = pendingAmount.add(invoice.getHoldBacksTotal());
    BigDecimal remainingAmount =
        invoice.getFinancialDiscount() != null
            ? invoice.getRemainingAmountAfterFinDiscount()
            : invoice.getAmountRemaining();
    return remainingAmount.compareTo(pendingAmount) <= 0;
  }

  @Override
  public Pair<List<Long>, Boolean> changeAmount(InvoicePayment invoicePayment, Long invoiceId)
      throws AxelorException {
    if (invoicePayment.getCurrency() == null) {
      return null;
    }

    List<Long> invoiceTermIdList = null;
    boolean amountError = false;

    if (invoiceId > 0) {
      List<InvoiceTerm> invoiceTerms =
          invoiceTermFilterService.getUnpaidInvoiceTermsFiltered(invoicePayment.getInvoice());

      if (invoicePayment.getManualChange()) {
        invoiceTerms.stream()
            .filter(it -> it.getFinancialDiscount() != null)
            .forEach(
                it -> it.setApplyFinancialDiscount(invoicePayment.getApplyFinancialDiscount()));
      }

      BigDecimal payableCurrencyAmount =
          this.getPayableAmount(
              invoiceTerms,
              invoicePayment.getPaymentDate(),
              invoicePayment.getManualChange(),
              invoicePayment.getCurrency());
      payableCurrencyAmount =
          payableCurrencyAmount.subtract(invoicePayment.getInvoice().getHoldBacksTotal().abs());

      if (!invoicePayment.getManualChange()
          || invoicePayment.getAmount().compareTo(payableCurrencyAmount) > 0) {
        invoicePayment.setAmount(payableCurrencyAmount);
        amountError = !invoicePayment.getApplyFinancialDiscount();
      }

      invoiceTermIdList =
          invoiceTerms.stream().map(InvoiceTerm::getId).collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(invoiceTerms)) {

        BigDecimal companyAmount =
            this.computeCompanyAmount(invoicePayment.getAmount(), invoicePayment);

        invoicePayment.clearInvoiceTermPaymentList();
        invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
            invoicePayment, invoiceTerms, companyAmount, companyAmount);

        if (invoicePayment.getApplyFinancialDiscount() && invoicePayment.getManualChange()) {
          BigDecimal financialDiscountAmount =
              invoicePayment.getInvoiceTermPaymentList().stream()
                  .map(InvoiceTermPayment::getInvoiceTerm)
                  .map(InvoiceTerm::getFinancialDiscountAmount)
                  .reduce(BigDecimal::add)
                  .orElse(BigDecimal.ZERO);
          BigDecimal amountWithFinancialDiscount = companyAmount.add(financialDiscountAmount);

          invoicePayment.clearInvoiceTermPaymentList();
          invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
              invoicePayment,
              invoiceTerms,
              amountWithFinancialDiscount,
              amountWithFinancialDiscount);
        } else if (this.isPartialPayment(invoicePayment)) {
          invoicePayment.clearInvoiceTermPaymentList();
          invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
              invoicePayment, invoiceTerms, companyAmount, companyAmount);
        }

        if (this.isPartialPayment(invoicePayment)) {
          invoicePayment.setApplyFinancialDiscount(false);
        }
        invoicePaymentFinancialDiscountService.computeFinancialDiscount(invoicePayment);
      }
    }
    return Pair.of(invoiceTermIdList, !invoicePayment.getManualChange() || !amountError);
  }

  @Override
  public void updatePaymentProgress(Invoice invoice) {

    invoice.setPaymentProgress(
        invoice
            .getAmountPaid()
            .multiply(new BigDecimal(100))
            .divide(
                invoice.getInTaxTotal().subtract(invoice.getHoldBacksTotal().abs()),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP)
            .intValue());
  }
}
