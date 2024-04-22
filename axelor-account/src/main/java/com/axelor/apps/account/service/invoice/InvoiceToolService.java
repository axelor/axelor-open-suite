/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentConditionLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

/** InvoiceService est une classe implémentant l'ensemble des services de facturations. */
@RequestScoped
public class InvoiceToolService {

  @CallMethod
  public static LocalDate getDueDate(Invoice invoice) throws AxelorException {
    LocalDate invoiceDate =
        isPurchase(invoice) ? invoice.getOriginDate() : invoice.getInvoiceDate();
    return ObjectUtils.isEmpty(invoice.getInvoiceTermList())
        ? getMaxDueDate(invoice.getPaymentCondition(), invoiceDate)
        : Beans.get(InvoiceTermService.class).getDueDate(invoice.getInvoiceTermList(), invoiceDate);
  }

  protected static LocalDate getMaxDueDate(
      PaymentCondition paymentCondition, LocalDate defaultDate) {
    if (paymentCondition == null
        || ObjectUtils.isEmpty(paymentCondition.getPaymentConditionLineList())) {
      return defaultDate;
    }

    return getDueDate(
        paymentCondition.getPaymentConditionLineList().stream()
            .max(Comparator.comparing(PaymentConditionLine::getSequence))
            .get(),
        defaultDate);
  }

  @CallMethod
  public static LocalDate getNextDueDate(Invoice invoice) throws AxelorException {
    LocalDate invoiceDate =
        isPurchase(invoice) ? invoice.getOriginDate() : invoice.getInvoiceDate();
    if (CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return invoiceDate;
    }
    if (invoice.getInvoiceTermList().size() == 1) {
      return invoice.getInvoiceTermList().get(0).getDueDate();
    }
    return invoice.getInvoiceTermList().stream()
        .filter(
            invoiceTerm ->
                invoiceTerm.getDueDate() != null
                    && (invoiceTerm.getDueDate().isEqual(LocalDate.now())
                        || invoiceTerm.getDueDate().isAfter(LocalDate.now()))
                    && !invoiceTerm.getIsPaid())
        .map(invoiceTerm -> invoiceTerm.getDueDate())
        .filter(Objects::nonNull)
        .min(Comparator.comparing(LocalDate::toEpochDay))
        .orElse(invoice.getNextDueDate());
  }

  /**
   * Method to compute due date based on paymentConditionLine and invoiceDate
   *
   * @param paymentConditionLine
   * @param invoiceDate
   * @return
   */
  public static LocalDate getDueDate(
      PaymentConditionLine paymentConditionLine, LocalDate invoiceDate) {

    return getDueDate(
        paymentConditionLine.getTypeSelect(),
        paymentConditionLine.getPaymentTime(),
        paymentConditionLine.getPeriodTypeSelect(),
        paymentConditionLine.getDaySelect(),
        invoiceDate);
  }

  /**
   * Method to compute due date based on paymentCondition and invoiceDate
   *
   * @param typeSelect
   * @param paymentTime
   * @param periodTypeSelect
   * @param daySelect
   * @param invoiceDate
   * @return
   */
  public static LocalDate getDueDate(
      Integer typeSelect,
      Integer paymentTime,
      Integer periodTypeSelect,
      Integer daySelect,
      LocalDate invoiceDate) {
    if (invoiceDate == null) {
      return null;
    }

    LocalDate nDaysDate;
    if (periodTypeSelect.equals(PaymentConditionLineRepository.PERIOD_TYPE_DAYS)) {
      nDaysDate = invoiceDate.plusDays(paymentTime);
    } else {
      nDaysDate = invoiceDate.plusMonths(paymentTime);
    }

    switch (typeSelect) {
      case PaymentConditionLineRepository.TYPE_NET:
        return nDaysDate;

      case PaymentConditionLineRepository.TYPE_END_OF_MONTH_N_DAYS:
        if (periodTypeSelect.equals(PaymentConditionLineRepository.PERIOD_TYPE_DAYS)) {
          return invoiceDate.withDayOfMonth(invoiceDate.lengthOfMonth()).plusDays(paymentTime);
        } else {
          return invoiceDate.withDayOfMonth(invoiceDate.lengthOfMonth()).plusMonths(paymentTime);
        }
      case PaymentConditionLineRepository.TYPE_N_DAYS_END_OF_MONTH:
        return nDaysDate.withDayOfMonth(nDaysDate.lengthOfMonth());

      case PaymentConditionLineRepository.TYPE_N_DAYS_END_OF_MONTH_AT:
        return nDaysDate.withDayOfMonth(nDaysDate.lengthOfMonth()).plusDays(daySelect);
      default:
        return invoiceDate;
    }
  }

