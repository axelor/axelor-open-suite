package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PaymentSessionBankOrderServiceImpl implements PaymentSessionBankOrderService {

  protected BankOrderService bankOrderService;
  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderLineService bankOrderLineService;
  protected BankOrderLineOriginService bankOrderLineOriginService;
  protected BankOrderRepository bankOrderRepo;
  protected CurrencyService currencyService;
  protected AppAccountService appAccountService;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected DateService dateService;
  protected BankOrderLineRepository bankOrderLineRepo;

  @Inject
  public PaymentSessionBankOrderServiceImpl(
      BankOrderService bankOrderService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderLineService bankOrderLineService,
      BankOrderLineOriginService bankOrderLineOriginService,
      BankOrderRepository bankOrderRepo,
      CurrencyService currencyService,
      AppAccountService appAccountService,
      InvoicePaymentRepository invoicePaymentRepo,
      DateService dateService,
      BankOrderLineRepository bankOrderLineRepo) {
    this.bankOrderService = bankOrderService;
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderLineService = bankOrderLineService;
    this.bankOrderLineOriginService = bankOrderLineOriginService;
    this.bankOrderRepo = bankOrderRepo;
    this.currencyService = currencyService;
    this.appAccountService = appAccountService;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.dateService = dateService;
    this.bankOrderLineRepo = bankOrderLineRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BankOrder generateBankOrderFromPaymentSession(PaymentSession paymentSession)
      throws AxelorException {
    BankOrder bankOrder = this.createBankOrder(paymentSession);

    paymentSession.setBankOrder(bankOrder);
    bankOrderService.generateSequence(bankOrder);

    return bankOrder;
  }

  protected BankOrder createBankOrder(PaymentSession paymentSession) throws AxelorException {
    BankOrder bankOrder =
        bankOrderCreateService.createBankOrder(
            paymentSession.getPaymentMode(),
            paymentSession.getPartnerTypeSelect(),
            paymentSession.getPaymentDate(),
            paymentSession.getCompany(),
            paymentSession.getBankDetails(),
            paymentSession.getCurrency(),
            paymentSession.getSequence(),
            this.getLabel(paymentSession),
            BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            BankOrderRepository.FUNCTIONAL_ORIGIN_PAYMENT_SESSION,
            paymentSession.getAccountingTriggerSelect());

    if (!paymentSession.getCurrency().equals(paymentSession.getCompany().getCurrency())) {
      bankOrder.setIsMultiCurrency(true);
    }

    return bankOrder;
  }

  protected boolean isFileFormatMultiDate(PaymentSession paymentSession) {
    return Optional.of(paymentSession)
        .map(PaymentSession::getPaymentMode)
        .map(PaymentMode::getBankOrderFileFormat)
        .map(BankOrderFileFormat::getIsMultiDate)
        .orElse(false);
  }

  @Override
  public void createOrUpdateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrder bankOrder,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {
    BankOrderLine bankOrderLine = null;

    if (paymentSession.getPaymentMode().getConsoBankOrderLinePerPartner()) {
      bankOrderLine =
          bankOrder.getBankOrderLineList().stream()
              .filter(
                  it ->
                      (it.getBankOrderDate() == null
                              || it.getBankOrderDate().equals(invoiceTerm.getDueDate()))
                          && it.getPartner().equals(invoiceTerm.getMoveLine().getPartner())
                          && ((it.getReceiverBankDetails() == null
                                  && invoiceTerm.getBankDetails() == null)
                              || (it.getReceiverBankDetails() != null
                                  && it.getReceiverBankDetails()
                                      .equals(invoiceTerm.getBankDetails()))))
              .findFirst()
              .orElse(null);
    }

    BigDecimal reconciledAmount = BigDecimal.ZERO;
    if (!CollectionUtils.isEmpty(invoiceTermLinkWithRefundList)) {
      List<Pair<InvoiceTerm, BigDecimal>> invoiceTermByAmountList =
          invoiceTermLinkWithRefundList.stream()
              .filter(pair -> pair.getLeft().equals(invoiceTerm))
              .map(pair -> pair.getRight())
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(invoiceTermByAmountList)) {
        for (Pair<InvoiceTerm, BigDecimal> pair : invoiceTermByAmountList) {
          reconciledAmount = reconciledAmount.add(pair.getRight());
        }
      }
    }

    if (bankOrderLine == null) {
      this.generateBankOrderLineFromInvoiceTerm(
          paymentSession, invoiceTerm, bankOrder, reconciledAmount);
    } else {
      this.updateBankOrderLine(paymentSession, invoiceTerm, bankOrderLine, reconciledAmount);
    }

    bankOrderRepo.save(paymentSession.getBankOrder());
  }

  protected void generateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrder bankOrder,
      BigDecimal reconciledAmount)
      throws AxelorException {
    LocalDate bankOrderDate = null;
    if (invoiceTerm.getAmountPaid().subtract(reconciledAmount).signum() == 0) {
      return;
    }
    if (this.isFileFormatMultiDate(paymentSession)) {
      bankOrderDate =
          paymentSession.getMoveAccountingDateSelect()
                  == PaymentSessionRepository.MOVE_ACCOUNTING_DATE_PAYMENT
              ? paymentSession.getPaymentDate()
              : invoiceTerm.getDueDate();
    }

    BankOrderLine bankOrderLine =
        bankOrderLineService.createBankOrderLine(
            bankOrder.getBankOrderFileFormat(),
            null,
            invoiceTerm.getMoveLine().getPartner(),
            invoiceTerm.getBankDetails(),
            invoiceTerm.getAmountPaid().subtract(reconciledAmount),
            paymentSession.getCurrency(),
            bankOrderDate,
            this.getReference(invoiceTerm),
            this.getLabel(paymentSession),
            invoiceTerm);

    bankOrder.addBankOrderLineListItem(bankOrderLine);
    bankOrderLine.setCompanyCurrencyAmount(
        this.getAmountPaidInCompanyCurrency(
            paymentSession, invoiceTerm, bankOrderLine, reconciledAmount));
  }

  protected void updateBankOrderLine(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrderLine bankOrderLine,
      BigDecimal reconciledAmount)
      throws AxelorException {

    if (invoiceTerm.getAmountPaid().subtract(reconciledAmount).signum() == 0) {
      return;
    }
    this.updateReference(invoiceTerm, bankOrderLine);
    bankOrderLine.setBankOrderAmount(
        bankOrderLine
            .getBankOrderAmount()
            .add(invoiceTerm.getAmountPaid().subtract(reconciledAmount)));
    if (bankOrderLine.getBankOrderAmount().signum() == 0) {
      resetBankOrderLine(bankOrderLine);
      return;
    }
    bankOrderLine.setCompanyCurrencyAmount(
        bankOrderLine
            .getCompanyCurrencyAmount()
            .add(
                this.getAmountPaidInCompanyCurrency(
                    paymentSession, invoiceTerm, bankOrderLine, reconciledAmount)));
    bankOrderLine.addBankOrderLineOriginListItem(
        bankOrderLineOriginService.createBankOrderLineOrigin(invoiceTerm));
  }

  protected String getLabel(PaymentSession paymentSession) {
    return String.format(
        "%s - %s",
        paymentSession.getPaymentMode().getName(), paymentSession.getCompany().getName());
  }

  protected String getReference(InvoiceTerm invoiceTerm) throws AxelorException {
    if (StringUtils.isEmpty(invoiceTerm.getMoveLine().getOrigin())) {
      return null;
    }
    return String.format(
        "%s (%s)",
        invoiceTerm.getMoveLine().getOrigin(),
        invoiceTerm.getDueDate().format(dateService.getDateFormat()));
  }

  protected void updateReference(InvoiceTerm invoiceTerm, BankOrderLine bankOrderLine)
      throws AxelorException {
    String newReference =
        String.format(
            "%s/%s", bankOrderLine.getReceiverReference(), this.getReference(invoiceTerm));

    if (newReference.length() < 256) {
      bankOrderLine.setReceiverReference(newReference);
    }
  }

  protected BigDecimal getAmountPaidInCompanyCurrency(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrderLine bankOrderLine,
      BigDecimal reconciledAmount)
      throws AxelorException {
    return bankOrderLine.getBankOrder().getIsMultiCurrency()
        ? currencyService
            .getAmountCurrencyConvertedAtDate(
                paymentSession.getCurrency(),
                paymentSession.getCompany().getCurrency(),
                invoiceTerm.getAmountPaid().subtract(reconciledAmount),
                bankOrderLine.getBankOrderDate())
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
        : invoiceTerm.getAmountPaid().subtract(reconciledAmount);
  }

  @Transactional
  protected void resetBankOrderLine(BankOrderLine bankOrderLine) {
    if (bankOrderLine != null) {
      BankOrder bankOrder = bankOrderLine.getBankOrder();
      bankOrder.removeBankOrderLineListItem(bankOrderLine);
      bankOrderLine.setBankOrder(null);

      if (!ObjectUtils.isEmpty(bankOrderLine.getBankOrderLineOriginList())) {
        for (BankOrderLineOrigin origin : bankOrderLine.getBankOrderLineOriginList()) {
          origin.setBankOrderLine(null);
          bankOrderLine.removeBankOrderLineOriginListItem(origin);
        }
      }

      if (bankOrderLine.getId() != null) {
        bankOrderLineRepo.remove(bankOrderLine);
      }
    }
  }

  @Override
  @Transactional
  public void manageInvoicePayment(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, BigDecimal reconciliedAmount) {
    InvoicePayment invoicePayment = this.findInvoicePayment(paymentSession, invoiceTerm);
    if (invoicePayment != null) {
      if (invoicePayment.getAmount().subtract(reconciliedAmount).signum() == 0) {
        Invoice invoice = invoicePayment.getInvoice();
        if (invoice != null) {
          invoice.removeInvoicePaymentListItem(invoicePayment);
          invoicePayment.setInvoice(null);
        }

        invoicePayment.setPaymentSession(null);
        invoicePayment.clearInvoiceTermPaymentList();
        invoicePayment.setBankOrder(null);
        invoicePayment.setMove(null);
        if (invoicePayment.getId() != null) {
          invoicePaymentRepo.remove(invoicePayment);
        }
      } else {
        invoicePayment.setAmount(invoicePayment.getAmount().subtract(reconciliedAmount));
      }
    }
  }

  protected InvoicePayment findInvoicePayment(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() == null
        || CollectionUtils.isEmpty(invoiceTerm.getInvoice().getInvoicePaymentList())) {
      return null;
    }

    return invoiceTerm.getInvoice().getInvoicePaymentList().stream()
        .filter(
            it ->
                it.getPaymentSession() != null
                    && it.getPaymentSession().equals(paymentSession)
                    && it.getInvoiceTermPaymentList().stream()
                        .anyMatch(itp -> invoiceTerm.equals(itp.getInvoiceTerm())))
        .findFirst()
        .orElse(null);
  }
}
