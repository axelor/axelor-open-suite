package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.List;
import java.util.StringJoiner;

public class InvoiceMergingServiceImpl implements InvoiceMergingService {

  protected static class CommonFieldsImpl implements CommonFields {
    private Company commonCompany = null;
    private Currency commonCurrency = null;
    private Partner commonPartner = null;
    private PaymentCondition commonPaymentCondition = null;
    private Partner commonContactPartner = null;
    private PriceList commonPriceList = null;
    private PaymentMode commonPaymentMode = null;

    public Company getCommonCompany() {
      return commonCompany;
    }

    public void setCommonCompany(Company commonCompany) {
      this.commonCompany = commonCompany;
    }

    public Currency getCommonCurrency() {
      return commonCurrency;
    }

    public void setCommonCurrency(Currency commonCurrency) {
      this.commonCurrency = commonCurrency;
    }

    public Partner getCommonPartner() {
      return commonPartner;
    }

    public void setCommonPartner(Partner commonPartner) {
      this.commonPartner = commonPartner;
    }

    public PaymentCondition getCommonPaymentCondition() {
      return commonPaymentCondition;
    }

    public void setCommonPaymentCondition(PaymentCondition commonPaymentCondition) {
      this.commonPaymentCondition = commonPaymentCondition;
    }

    public Partner getCommonContactPartner() {
      return commonContactPartner;
    }

    public void setCommonContactPartner(Partner commonContactPartner) {
      this.commonContactPartner = commonContactPartner;
    }

    public PriceList getCommonPriceList() {
      return commonPriceList;
    }

    public void setCommonPriceList(PriceList commonPriceList) {
      this.commonPriceList = commonPriceList;
    }

    public PaymentMode getCommonPaymentMode() {
      return commonPaymentMode;
    }

    public void setCommonPaymentMode(PaymentMode commonPaymentMode) {
      this.commonPaymentMode = commonPaymentMode;
    }
  }

  protected static class ChecksImpl implements Checks {
    private boolean existPaymentConditionDiff = false;
    private boolean existContactPartnerDiff = false;
    private boolean existPriceListDiff = false;
    private boolean existPaymentModeDiff = false;

    @Override
    public boolean isExistPaymentConditionDiff() {
      return existPaymentConditionDiff;
    }

    @Override
    public void setExistPaymentConditionDiff(boolean existPaymentConditionDiff) {
      this.existPaymentConditionDiff = existPaymentConditionDiff;
    }

    @Override
    public boolean isExistContactPartnerDiff() {
      return existContactPartnerDiff;
    }

    @Override
    public void setExistContactPartnerDiff(boolean existContactPartnerDiff) {
      this.existContactPartnerDiff = existContactPartnerDiff;
    }

    @Override
    public boolean isExistPriceListDiff() {
      return existPriceListDiff;
    }

    @Override
    public void setExistPriceListDiff(boolean existPriceListDiff) {
      this.existPriceListDiff = existPriceListDiff;
    }

    @Override
    public boolean isExistPaymentModeDiff() {
      return existPaymentModeDiff;
    }

    @Override
    public void setExistPaymentModeDiff(boolean existPaymentModeDiff) {
      this.existPaymentModeDiff = existPaymentModeDiff;
    }
  }

  protected static class InvoiceMergingResultImpl implements InvoiceMergingResult {
    private Invoice invoice;
    private boolean isConfirmationNeeded;
    private final CommonFieldsImpl commonFields;
    private final ChecksImpl checks;

    public InvoiceMergingResultImpl() {
      this.invoice = null;
      this.isConfirmationNeeded = false;
      this.commonFields = new CommonFieldsImpl();
      this.checks = new ChecksImpl();
    }

    @Override
    public void setInvoice(Invoice invoice) {
      this.invoice = invoice;
    }

    @Override
    public Invoice getInvoice() {
      return invoice;
    }

    @Override
    public void needConfirmation() {
      isConfirmationNeeded = true;
    }

    @Override
    public boolean isConfirmationNeeded() {
      return isConfirmationNeeded;
    }
  }

  @Override
  public InvoiceMergingResultImpl create() {
    return new InvoiceMergingResultImpl();
  }

  @Override
  public CommonFieldsImpl getCommonFields(InvoiceMergingResult result) {
    return ((InvoiceMergingResultImpl) result).commonFields;
  }

  @Override
  public ChecksImpl getChecks(InvoiceMergingResult result) {
    return ((InvoiceMergingResultImpl) result).checks;
  }

  @Override
  public InvoiceMergingResult mergeInvoices(List<Invoice> invoicesToMerge) throws AxelorException {
    InvoiceMergingResult result = create();

    int invoiceCount = 1;
    for (Invoice invoice : invoicesToMerge) {
      fillCommonFields(invoice, result, invoiceCount);
      invoiceCount++;
    }

    StringJoiner fieldErrors = new StringJoiner("<BR/>");
    checkErrors(fieldErrors, result);
    if (fieldErrors.length() > 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, fieldErrors.toString());
    }

    if (isConfirmationNeeded(result)) {
      return result;
    }

    result.setInvoice(mergeInvoices(invoicesToMerge, result));

