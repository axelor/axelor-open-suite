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
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.bankpayment.service.bankstatement.file.afb120.BankStatementFileAFB120Service;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class BankStatementService {

  protected BankStatementRepository bankStatementRepository;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;

  @Inject
  public BankStatementService(
      BankStatementRepository bankStatementRepository,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository) {
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
  }

  public void runImport(BankStatement bankStatement, boolean alertIfFormatNotSupported)
      throws IOException, AxelorException {

    bankStatement = find(bankStatement);

    if (bankStatement.getBankStatementFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_STATEMENT_MISSING_FILE));
    }

    if (bankStatement.getBankStatementFileFormat() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_STATEMENT_MISSING_FILE_FORMAT));
    }

    BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

    switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        Beans.get(BankStatementFileAFB120Service.class).process(bankStatement);
        updateStatus(bankStatement);
        break;

      default:
        if (alertIfFormatNotSupported) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.BANK_STATEMENT_FILE_UNKNOWN_FORMAT));
        }
    }
  }

  @Transactional
  public void updateStatus(BankStatement bankStatement) {
    bankStatement = find(bankStatement);
    bankStatement.setStatusSelect(BankStatementRepository.STATUS_IMPORTED);
    bankStatementRepository.save(bankStatement);
  }

  /**
   * Print bank statement.
   *
   * @param bankStatement
   * @return
   * @throws AxelorException
   */
  public String print(BankStatement bankStatement) throws AxelorException {
    String reportName;

    switch (bankStatement.getBankStatementFileFormat().getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        reportName = IReport.BANK_STATEMENT_AFB120;
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.BANK_STATEMENT_FILE_UNKNOWN_FORMAT));
    }

    return ReportFactory.createReport(reportName, bankStatement.getName() + "-${date}")
        .addParam("BankStatementId", bankStatement.getId())
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam("Timezone", getTimezone(bankStatement))
        .addFormat("pdf")
        .toAttach(bankStatement)
        .generate()
        .getFileLink();
  }

  private String getTimezone(BankStatement bankStatement) {
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

  /**
   * Finds bank statement.
   *
   * @param bankStatement
   * @return
   */
  public BankStatement find(BankStatement bankStatement) {
    return JPA.em().contains(bankStatement)
        ? bankStatement
        : bankStatementRepository.find(bankStatement.getId());
  }

  @Transactional
  public void deleteBankStatementLines(BankStatement bankStatement) {
    List<BankStatementLineAFB120> bankStatementLines;
    bankStatementLines =
        bankStatementLineAFB120Repository
            .all()
            .filter("self.bankStatement = :bankStatement")
            .bind("bankStatement", bankStatement)
            .fetch();
    for (BankStatementLineAFB120 bsl : bankStatementLines) {
      bankStatementLineAFB120Repository.remove(bsl);
    }
    bankStatement.setStatusSelect(BankStatementRepository.STATUS_RECEIVED);
  }

  @Transactional
  public BankStatement setIsFullyReconciled(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines =
        Beans.get(BankStatementLineRepository.class).findByBankStatement(bankStatement).fetch();
    BigDecimal amountToReconcile = BigDecimal.ZERO;
    for (BankStatementLine bankStatementLine : bankStatementLines) {
      amountToReconcile = amountToReconcile.add(bankStatementLine.getAmountRemainToReconcile());
    }
    if (amountToReconcile.compareTo(BigDecimal.ZERO) == 0) {
      bankStatement.setIsFullyReconciled(true);
    }

    return bankStatementRepository.save(bankStatement);
  }
}
