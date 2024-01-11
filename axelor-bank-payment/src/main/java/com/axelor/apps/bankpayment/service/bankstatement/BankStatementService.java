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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.bankpayment.service.bankstatement.file.afb120.BankStatementFileAFB120Service;
import com.axelor.apps.bankpayment.service.bankstatement.file.afb120.BankStatementLineAFB120Service;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;

public class BankStatementService {

  protected BankStatementRepository bankStatementRepository;
  protected BankPaymentBankStatementLineAFB120Repository
      bankPaymentBankStatementLineAFB120Repository;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected BankStatementLineAFB120Service bankStatementLineAFB120Service;
  protected BankDetailsRepository bankDetailsRepository;

  @Inject
  public BankStatementService(
      BankStatementRepository bankStatementRepository,
      BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository,
      BankStatementLineRepository bankStatementLineRepository,
      BankStatementLineAFB120Service bankStatementLineAFB120Service,
      BankDetailsRepository bankDetailsRepository) {
    this.bankStatementRepository = bankStatementRepository;
    this.bankPaymentBankStatementLineAFB120Repository =
        bankPaymentBankStatementLineAFB120Repository;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.bankStatementLineAFB120Service = bankStatementLineAFB120Service;
    this.bankDetailsRepository = bankDetailsRepository;
  }

