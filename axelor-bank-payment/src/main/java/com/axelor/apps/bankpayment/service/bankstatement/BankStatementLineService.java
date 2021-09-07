/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class BankStatementLineService {

  public BankStatementLine createBankStatementLine(
      BankStatement bankStatement,
      int sequence,
      BankDetails bankDetails,
      BigDecimal debit,
      BigDecimal credit,
      Currency currency,
      String description,
      LocalDate operationDate,
      LocalDate valueDate,
      InterbankCodeLine operationInterbankCodeLine,
      InterbankCodeLine rejectInterbankCodeLine,
      String origin,
      String reference) {

    BankStatementLine bankStatementLine = new BankStatementLine();
    bankStatementLine.setBankStatement(bankStatement);
    bankStatementLine.setSequence(sequence);
    bankStatementLine.setBankDetails(bankDetails);
    bankStatementLine.setDebit(debit);
    bankStatementLine.setCredit(credit);
    bankStatementLine.setCurrency(currency);
    bankStatementLine.setDescription(description);
    bankStatementLine.setOperationDate(operationDate);
    bankStatementLine.setValueDate(valueDate);
    bankStatementLine.setOperationInterbankCodeLine(operationInterbankCodeLine);
    bankStatementLine.setRejectInterbankCodeLine(rejectInterbankCodeLine);
    bankStatementLine.setOrigin(origin);
    bankStatementLine.setReference(reference);

    // Used for Bank reconcile process
    bankStatementLine.setAmountRemainToReconcile(
        bankStatementLine.getDebit().add(bankStatementLine.getCredit()));

    return bankStatementLine;
  }

  public String print(LocalDate fromDate, LocalDate toDate, Long bankDetails, String exportType)
      throws AxelorException {
    String fileLink = null;
    List<BankStatementLineAFB120> bankStatementLineList =
        Beans.get(BankStatementLineAFB120Repository.class)
            .all()
            .filter(
                "self.operationDate >= ?1 and self.operationDate <= ?2 and (self.lineTypeSelect = ?3 or self.lineTypeSelect = ?4) and self.bankDetails.id = ?5",
                fromDate,
                toDate,
                BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE,
                BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE,
                bankDetails)
            .order("operationDate")
            .order("sequence")
            .fetch();
    if (ObjectUtils.notEmpty(bankStatementLineList) && bankStatementLineList.size() >= 2) {
      BankStatementLineAFB120 initalBankStatementLine = bankStatementLineList.get(0);
      BankStatementLineAFB120 finalBankStatementLine =
          bankStatementLineList.get(bankStatementLineList.size() - 1);
      if (exportType.equals("pdf")
          && ObjectUtils.notEmpty(initalBankStatementLine)
          && ObjectUtils.notEmpty(finalBankStatementLine)) {
        fileLink =
            ReportFactory.createReport(
                    IReport.BANK_STATEMENT_LINES,
                    "Bank statement lines - " + fromDate + " to " + toDate)
                .addParam("InitialLineId", initalBankStatementLine.getId())
                .addParam("FinalLineId", finalBankStatementLine.getId())
                .addParam("FromDate", Date.valueOf(fromDate))
                .addParam("ToDate", Date.valueOf(toDate))
                .addParam("BankDetails", bankDetails)
                .addParam("Locale", ReportSettings.getPrintingLocale(null))
                .addFormat(ReportSettings.FORMAT_PDF)
                .generate()
                .getFileLink();
      }
    }
    return fileLink;
  }
}
