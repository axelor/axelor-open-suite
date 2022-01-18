package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.time.LocalDate;
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
    private String commonSupplierInvoiceNb = null;
    private LocalDate commonOriginDate = null;

    @Override
    public Company getCommonCompany() {
      return commonCompany;
    }

    @Override
    public void setCommonCompany(Company commonCompany) {
      this.commonCompany = commonCompany;
    }

    @Override
    public Currency getCommonCurrency() {
      return commonCurrency;
    }

    @Override
    public void setCommonCurrency(Currency commonCurrency) {
      this.commonCurrency = commonCurrency;
    }

    @Override
    public Partner getCommonPartner() {
      return commonPartner;
    }

    @Override
    public void setCommonPartner(Partner commonPartner) {
      this.commonPartner = commonPartner;
    }

    @Override
    public PaymentCondition getCommonPaymentCondition() {
      return commonPaymentCondition;
    }

    @Override
    public void setCommonPaymentCondition(PaymentCondition commonPaymentCondition) {
      this.commonPaymentCondition = commonPaymentCondition;
    }

    @Override
    public Partner getCommonContactPartner() {
      return commonContactPartner;
    }

    @Override
    public void setCommonContactPartner(Partner commonContactPartner) {
      this.commonContactPartner = commonContactPartner;
    }

    @Override
    public PriceList getCommonPriceList() {
      return commonPriceList;
    }

    @Override
    public void setCommonPriceList(PriceList commonPriceList) {
      this.commonPriceList = commonPriceList;
    }

    @Override
    public PaymentMode getCommonPaymentMode() {
      return commonPaymentMode;
    }

    @Override
    public void setCommonPaymentMode(PaymentMode commonPaymentMode) {
      this.commonPaymentMode = commonPaymentMode;
    }

    @Override
    public String getCommonSupplierInvoiceNb() {
      return commonSupplierInvoiceNb;
    }

    @Override
    public void setCommonSupplierInvoiceNb(String commonSupplierInvoiceNb) {
      this.commonSupplierInvoiceNb = commonSupplierInvoiceNb;
    }

    @Override
    public LocalDate getCommonOriginDate() {
      return commonOriginDate;
    }

    @Override
    public void setCommonOriginDate(LocalDate commonOriginDate) {
      this.commonOriginDate = commonOriginDate;
    }
  }

  protected static class ChecksImpl implements Checks {
    private boolean existPaymentConditionDiff = false;
    private boolean existContactPartnerDiff = false;
    private boolean existPriceListDiff = false;
    private boolean existPaymentModeDiff = false;
    private boolean existSupplierInvoiceNbDiff = false;
    private boolean existOriginDateDiff = false;

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

    @Override
    public boolean isExistSupplierInvoiceNbDiff() {
      return existSupplierInvoiceNbDiff;
    }

    @Override
    public void setExistSupplierInvoiceNbDiff(boolean existSupplierInvoiceNbDiff) {
      this.existSupplierInvoiceNbDiff = existSupplierInvoiceNbDiff;
    }

    @Override
    public boolean isExistOriginDateDiff() {
      return existOriginDateDiff;
    }

    @Override
    public void setExistOriginDateDiff(boolean existOriginDateDiff) {
      this.existOriginDateDiff = existOriginDateDiff;
    }
  }

  protected static class InvoiceMergingResultImpl implements InvoiceMergingResult {
    private Integer invoiceType;
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
    public void setInvoiceType(Integer type) {
      invoiceType = type;
    }

    @Override
    public Integer getInvoiceType() {
      return invoiceType;
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

  @Override
  public InvoiceMergingResult mergeInvoices(
      List<Invoice> invoicesToMerge,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      String supplierInvoiceNb,
      LocalDate originDate)
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
    if (supplierInvoiceNb != null) {
      getCommonFields(result).setCommonSupplierInvoiceNb(supplierInvoiceNb);
    }
    if (originDate != null) {
      getCommonFields(result).setCommonOriginDate(originDate);
    }

    result.setInvoice(mergeInvoices(invoicesToMerge, result));

    return result;
  }

  protected void fillCommonFields(Invoice invoice, InvoiceMergingResult result, int invoiceCount) {
    if (invoiceCount == 1) {
      result.setInvoiceType(invoice.getOperationTypeSelect());
      getCommonFields(result).setCommonCompany(invoice.getCompany());
      getCommonFields(result).setCommonCurrency(invoice.getCurrency());
      getCommonFields(result).setCommonPartner(invoice.getPartner());
      getCommonFields(result).setCommonPaymentCondition(invoice.getPaymentCondition());
      getCommonFields(result).setCommonContactPartner(invoice.getContactPartner());
      getCommonFields(result).setCommonPriceList(invoice.getPriceList());
      getCommonFields(result).setCommonPaymentMode(invoice.getPaymentMode());
      if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
        getCommonFields(result).setCommonSupplierInvoiceNb(invoice.getSupplierInvoiceNb());
        getCommonFields(result).setCommonOriginDate(invoice.getOriginDate());
      }
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
      if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
        if (getCommonFields(result).getCommonSupplierInvoiceNb() != null
            && !getCommonFields(result)
                .getCommonSupplierInvoiceNb()
                .equals(invoice.getSupplierInvoiceNb())) {
          getCommonFields(result).setCommonSupplierInvoiceNb(null);
          getChecks(result).setExistSupplierInvoiceNbDiff(true);
        }
        if (getCommonFields(result).getCommonOriginDate() != null
            && !getCommonFields(result).getCommonOriginDate().equals(invoice.getOriginDate())) {
          getCommonFields(result).setCommonOriginDate(null);
          getChecks(result).setExistOriginDateDiff(true);
        }
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
        || getChecks(result).isExistPaymentModeDiff()
        || getChecks(result).isExistSupplierInvoiceNbDiff()
        || getChecks(result).isExistOriginDateDiff()) {
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
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
      return Beans.get(InvoiceService.class)
          .mergeInvoiceProcess(
              invoicesToMerge,
              getCommonFields(result).getCommonCompany(),
              getCommonFields(result).getCommonCurrency(),
              getCommonFields(result).getCommonPartner(),
              getCommonFields(result).getCommonContactPartner(),
              getCommonFields(result).getCommonPriceList(),
              getCommonFields(result).getCommonPaymentMode(),
              getCommonFields(result).getCommonPaymentCondition(),
              getCommonFields(result).getCommonSupplierInvoiceNb(),
              getCommonFields(result).getCommonOriginDate());
    }
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
