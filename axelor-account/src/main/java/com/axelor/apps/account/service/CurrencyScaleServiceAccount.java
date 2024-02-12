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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import java.math.BigDecimal;

public interface CurrencyScaleServiceAccount extends CurrencyScaleService {

  BigDecimal getScaledValue(Move move, BigDecimal amount);

  BigDecimal getCompanyScaledValue(Move move, BigDecimal amount);

  BigDecimal getScaledValue(MoveLine moveLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(MoveLine moveLine, BigDecimal amount);

  BigDecimal getScaledValue(InvoiceTerm invoiceTerm, BigDecimal amount);

  BigDecimal getCompanyScaledValue(InvoiceTerm invoiceTerm, BigDecimal amount);

  BigDecimal getScaledValue(Invoice invoice, BigDecimal amount);

  BigDecimal getCompanyScaledValue(Invoice invoice, BigDecimal amount);

  BigDecimal getScaledValue(InvoiceLine invoiceLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(InvoiceLine invoiceLine, BigDecimal amount);

  BigDecimal getScaledValue(InvoicePayment invoicePayment, BigDecimal amount);

  BigDecimal getCompanyScaledValue(InvoicePayment invoicePayment, BigDecimal amount);

  BigDecimal getScaledValue(AnalyticMoveLine analyticMoveLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(Company company, BigDecimal amount);

  BigDecimal getScaledValue(PaymentVoucher paymentVoucher, BigDecimal amount);

  BigDecimal getCompanyScaledValue(PaymentVoucher paymentVoucher, BigDecimal amount);

  BigDecimal getCompanyScaledValue(FixedAsset fixedAsset, BigDecimal amount);

  BigDecimal getCompanyScaledValue(FixedAssetLine fixedAssetLine, BigDecimal amount)
      throws AxelorException;

  int getScale(Move move);

  int getCompanyScale(Move move);

  int getScale(MoveLine moveLine);

  int getCompanyScale(MoveLine moveLine);

  int getScale(Invoice invoice);

  int getCompanyScale(Invoice invoice);

  int getScale(InvoiceLine invoiceLine);

  int getCompanyScale(InvoiceLine invoiceLine);

  int getScale(InvoiceTerm invoiceTerm);

  int getCompanyScale(InvoiceTerm invoiceTerm);

  int getScale(InvoicePayment invoicePayment);

  int getCompanyScale(InvoicePayment invoicePayment);

  int getScale(AnalyticMoveLine analyticMoveLine);

  int getScale(PaymentVoucher paymentVoucher);

  int getCompanyScale(PaymentVoucher paymentVoucher);

  int getCompanyScale(FixedAsset fixedAsset);

  int getCompanyScale(FixedAssetLine fixedAssetLine) throws AxelorException;

  int getScale(Currency currency);

  int getCompanyScale(Company company);

  boolean isGreaterThan(
      BigDecimal amount1, BigDecimal amount2, MoveLine moveLine, boolean isCompanyValue);

  boolean equals(BigDecimal amount1, BigDecimal amount2, MoveLine moveLine, boolean isCompanyValue);
}
