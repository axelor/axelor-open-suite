package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class CurrencyScaleServiceAccountImpl extends CurrencyScaleServiceImpl
    implements CurrencyScaleServiceAccount {

  protected FixedAssetLineService fixedAssetLineService;

  @Inject
  public CurrencyScaleServiceAccountImpl(FixedAssetLineService fixedAssetLineService) {
    this.fixedAssetLineService = fixedAssetLineService;
  }

  @Override
  public BigDecimal getScaledValue(Move move, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(move.getCurrency()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(Move move, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(move.getCompany()));
  }

  @Override
  public BigDecimal getScaledValue(MoveLine moveLine, BigDecimal amount) {
    return moveLine.getMove() != null
        ? this.getScaledValue(moveLine.getMove(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(MoveLine moveLine, BigDecimal amount) {
    return moveLine.getMove() != null
        ? this.getCompanyScaledValue(moveLine.getMove(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getScaledValue(InvoiceTerm invoiceTerm, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(invoiceTerm.getCurrency()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(InvoiceTerm invoiceTerm, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(invoiceTerm.getCompany()));
  }

  @Override
  public BigDecimal getScaledValue(Invoice invoice, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(invoice.getCurrency()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(Invoice invoice, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(invoice.getCompany()));
  }

  @Override
  public BigDecimal getScaledValue(InvoiceLine invoiceLine, BigDecimal amount) {
    return invoiceLine.getInvoice() != null
        ? this.getScaledValue(invoiceLine.getInvoice(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(InvoiceLine invoiceLine, BigDecimal amount) {
    return invoiceLine.getInvoice() != null
        ? this.getCompanyScaledValue(invoiceLine.getInvoice(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getScaledValue(InvoicePayment invoicePayment, BigDecimal amount) {
    return invoicePayment.getInvoice() != null
        ? this.getScaledValue(invoicePayment.getInvoice(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(InvoicePayment invoicePayment, BigDecimal amount) {
    return invoicePayment.getInvoice() != null
        ? this.getCompanyScaledValue(invoicePayment.getInvoice(), amount)
        : this.getScaledValue(invoicePayment.getMove(), amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(FixedAsset fixedAsset, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(fixedAsset.getCompany()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(FixedAssetLine fixedAssetLine, BigDecimal amount)
      throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);
    return this.getScaledValue(amount, this.getCompanyScale(fixedAsset));
  }

  @Override
  public int getScale(Move move) {
    return this.getScale(move.getCurrency());
  }

  @Override
  public int getCompanyScale(Move move) {
    return this.getCompanyScale(move.getCompany());
  }

  @Override
  public int getScale(MoveLine moveLine) {
    return moveLine.getMove() != null ? this.getScale(moveLine.getMove()) : this.getScale();
  }

  @Override
  public int getCompanyScale(MoveLine moveLine) {
    return moveLine.getMove() != null ? this.getCompanyScale(moveLine.getMove()) : this.getScale();
  }

  @Override
  public int getScale(Invoice invoice) {
    return this.getScale(invoice.getCurrency());
  }

  @Override
  public int getCompanyScale(Invoice invoice) {
    return this.getCompanyScale(invoice.getCompany());
  }

  @Override
  public int getScale(InvoiceLine invoiceLine) {
    return invoiceLine.getInvoice() != null
        ? this.getScale(invoiceLine.getInvoice())
        : this.getScale();
  }

  @Override
  public int getCompanyScale(InvoiceLine invoiceLine) {
    return invoiceLine.getInvoice() != null
        ? this.getCompanyScale(invoiceLine.getInvoice())
        : this.getScale();
  }

  @Override
  public int getScale(InvoiceTerm invoiceTerm) {
    return this.getScale(invoiceTerm.getCurrency());
  }

  @Override
  public int getCompanyScale(InvoiceTerm invoiceTerm) {
    return this.getCompanyScale(invoiceTerm.getCompany());
  }

  @Override
  public int getScale(InvoicePayment invoicePayment) {
    return this.getScale(invoicePayment.getCurrency());
  }

  @Override
  public int getCompanyScale(InvoicePayment invoicePayment) {
    return invoicePayment.getInvoice() != null
        ? this.getCompanyScale(invoicePayment.getInvoice())
        : this.getCompanyScale(invoicePayment.getMove());
  }

  @Override
  public int getCompanyScale(FixedAsset fixedAsset) {
    return this.getCompanyScale(fixedAsset.getCompany());
  }

  @Override
  public int getCompanyScale(FixedAssetLine fixedAssetLine) throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);
    return this.getCompanyScale(fixedAsset);
  }

  @Override
  public int getScale(Currency currency) {
    return this.getCurrencyScale(currency);
  }

  @Override
  public int getCompanyScale(Company company) {
    return this.getCompanyCurrencyScale(company);
  }

  protected int getCompanyCurrencyScale(Company company) {
    return company != null && company.getCurrency() != null
        ? this.getCurrencyScale(company.getCurrency())
        : this.getScale();
  }

  protected int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : this.getScale();
  }
}
