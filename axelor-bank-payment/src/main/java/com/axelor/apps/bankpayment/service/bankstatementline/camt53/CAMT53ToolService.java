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

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.DateAndDateTimeChoice;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.db.BankDetails;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CAMT53ToolService {
  void computeBankStatementDates(BankStatement bankStatement, List<AccountStatement2> stmtList);

  LocalDate computeLocalDateFromDateTimeChoice(DateAndDateTimeChoice dateTimeChoice);

  String getBalanceType(CashBalance3 balanceEntry);

  String getCreditDebitIndicatorFromReportEntry(ReportEntry2 ntry);

  String getCreditDebitIndicatorFromCashEntry(CashBalance3 balanceEntry);

  BigDecimal getReportEntryValue(ReportEntry2 ntry);

  BigDecimal getCashEntryValue(CashBalance3 balance);

  String getReference(ReportEntry2 ntry);

  String getOrigin(ReportEntry2 ntry);

  Integer getCommissionExemptionIndexSelect(ReportEntry2 ntry);

  BankDetails findBankDetailsByIBAN(CashAccount20 acct);

  String constructDescriptionFromNtry(ReportEntry2 ntry);

  InterbankCodeLine getOperationCodeInterBankCodeLineCode(ReportEntry2 ntry);

  InterbankCodeLine getRejectReturnInterBankCodeLineCode(ReportEntry2 reportEntry2);
}