  /**
   * @param invoice
   *     <p>OperationTypeSelect 1 : Achat fournisseur 2 : Avoir fournisseur 3 : Vente client 4 :
   *     Avoir client
   * @return
   * @throws AxelorException
   */
  public static boolean isPurchase(Invoice invoice) throws AxelorException {

    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE: // fall-through
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        return true;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND: // fall-through
        return false;

      default:
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.MOVE_1),
            invoice.getInvoiceId());
    }
  }

  /**
   * @param invoice
   *     <p>OperationTypeSelect 1 : Achat fournisseur 2 : Avoir fournisseur 3 : Vente client 4 :
   *     Avoir client
   * @return
   * @throws AxelorException
   */
  public static boolean isRefund(Invoice invoice) throws AxelorException {

    boolean isRefund;

    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        isRefund = false;
        break;
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        isRefund = true;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        isRefund = false;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        isRefund = true;
        break;

      default:
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.MOVE_1),
            invoice.getInvoiceId());
    }

    return isRefund;
  }

  /**
   * @param invoice
   * @return
   * @throws AxelorException
   */
  public static boolean isOutPayment(Invoice invoice) throws AxelorException {
    if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) >= 0) {
      // result of XOR operator, we could also have written "bool1 ^ bool2"
      return (isPurchase(invoice) != isRefund(invoice));
    } else {
      // return opposite if total amount is negative
      return (isPurchase(invoice) == isRefund(invoice));
    }
  }

  public static PaymentMode getPaymentMode(Invoice invoice) throws AxelorException {
    Partner partner = invoice.getPartner();
    Company company = invoice.getCompany();

    if (InvoiceToolService.isOutPayment(invoice)) {
      if (partner != null) {
        PaymentMode paymentMode = partner.getOutPaymentMode();
        if (paymentMode != null) {
          return paymentMode;
        }
      }
      if (company != null) {
        return Beans.get(AccountConfigService.class).getAccountConfig(company).getOutPaymentMode();
      }
    } else {
      if (partner != null) {
        PaymentMode paymentMode = partner.getInPaymentMode();
        if (paymentMode != null) {
          return paymentMode;
        }
      }
      if (company != null) {
        return Beans.get(AccountConfigService.class).getAccountConfig(company).getInPaymentMode();
      }
    }
    return null;
  }

  public static PaymentCondition getPaymentCondition(Invoice invoice) throws AxelorException {
    Partner partner = invoice.getPartner();

    if (partner != null) {
      PaymentCondition paymentCondition = partner.getPaymentCondition();
      if (paymentCondition != null) {
        return paymentCondition;
      }
    }
    if (invoice.getCompany() == null) {
      return null;
    }
    return Beans.get(AccountConfigService.class)
        .getAccountConfig(invoice.getCompany())
        .getDefPaymentCondition();
  }

  /**
   * Method to call after copying an invoice to reset the status. Can be used after JPA.copy to
   * reset invoice status without losing references to other objets.<br>
   * <b>Most of the time you do not want to use this method directly but call {@link
   * InvoiceRepository#save(Invoice)} instead.</b>
   *
   * @param copy a copy of an invoice
   */
  public static void resetInvoiceStatusOnCopy(Invoice copy) throws AxelorException {
    copy.setStatusSelect(InvoiceRepository.STATUS_DRAFT);
    copy.setInvoiceId(null);
    copy.setInvoiceDate(null);
    copy.setDueDate(null);
    copy.setValidatedByUser(null);
    copy.setMove(null);
    copy.setOriginalInvoice(null);
    copy.setCompanyInTaxTotalRemaining(BigDecimal.ZERO);
    copy.setAmountPaid(BigDecimal.ZERO);
    copy.setAmountRemaining(copy.getInTaxTotal());
    copy.setIrrecoverableStatusSelect(InvoiceRepository.IRRECOVERABLE_STATUS_NOT_IRRECOUVRABLE);
    copy.setAmountRejected(BigDecimal.ZERO);
    copy.setPaymentProgress(0);
    copy.clearBatchSet();
    copy.setDebitNumber(null);
    copy.setDoubtfulCustomerOk(false);
    copy.setMove(null);
    copy.setInterbankCodeLine(null);
    copy.setPaymentMove(null);
    copy.clearRefundInvoiceList();
    copy.setRejectDateTime(null);
    copy.setOriginalInvoice(null);
    copy.setUsherPassageOk(false);
    copy.setAlreadyPrintedOk(false);
    copy.setCanceledPaymentSchedule(null);
    copy.setDirectDebitAmount(BigDecimal.ZERO);
    copy.setImportId(null);
    copy.setPartnerAccount(null);
    copy.setJournal(null);
    copy.clearInvoicePaymentList();
    copy.setPrintedPDF(null);
    copy.setValidatedDateTime(null);
    copy.setVentilatedByUser(null);
    copy.setVentilatedDateTime(null);
    copy.setDecisionPfpTakenDateTime(null);
    copy.setInternalReference(null);
    copy.setExternalReference(null);
    copy.setLcrAccounted(false);
    copy.clearInvoiceTermList();
    setFinancialDiscount(copy);
    copy.setOldMove(null);
    copy.setBillOfExchangeBlockingOk(false);
    copy.setBillOfExchangeBlockingReason(null);
    copy.setBillOfExchangeBlockingToDate(null);
    copy.setBillOfExchangeBlockingByUser(null);
    copy.setNextDueDate(getNextDueDate(copy));
    setPfpStatus(copy);
    copy.setHasPendingPayments(false);
    copy.setReasonOfRefusalToPay(null);
    copy.setReasonOfRefusalToPayStr(null);
    copy.setSubrogationRelease(null);
    copy.setSubrogationReleaseMove(null);
    copy.setOriginDate(null);
    copy.setSupplierInvoiceNb(null);
  }

  /**
   * Returns the functional origin of the invoice
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  public static int getFunctionalOrigin(Invoice invoice) throws AxelorException {
    int functionalOrigin = 0;
    if (isPurchase(invoice)) {
      functionalOrigin = MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE;
    } else {
      functionalOrigin = MoveRepository.FUNCTIONAL_ORIGIN_SALE;
    }
    return functionalOrigin;
  }

  public static void setPfpStatus(Invoice invoice) throws AxelorException {
    AccountConfig accountConfig =
        Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany());

    if (accountConfig.getIsManagePassedForPayment()
        && (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
                && accountConfig.getIsManagePFPInRefund()))) {
      invoice.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_AWAITING);
    } else {
      invoice.setPfpValidateStatusSelect(InvoiceRepository.PFP_NONE);
    }
  }

  public static boolean isMultiCurrency(Invoice invoice) {
    return invoice != null
        && invoice.getCurrency() != null
        && invoice.getCompany() != null
        && !Objects.equals(invoice.getCurrency(), invoice.getCompany().getCurrency());
  }

  public static void checkUseForPartnerBalanceAndReconcileOk(Invoice invoice)
      throws AxelorException {
    if (invoice.getPartnerAccount() != null
        && (!invoice.getPartnerAccount().getReconcileOk()
            || !invoice.getPartnerAccount().getUseForPartnerBalance())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.ACCOUNT_USE_FOR_PARTNER_BALANCE_AND_RECONCILE_OK),
          invoice.getPartnerAccount().getName());
    }
  }

  protected static void setFinancialDiscount(Invoice copy) {
    FinancialDiscount financialDiscount =
        Optional.of(copy).map(Invoice::getPartner).map(Partner::getFinancialDiscount).orElse(null);
    BigDecimal discountRate = BigDecimal.ZERO;
    BigDecimal financialDiscountTotalAmount = BigDecimal.ZERO;
    BigDecimal remainingAmountAfterFinDiscount = BigDecimal.ZERO;
    String legalNotice = null;
    BigDecimal inTaxTotal = copy.getInTaxTotal();

    if (financialDiscount != null) {
      discountRate = financialDiscount.getDiscountRate();
      financialDiscountTotalAmount =
          discountRate.multiply(inTaxTotal).divide(BigDecimal.valueOf(100));
      remainingAmountAfterFinDiscount = inTaxTotal.subtract(financialDiscountTotalAmount);
      legalNotice = financialDiscount.getLegalNotice();
    }
    copy.setFinancialDiscount(financialDiscount);
    copy.setFinancialDiscountDeadlineDate(null);
    copy.setFinancialDiscountRate(discountRate);
    copy.setFinancialDiscountTotalAmount(financialDiscountTotalAmount);
    copy.setRemainingAmountAfterFinDiscount(remainingAmountAfterFinDiscount);
    copy.setLegalNotice(legalNotice);
  }
}
