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

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceTermToolService {
  boolean isPartiallyPaid(InvoiceTerm invoiceTerm);

  boolean isEnoughAmountToPay(List<InvoiceTerm> invoiceTermList, BigDecimal amount, LocalDate date);

  boolean isNotReadonly(InvoiceTerm invoiceTerm);

  boolean isNotReadonlyExceptPfp(InvoiceTerm invoiceTerm);

  BigDecimal getAmountRemaining(InvoiceTerm invoiceTerm, LocalDate date, boolean isCompanyCurrency);

  boolean isThresholdNotOnLastUnpaidInvoiceTerm(
      MoveLine moveLine, BigDecimal thresholdDistanceFromRegulation);

  BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal);

  BigDecimal computeCustomizedPercentageUnscaled(BigDecimal amount, BigDecimal inTaxTotal);

  List<InvoiceTerm> getInvoiceTerms(List<Long> invoiceTermIds);
}
