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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.google.common.base.Preconditions;
import java.util.Optional;

public class AnalyticLineModelInitAccountService {

  public static AnalyticLineModel castAsAnalyticLineModel(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException {
    Preconditions.checkNotNull(invoiceLine);

    if (invoice == null && invoiceLine.getInvoice() != null) {
      invoice = invoiceLine.getInvoice();
    }
    Company company = null;
    TradingName tradingName = null;
    Partner partner = null;
    FiscalPosition fiscalPosition = null;
    boolean isPurchase = false;

    if (invoice != null) {
      company = invoice.getCompany();
      tradingName = invoice.getTradingName();
      partner = invoice.getPartner();
      fiscalPosition = invoice.getFiscalPosition();
      isPurchase = InvoiceToolService.isPurchase(invoice);
    }

    return new AnalyticLineModel(
        invoiceLine,
        invoiceLine.getProduct(),
        invoiceLine.getAccount(),
        company,
        tradingName,
        partner,
        isPurchase,
        invoiceLine.getCompanyExTaxTotal(),
        fiscalPosition);
  }

  public static AnalyticLineModel castAsAnalyticLineModel(MoveLine moveLine, Move move)
      throws AxelorException {
    Preconditions.checkNotNull(moveLine);

    if (move == null && moveLine.getMove() != null) {
      move = moveLine.getMove();
    }
    Company company = null;
    TradingName tradingName = null;
    Partner partner = null;
    FiscalPosition fiscalPosition = null;

    String technicalTypeSelect =
        Optional.of(moveLine)
            .map(MoveLine::getAccount)
            .map(Account::getAccountType)
            .map(AccountType::getTechnicalTypeSelect)
            .orElse(null);

    boolean isPurchase = !AccountTypeRepository.TYPE_INCOME.equals(technicalTypeSelect);
    if (move != null) {
      company = move.getCompany();
      tradingName = move.getTradingName();
      partner = move.getPartner();
      fiscalPosition = move.getFiscalPosition();
      if (move.getInvoice() != null) {
        isPurchase = InvoiceToolService.isPurchase(move.getInvoice());
      }
    }

    return new AnalyticLineModel(
        moveLine,
        null,
        moveLine.getAccount(),
        company,
        tradingName,
        partner,
        isPurchase,
        moveLine.getCredit().max(moveLine.getDebit()),
        fiscalPosition);
  }
}