  public void runImport(BankStatement bankStatement, boolean alertIfFormatNotSupported)
      throws IOException, AxelorException {

    bankStatement = find(bankStatement);

    if (bankStatement.getBankStatementFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_MISSING_FILE));
    }

    if (bankStatement.getBankStatementFileFormat() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_MISSING_FILE_FORMAT));
    }

    BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

    switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        Beans.get(BankStatementFileAFB120Service.class).process(bankStatement);
        this.checkImport(bankStatement);
        updateStatus(bankStatement);
        break;

      default:
        if (alertIfFormatNotSupported) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_FILE_UNKNOWN_FORMAT));
        }
    }
  }

  @Transactional
  public void updateStatus(BankStatement bankStatement) {
    bankStatement = find(bankStatement);
    List<BankDetails> bankDetailsList = fetchBankDetailsList(bankStatement);
    if (!ObjectUtils.isEmpty(bankDetailsList)) {
      for (BankDetails bankDetails : bankDetailsList) {
        BankStatementLineAFB120 finalBankStatementLineAFB120 =
            bankPaymentBankStatementLineAFB120Repository
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement,
                    bankDetails,
                    BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
                .order("-operationDate")
                .order("-sequence")
                .fetchOne();
        bankDetails.setBalance(
            (finalBankStatementLineAFB120
                .getCredit()
                .subtract(finalBankStatementLineAFB120.getDebit())));
        bankDetails.setBalanceUpdatedDate(finalBankStatementLineAFB120.getOperationDate());
      }
    }

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
            I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_FILE_UNKNOWN_FORMAT));
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

  protected String getTimezone(BankStatement bankStatement) {
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
        bankPaymentBankStatementLineAFB120Repository
            .all()
            .filter("self.bankStatement = :bankStatement")
            .bind("bankStatement", bankStatement)
            .fetch();
    for (BankStatementLineAFB120 bsl : bankStatementLines) {
      bankPaymentBankStatementLineAFB120Repository.remove(bsl);
    }
    bankStatement.setStatusSelect(BankStatementRepository.STATUS_RECEIVED);
  }

  @Transactional
  public BankStatement setIsFullyReconciled(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines =
        bankStatementLineRepository.findByBankStatement(bankStatement).fetch();
    BigDecimal amountToReconcile = BigDecimal.ZERO;
    for (BankStatementLine bankStatementLine : bankStatementLines) {
      amountToReconcile = amountToReconcile.add(bankStatementLine.getAmountRemainToReconcile());
    }
    if (amountToReconcile.compareTo(BigDecimal.ZERO) == 0) {
      bankStatement.setIsFullyReconciled(true);
    }

    return bankStatementRepository.save(bankStatement);
  }

  public boolean bankStatementLineAlreadyExists(List<BankStatementLineAFB120> initialLines) {
    boolean alreadyImported = false;
    BankStatementLineAFB120 tempBankStatementLineAFB120;
    for (BankStatementLineAFB120 bslAFB120 : initialLines) {
      tempBankStatementLineAFB120 =
          bankPaymentBankStatementLineAFB120Repository
              .all()
              .filter(
                  "self.operationDate = :operationDate"
                      + " AND self.lineTypeSelect = :lineTypeSelect"
                      + " AND self.bankStatement != :bankStatement"
                      + " AND self.bankDetails = :bankDetails")
              .bind("operationDate", bslAFB120.getOperationDate())
              .bind("lineTypeSelect", bslAFB120.getLineTypeSelect())
              .bind("bankStatement", bslAFB120.getBankStatement())
              .bind("bankDetails", bslAFB120.getBankDetails())
              .fetchOne();
      if (ObjectUtils.notEmpty(tempBankStatementLineAFB120)) {
        alreadyImported = true;
        break;
      }
    }
    return alreadyImported;
  }

  public List<BankDetails> fetchBankDetailsList(BankStatement bankStatement) {
    List<BankDetails> bankDetails;
    String query =
        "select distinct bankDetails from BankStatementLineAFB120 as self"
            + " where self.bankStatement = ?1";
    Query q = JPA.em().createQuery(query, BankDetails.class);
    q.setParameter(1, bankStatement);
    bankDetails = (List<BankDetails>) q.getResultList();

    return bankDetails;
  }

  public void checkAmountWithPreviousBankStatement(
      BankStatement bankStatement, List<BankDetails> bankDetails) throws AxelorException {
    boolean deleteLines = false;
    for (BankDetails bd : bankDetails) {
      BankStatementLineAFB120 initialBankStatementLineAFB120 =
          bankPaymentBankStatementLineAFB120Repository
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
              .order("operationDate")
              .order("sequence")
              .fetchOne();
      BankStatementLineAFB120 finalBankStatementLineAFB120 =
          bankPaymentBankStatementLineAFB120Repository
              .findByBankDetailsLineTypeExcludeBankStatement(
                  bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
              .order("-operationDate")
              .order("-sequence")
              .fetchOne();
      if (ObjectUtils.notEmpty(finalBankStatementLineAFB120)
          && (initialBankStatementLineAFB120
                      .getDebit()
                      .compareTo(finalBankStatementLineAFB120.getDebit())
                  != 0
              || initialBankStatementLineAFB120
                      .getCredit()
                      .compareTo(finalBankStatementLineAFB120.getCredit())
                  != 0)) {
        deleteLines = true;
      }
    }
    // delete imported
    if (deleteLines) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_NOT_MATCHING));
    }
  }

  public void checkAmountWithinBankStatement(
      BankStatement bankStatement, List<BankDetails> bankDetails) throws AxelorException {
    boolean deleteLines = false;
    for (BankDetails bd : bankDetails) {
      List<BankStatementLineAFB120> initialBankStatementLineAFB120 =
          bankPaymentBankStatementLineAFB120Repository
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
              .order("sequence")
              .fetch();
      List<BankStatementLineAFB120> finalBankStatementLineAFB120 =
          bankPaymentBankStatementLineAFB120Repository
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
              .order("sequence")
              .fetch();
      initialBankStatementLineAFB120.remove(0);
      finalBankStatementLineAFB120.remove(finalBankStatementLineAFB120.size() - 1);
      if (initialBankStatementLineAFB120.size() != finalBankStatementLineAFB120.size()) {
        deleteLines = true;
        break;
      }
      if (!deleteLines) {
        for (int i = 0; i < initialBankStatementLineAFB120.size(); i++) {
          deleteLines =
              deleteLines
                  || (initialBankStatementLineAFB120
                              .get(i)
                              .getDebit()
                              .compareTo(finalBankStatementLineAFB120.get(i).getDebit())
                          != 0
                      || initialBankStatementLineAFB120
                              .get(i)
                              .getCredit()
                              .compareTo(finalBankStatementLineAFB120.get(i).getCredit())
                          != 0);
          if (deleteLines) {
            break;
          }
        }
      }
      if (deleteLines) {
        break;
      }
    }
    // delete imported
    if (deleteLines) {
      throw new AxelorException(
          bankStatement,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_INCOHERENT_BALANCE));
    }
  }

  public void checkImport(BankStatement bankStatement) throws AxelorException {
    try {
      boolean alreadyImported = false;

      List<BankStatementLineAFB120> initialLines;
      List<BankStatementLineAFB120> finalLines;
      List<BankDetails> bankDetails = fetchBankDetailsList(bankStatement);
      // Load lines
      for (BankDetails bd : bankDetails) {
        initialLines =
            bankPaymentBankStatementLineAFB120Repository
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
                .fetch();

        finalLines =
            bankPaymentBankStatementLineAFB120Repository
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
                .fetch();

        alreadyImported =
            bankStatementLineAlreadyExists(initialLines)
                || bankStatementLineAlreadyExists(finalLines)
                || alreadyImported;
      }
      if (!alreadyImported) {
        checkAmountWithPreviousBankStatement(bankStatement, bankDetails);
        checkAmountWithinBankStatement(bankStatement, bankDetails);
      } else {
        throw new AxelorException(
            bankStatement,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_ALREADY_IMPORTED));
      }

    } catch (Exception e) {
      deleteBankStatementLines(bankStatementRepository.find(bankStatement.getId()));
      throw e;
    }
  }

  @Transactional
  public void updateBankDetailsBalanceAndDate(List<BankDetails> bankDetails) {
    if (CollectionUtils.isEmpty(bankDetails)) {
      return;
    }
    BankStatementLineAFB120 lastLine;
    for (BankDetails bankDetail : bankDetails) {
      lastLine =
          bankStatementLineAFB120Service.getLastBankStatementLineAFB120FromBankDetails(bankDetail);
      if (lastLine != null) {
        bankDetail.setBalance(
            lastLine.getDebit().compareTo(BigDecimal.ZERO) > 0
                ? lastLine.getDebit()
                : lastLine.getCredit());
        bankDetail.setBalanceUpdatedDate(lastLine.getOperationDate());
      } else {
        bankDetail.setBalance(BigDecimal.ZERO);
        bankDetail.setBalanceUpdatedDate(null);
      }
      bankDetailsRepository.save(bankDetail);
    }
  }

  public List<BankStatementLineAFB120> getBankStatementLines(BankStatement bankStatement) {
    List<BankStatementLineAFB120> bankStatementLines;
    bankStatementLines =
        bankPaymentBankStatementLineAFB120Repository
            .all()
            .filter("self.bankStatement = :bankStatement")
            .bind("bankStatement", bankStatement)
            .fetch();
    return bankStatementLines;
  }
}
