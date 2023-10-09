package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;

public interface ScaleServiceAccount {

  BigDecimal getScaledValue(Move move, BigDecimal amount, boolean isCompanyAmount);

  BigDecimal getScaledValue(MoveLine moveLine, BigDecimal amount, boolean isCompanyAmount);

  BigDecimal getScaledValue(InvoiceTerm invoiceTerm, BigDecimal amount, boolean isCompanyAmount);

  BigDecimal getScaledValue(Invoice invoice, BigDecimal amount, boolean isCompanyAmount);

  BigDecimal getScaledValue(InvoiceLine invoiceLine, BigDecimal amount, boolean isCompanyAmount);

  int getScale(Move move, boolean isCompanyAmount);

  int getScale(MoveLine moveLine, boolean isCompanyAmount);

  int getScale(Invoice invoice, boolean isCompanyAmount);

  int getScale(InvoiceLine invoiceLine, boolean isCompanyAmount);

  int getScale(InvoiceTerm invoiceTerm, boolean isCompanyAmount);

  int getScale(Company company, Currency currency, boolean isCompanyAmount);
}
