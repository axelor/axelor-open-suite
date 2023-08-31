package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.ScaleServiceImpl;
import java.math.BigDecimal;

public class ScaleServiceAccountImpl extends ScaleServiceImpl implements ScaleServiceAccount {

  @Override
  public BigDecimal getScaledValue(BigDecimal value) {
    return super.getScaledValue(value);
  }

  @Override
  public BigDecimal getScaledValue(BigDecimal value, int customizedScale) {
    return super.getScaledValue(value, customizedScale);
  }

  @Override
  public BigDecimal getScaledValue(Move move, BigDecimal amount, boolean isCompanyAmount) {
    return this.getScaledValue(
        amount, this.getScale(move.getCompany(), move.getCurrency(), isCompanyAmount));
  }

  @Override
  public BigDecimal getScaledValue(MoveLine moveLine, BigDecimal amount, boolean isCompanyAmount) {
    return moveLine.getMove() != null
        ? this.getScaledValue(moveLine.getMove(), amount, isCompanyAmount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getScaledValue(
      InvoiceTerm invoiceTerm, BigDecimal amount, boolean isCompanyAmount) {
    return this.getScaledValue(
        amount,
        this.getScale(invoiceTerm.getCompany(), invoiceTerm.getCurrency(), isCompanyAmount));
  }

  @Override
  public BigDecimal getScaledValue(Invoice invoice, BigDecimal amount, boolean isCompanyAmount) {
    return this.getScaledValue(
        amount, this.getScale(invoice.getCompany(), invoice.getCurrency(), isCompanyAmount));
  }

  public int getScale() {
    return super.getScale();
  }

  public int getScale(Move move, boolean isCompanyAmount) {
    return this.getScale(move.getCompany(), move.getCurrency(), isCompanyAmount);
  }

  public int getScale(MoveLine moveLine, boolean isCompanyAmount) {
    return moveLine.getMove() != null
        ? this.getScale(moveLine.getMove(), isCompanyAmount)
        : super.getScale();
  }

  public int getScale(Invoice invoice, boolean isCompanyAmount) {
    return this.getScale(invoice.getCompany(), invoice.getCurrency(), isCompanyAmount);
  }

  public int getScale(InvoiceTerm invoiceTerm, boolean isCompanyAmount) {
    return this.getScale(invoiceTerm.getCompany(), invoiceTerm.getCurrency(), isCompanyAmount);
  }

  public int getScale(Company company, Currency currency, boolean isCompanyAmount) {
    return isCompanyAmount
        ? this.getCompanyCurrencyScale(company)
        : this.getCurrencyScale(currency);
  }

  protected int getCompanyCurrencyScale(Company company) {
    return company != null && company.getCurrency() != null
        ? this.getCurrencyScale(company.getCurrency())
        : super.getScale();
  }

  protected int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : super.getScale();
  }
}