    return result;
  }

  @Override
  public InvoiceMergingResult mergeInvoices(
      List<Invoice> invoicesToMerge,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition)
      throws AxelorException {
    InvoiceMergingResult result = create();

    int invoiceCount = 1;
    for (Invoice invoice : invoicesToMerge) {
      fillCommonFields(invoice, result, invoiceCount);
      invoiceCount++;
    }

    StringJoiner fieldErrors = new StringJoiner("<BR/>");
    checkErrors(fieldErrors, result);
    if (fieldErrors.length() > 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, fieldErrors.toString());
    }

    if (contactPartner != null) {
      getCommonFields(result).setCommonContactPartner(contactPartner);
    }
    if (priceList != null) {
      getCommonFields(result).setCommonPriceList(priceList);
    }
    if (paymentMode != null) {
      getCommonFields(result).setCommonPaymentMode(paymentMode);
    }
    if (paymentCondition != null) {
      getCommonFields(result).setCommonPaymentCondition(paymentCondition);
    }

    result.setInvoice(mergeInvoices(invoicesToMerge, result));

    return result;
  }

  protected void fillCommonFields(Invoice invoice, InvoiceMergingResult result, int invoiceCount) {
    if (invoiceCount == 1) {
      getCommonFields(result).setCommonCompany(invoice.getCompany());
      getCommonFields(result).setCommonCurrency(invoice.getCurrency());
      getCommonFields(result).setCommonPartner(invoice.getPartner());
      getCommonFields(result).setCommonPaymentCondition(invoice.getPaymentCondition());
      getCommonFields(result).setCommonContactPartner(invoice.getContactPartner());
      getCommonFields(result).setCommonPriceList(invoice.getPriceList());
      getCommonFields(result).setCommonPaymentMode(invoice.getPaymentMode());
    } else {
      if (getCommonFields(result).getCommonCompany() != null
          && !getCommonFields(result).getCommonCompany().equals(invoice.getCompany())) {
        getCommonFields(result).setCommonCompany(null);
      }
      if (getCommonFields(result).getCommonCurrency() != null
          && !getCommonFields(result).getCommonCurrency().equals(invoice.getCurrency())) {
        getCommonFields(result).setCommonCurrency(null);
      }
      if (getCommonFields(result).getCommonPartner() != null
          && !getCommonFields(result).getCommonPartner().equals(invoice.getPartner())) {
        getCommonFields(result).setCommonPartner(null);
      }
      if (getCommonFields(result).getCommonPaymentCondition() != null
          && !getCommonFields(result)
              .getCommonPaymentCondition()
              .equals(invoice.getPaymentCondition())) {
        getCommonFields(result).setCommonPaymentCondition(null);
        getChecks(result).setExistPaymentConditionDiff(true);
      }
      if (getCommonFields(result).getCommonContactPartner() != null
          && !getCommonFields(result)
              .getCommonContactPartner()
              .equals(invoice.getContactPartner())) {
        getCommonFields(result).setCommonContactPartner(null);
        getChecks(result).setExistContactPartnerDiff(true);
      }
      if (getCommonFields(result).getCommonPriceList() != null
          && !getCommonFields(result).getCommonPriceList().equals(invoice.getPriceList())) {
        getCommonFields(result).setCommonPriceList(null);
        getChecks(result).setExistPriceListDiff(true);
      }
      if (getCommonFields(result).getCommonPaymentMode() != null
          && !getCommonFields(result).getCommonPaymentMode().equals(invoice.getPaymentMode())) {
        getCommonFields(result).setCommonPaymentMode(null);
        getChecks(result).setExistPaymentModeDiff(true);
      }
    }
  }

  protected void checkErrors(StringJoiner fieldErrors, InvoiceMergingResult result)
      throws AxelorException {
    if (getCommonFields(result).getCommonCurrency() == null) {
      fieldErrors.add(
          I18n.get(
              com.axelor.apps.account.exception.IExceptionMessage.INVOICE_MERGE_ERROR_CURRENCY));
    }
    if (getCommonFields(result).getCommonCompany() == null) {
      fieldErrors.add(
          I18n.get(
              com.axelor.apps.account.exception.IExceptionMessage.INVOICE_MERGE_ERROR_COMPANY));
    }
    if (getCommonFields(result).getCommonPartner() == null) {
      fieldErrors.add(
          I18n.get(
              com.axelor.apps.account.exception.IExceptionMessage.INVOICE_MERGE_ERROR_PARTNER));
    }
  }

  protected boolean isConfirmationNeeded(InvoiceMergingResult result) {
    if (getChecks(result).isExistPaymentConditionDiff()
        || getChecks(result).isExistContactPartnerDiff()
        || getChecks(result).isExistPriceListDiff()
        || getChecks(result).isExistPaymentModeDiff()) {
      result.needConfirmation();
      return true;
    }
    return false;
  }

  protected Invoice mergeInvoices(List<Invoice> invoicesToMerge, InvoiceMergingResult result)
      throws AxelorException {
    return generateMergedInvoice(invoicesToMerge, result);
  }

  protected Invoice generateMergedInvoice(
      List<Invoice> invoicesToMerge, InvoiceMergingResult result) throws AxelorException {
    return Beans.get(InvoiceService.class)
        .mergeInvoiceProcess(
            invoicesToMerge,
            getCommonFields(result).getCommonCompany(),
            getCommonFields(result).getCommonCurrency(),
            getCommonFields(result).getCommonPartner(),
            getCommonFields(result).getCommonContactPartner(),
            getCommonFields(result).getCommonPriceList(),
            getCommonFields(result).getCommonPaymentMode(),
            getCommonFields(result).getCommonPaymentCondition());
  }
}
