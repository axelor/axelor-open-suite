/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.util.TaxConfiguration;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Interface of service to create {@link MoveLine} */
public interface MoveLineCreateService {

  MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal currencyAmount,
      Set<TaxLine> taxLineSet,
      BigDecimal amount,
      BigDecimal currencyRate,
      boolean isDebit,
      LocalDate date,
      LocalDate dueDate,
      LocalDate originDate,
      Integer counter,
      String origin,
      String description,
      LocalDate cutOffStartDate,
      LocalDate cutOffEndDate)
      throws AxelorException;

  MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amountInSpecificMoveCurrency,
      boolean isDebit,
      LocalDate date,
      LocalDate dueDate,
      int counter,
      String origin,
      String description)
      throws AxelorException;

  MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amountInSpecificMoveCurrency,
      BigDecimal amountInCompanyCurrency,
      BigDecimal currencyRate,
      boolean isDebit,
      LocalDate date,
      LocalDate dueDate,
      LocalDate originDate,
      int counter,
      String origin,
      String description)
      throws AxelorException;

  MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amount,
      boolean isDebit,
      LocalDate date,
      int ref,
      String origin,
      String description)
      throws AxelorException;

  MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amount,
      boolean isDebit,
      Set<TaxLine> taxLineSet,
      LocalDate date,
      int ref,
      String origin,
      String description)
      throws AxelorException;

  List<MoveLine> createMoveLines(
      Invoice invoice,
      Move move,
      Company company,
      Partner partner,
      Account partnerAccount,
      boolean consolidate,
      boolean isPurchase,
      boolean isDebitCustomer)
      throws AxelorException;

  MoveLine fillMoveLineWithInvoiceLine(MoveLine moveLine, InvoiceLine invoiceLine, Company company)
      throws AxelorException;

  MoveLine createMoveLineForAutoTax(
      Move move,
      Map<String, MoveLine> map,
      Map<String, MoveLine> newMap,
      MoveLine moveLine,
      TaxLine taxLine,
      String accountType,
      Account newAccount,
      boolean percentMoveTemplate,
      List<TaxLine> nonDeductibleTaxList)
      throws AxelorException;

  MoveLine createTaxMoveLine(
      Move move,
      Partner partner,
      boolean isDebitInvoice,
      LocalDate paymentDate,
      Integer counter,
      String origin,
      BigDecimal amount,
      BigDecimal companyAmount,
      TaxConfiguration taxConfiguration)
      throws AxelorException;
}
