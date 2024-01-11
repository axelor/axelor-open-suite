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
package com.axelor.apps.bankpayment.service.bankstatement.file.afb120;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.mapper.Mapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BankStatementLineAFB120Service extends BankStatementLineService {

  @Inject
  public BankStatementLineAFB120Service(
      BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository) {
    super(bankPaymentBankStatementLineAFB120Repository);
  }

  public BankStatementLineAFB120 createBankStatementLine(
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
      String reference,
      int lineType,
      String unavailabilityIndexSelect,
      String commissionExemptionIndexSelect) {

    BankStatementLine bankStatementLine =
        super.createBankStatementLine(
            bankStatement,
            sequence,
            bankDetails,
            debit,
            credit,
            currency,
            description,
            operationDate,
            valueDate,
            operationInterbankCodeLine,
            rejectInterbankCodeLine,
            origin,
            reference);

    BankStatementLineAFB120 bankStatementLineAFB120 =
        Mapper.toBean(BankStatementLineAFB120.class, Mapper.toMap(bankStatementLine));

    bankStatementLineAFB120.setLineTypeSelect(lineType);

    if (lineType != BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT) {
      bankStatementLineAFB120.setAmountRemainToReconcile(BigDecimal.ZERO);
    }

    if (!Strings.isNullOrEmpty(unavailabilityIndexSelect)) {
      bankStatementLineAFB120.setUnavailabilityIndexSelect(
          Integer.parseInt(unavailabilityIndexSelect));
    }
    if (!Strings.isNullOrEmpty(commissionExemptionIndexSelect)) {
      bankStatementLineAFB120.setCommissionExemptionIndexSelect(
          Integer.parseInt(commissionExemptionIndexSelect));
    }

    return bankStatementLineAFB120;
  }

  public BankStatementLineAFB120 getFirstInitialLineBetweenDate(
      LocalDate fromDate, LocalDate toDate, BankDetails bankDetails) {
    return bankPaymentBankStatementLineAFB120Repository.findLineBetweenDate(
        fromDate,
        toDate,
        BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE,
        true,
        bankDetails);
  }

  public BankStatementLineAFB120 getLastFinalLineBetweenDate(
      LocalDate fromDate, LocalDate toDate, BankDetails bankDetails) {
    return bankPaymentBankStatementLineAFB120Repository.findLineBetweenDate(
        fromDate,
        toDate,
        BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE,
        false,
        bankDetails);
  }

  public List<BankStatementLineAFB120> getMovementLinesBetweenDates(
      LocalDate fromDate, LocalDate toDate, BankDetails bankDetails) {

    return bankPaymentBankStatementLineAFB120Repository.findLinesBetweenDate(
        fromDate, toDate, BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT, bankDetails);
  }

  public String print(
      BankStatementLineAFB120 initialLine,
      BankStatementLineAFB120 finalLine,
      LocalDate fromDate,
      LocalDate toDate,
      BankDetails bankDetails,
      String extention)
      throws AxelorException {
    String reportName = IReport.BANK_STATEMENT_LINES_AFB120;
    return ReportFactory.createReport(
            reportName, initialLine.getBankStatement().getName() + "-${date}")
        .addParam("InitialLineId", initialLine.getId())
        .addParam("FinalLineId", finalLine.getId())
        .addParam("FromDate", fromDate)
        .addParam("ToDate", toDate)
        .addParam("BankDetails", bankDetails)
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam("Timezone", getTimezone(initialLine))
        .addFormat(extention)
        .generate()
        .getFileLink();
  }

  protected String getTimezone(BankStatementLineAFB120 bankStatementLine) {
    BankStatement bankStatement = bankStatementLine.getBankStatement();
    if (bankStatement.getEbicsPartner() == null
        || bankStatement.getEbicsPartner().getDefaultSignatoryEbicsUser() == null
        || bankStatement.getEbicsPartner().getDefaultSignatoryEbicsUser().getAssociatedUser()
            == null
        || bankStatement
                .getEbicsPartner()
                .getDefaultSignatoryEbicsUser()
                .getAssociatedUser()
                .getActiveCompany()
            == null) {
      return null;
    }
    return bankStatement
        .getEbicsPartner()
        .getDefaultSignatoryEbicsUser()
        .getAssociatedUser()
        .getActiveCompany()
        .getTimezone();
  }

  public BankStatementLineAFB120 getLastBankStatementLineAFB120FromBankDetails(
      BankDetails bankDetails) {
    if (bankDetails != null) {
      String predicate =
          "self.bankDetails is not null AND self.bankDetails.id = "
              + bankDetails.getId()
              + " AND self.lineTypeSelect = "
              + BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE;
      Optional<BankStatementLineAFB120> id =
          bankPaymentBankStatementLineAFB120Repository
              .all()
              .filter(predicate)
              .fetchStream()
              .sorted(Comparator.comparing(BankStatementLineAFB120::getOperationDate))
              .findFirst();
      return id.isPresent()
          ? bankPaymentBankStatementLineAFB120Repository.find(id.get().getId())
          : null;
    }
    return null;
  }
}
