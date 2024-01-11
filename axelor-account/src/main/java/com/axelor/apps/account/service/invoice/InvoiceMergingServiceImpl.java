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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class InvoiceMergingServiceImpl implements InvoiceMergingService {

  protected final InvoiceService invoiceService;

  @Inject
  public InvoiceMergingServiceImpl(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

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
    private TradingName commonTradingName;
    private FiscalPosition commonFiscalPosition;

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

    @Override
    public void setCommonTradingName(TradingName commonTradingName) {
      this.commonTradingName = commonTradingName;
    }

    @Override
    public TradingName getCommonTradingName() {
      return commonTradingName;
    }

    @Override
    public void setCommonFiscalPosition(FiscalPosition commonFiscalPosition) {
      this.commonFiscalPosition = commonFiscalPosition;
    }

    @Override
    public FiscalPosition getCommonFiscalPosition() {
      return commonFiscalPosition;
    }
  }

  protected static class ChecksImpl implements Checks {
    private boolean existPaymentConditionDiff = false;
    private boolean existContactPartnerDiff = false;
    private boolean existPriceListDiff = false;
    private boolean existPaymentModeDiff = false;
    private boolean existSupplierInvoiceNbDiff = false;
    private boolean existOriginDateDiff = false;
    private boolean existTradingNameDiff = false;
    private boolean existFiscalPositionDiff = false;

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

    @Override
    public void setExistTradingNameDiff(boolean existTradingNameDiff) {
      this.existTradingNameDiff = existTradingNameDiff;
    }

    @Override
    public boolean isExistTradingNameDiff() {
      return existTradingNameDiff;
    }

    @Override
    public void setExistFiscalPositionDiff(boolean existFiscalPositionDiff) {
      this.existFiscalPositionDiff = existFiscalPositionDiff;
    }

    @Override
    public boolean isExistFiscalPositionDiff() {
      return existFiscalPositionDiff;
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

    extractFirstNonNullCommonFields(invoicesToMerge, result);
    for (Invoice invoice : invoicesToMerge) {
      fillCommonFields(invoice, result);
    }

    StringJoiner fieldErrors = new StringJoiner("<BR/>");
    checkErrors(fieldErrors, result);
    if (fieldErrors.length() > 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, fieldErrors.toString());
    }

    if (isConfirmationNeeded(result)) {
      result.needConfirmation();
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
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException {
    InvoiceMergingResult result = create();

    extractFirstNonNullCommonFields(invoicesToMerge, result);
    for (Invoice invoice : invoicesToMerge) {
      fillCommonFields(invoice, result);
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
    if (tradingName != null) {
      getCommonFields(result).setCommonTradingName(tradingName);
    }
    if (fiscalPosition != null) {
      getCommonFields(result).setCommonFiscalPosition(fiscalPosition);
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
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate)
      throws AxelorException {
    InvoiceMergingResult result = create();

    extractFirstNonNullCommonFields(invoicesToMerge, result);
    for (Invoice invoice : invoicesToMerge) {
      fillCommonFields(invoice, result);
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
    if (tradingName != null) {
      getCommonFields(result).setCommonTradingName(tradingName);
    }
    if (fiscalPosition != null) {
      getCommonFields(result).setCommonFiscalPosition(fiscalPosition);
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

  protected void extractFirstNonNullCommonFields(
      List<Invoice> invoicesToMerge, InvoiceMergingResult result) {
    if (invoicesToMerge == null) {
      return;
    }
    invoicesToMerge.stream()
        .map(Invoice::getOperationTypeSelect)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(result::setInvoiceType);
    invoicesToMerge.stream()
        .map(Invoice::getCompany)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonCompany(it));
    invoicesToMerge.stream()
        .map(Invoice::getCurrency)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonCurrency(it));
    invoicesToMerge.stream()
        .map(Invoice::getPartner)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonPartner(it));
    invoicesToMerge.stream()
        .map(Invoice::getPaymentCondition)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonPaymentCondition(it));
    invoicesToMerge.stream()
        .map(Invoice::getContactPartner)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonContactPartner(it));
    invoicesToMerge.stream()
        .map(Invoice::getPriceList)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonPriceList(it));
    invoicesToMerge.stream()
        .map(Invoice::getPaymentMode)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonPaymentMode(it));
    invoicesToMerge.stream()
        .map(Invoice::getTradingName)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonTradingName(it));
    invoicesToMerge.stream()
        .map(Invoice::getFiscalPosition)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonFiscalPosition(it));
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
      invoicesToMerge.stream()
          .map(Invoice::getSupplierInvoiceNb)
          .filter(Objects::nonNull)
          .findFirst()
          .ifPresent(it -> getCommonFields(result).setCommonSupplierInvoiceNb(it));
      invoicesToMerge.stream()
          .map(Invoice::getOriginDate)
          .filter(Objects::nonNull)
          .findFirst()
          .ifPresent(it -> getCommonFields(result).setCommonOriginDate(it));
    }
  }

  protected void fillCommonFields(Invoice invoice, InvoiceMergingResult result) {
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
        && !getCommonFields(result).getCommonContactPartner().equals(invoice.getContactPartner())) {
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
    if (getCommonFields(result).getCommonTradingName() != null
        && !getCommonFields(result).getCommonTradingName().equals(invoice.getTradingName())) {
      getCommonFields(result).setCommonTradingName(null);
      getChecks(result).setExistTradingNameDiff(true);
    }
    if (getCommonFields(result).getCommonFiscalPosition() != null
        && !getCommonFields(result).getCommonFiscalPosition().equals(invoice.getFiscalPosition())) {
      getCommonFields(result).setCommonFiscalPosition(null);
      getChecks(result).setExistFiscalPositionDiff(true);
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

  protected void checkErrors(StringJoiner fieldErrors, InvoiceMergingResult result)
      throws AxelorException {
    if (getCommonFields(result).getCommonCurrency() == null) {
      fieldErrors.add(
          I18n.get(
              com.axelor.apps.account.exception.AccountExceptionMessage
                  .INVOICE_MERGE_ERROR_CURRENCY));
    }
    if (getCommonFields(result).getCommonCompany() == null) {
      fieldErrors.add(
          I18n.get(
              com.axelor.apps.account.exception.AccountExceptionMessage
                  .INVOICE_MERGE_ERROR_COMPANY));
    }
    if (getCommonFields(result).getCommonPartner() == null) {
      fieldErrors.add(
          I18n.get(
              com.axelor.apps.account.exception.AccountExceptionMessage
                  .INVOICE_MERGE_ERROR_PARTNER));
    }
  }

  protected boolean isConfirmationNeeded(InvoiceMergingResult result) {
    return getChecks(result).isExistPaymentConditionDiff()
        || getChecks(result).isExistContactPartnerDiff()
        || getChecks(result).isExistPriceListDiff()
        || getChecks(result).isExistPaymentModeDiff()
        || getChecks(result).isExistSupplierInvoiceNbDiff()
        || getChecks(result).isExistOriginDateDiff()
        || getChecks(result).isExistTradingNameDiff()
        || getChecks(result).isExistFiscalPositionDiff();
  }

  protected Invoice mergeInvoices(List<Invoice> invoicesToMerge, InvoiceMergingResult result)
      throws AxelorException {
    return generateMergedInvoice(invoicesToMerge, result);
  }

  protected Invoice generateMergedInvoice(
      List<Invoice> invoicesToMerge, InvoiceMergingResult result) throws AxelorException {
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
      return invoiceService.mergeInvoiceProcess(
          invoicesToMerge,
          getCommonFields(result).getCommonCompany(),
          getCommonFields(result).getCommonCurrency(),
          getCommonFields(result).getCommonPartner(),
          getCommonFields(result).getCommonContactPartner(),
          getCommonFields(result).getCommonPriceList(),
          getCommonFields(result).getCommonPaymentMode(),
          getCommonFields(result).getCommonPaymentCondition(),
          getCommonFields(result).getCommonTradingName(),
          getCommonFields(result).getCommonFiscalPosition(),
          getCommonFields(result).getCommonSupplierInvoiceNb(),
          getCommonFields(result).getCommonOriginDate());
    }
    return invoiceService.mergeInvoiceProcess(
        invoicesToMerge,
        getCommonFields(result).getCommonCompany(),
        getCommonFields(result).getCommonCurrency(),
        getCommonFields(result).getCommonPartner(),
        getCommonFields(result).getCommonContactPartner(),
        getCommonFields(result).getCommonPriceList(),
        getCommonFields(result).getCommonPaymentMode(),
        getCommonFields(result).getCommonPaymentCondition(),
        getCommonFields(result).getCommonTradingName(),
        getCommonFields(result).getCommonFiscalPosition());
  }
}
