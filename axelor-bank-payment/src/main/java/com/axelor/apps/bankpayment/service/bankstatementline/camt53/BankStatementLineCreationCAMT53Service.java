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
package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.db.BankDetails;

public interface BankStatementLineCreationCAMT53Service {
  int createBalanceLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      CashBalance3 balanceEntry,
      int sequence,
      String balanceTypeRequired,
      String currencyCodeFromStmt);

  int createEntryLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      ReportEntry2 ntry,
      int sequence,
      String currencyCodeFromStmt);
}
