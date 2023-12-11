package com.axelor.apps.bankpayment.service.bankstatement.afb120;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportAbstractService;
import com.axelor.apps.bankpayment.service.bankstatement.line.afb120.BankStatementLineCreateAFB120Service;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;

public class BankStatementImportAFB120Service extends BankStatementImportAbstractService {

  protected BankPaymentBankStatementLineAFB120Repository
      bankPaymentBankStatementLineAFB120Repository;
  protected BankStatementLineDeleteService bankStatementLineDeleteService;
  protected BankStatementLineFetchService bankStatementLineFetchService;
  protected BankStatementLineCreateAFB120Service bankStatementLineCreateAFB120Service;

  @Inject
  public BankStatementImportAFB120Service(
      BankStatementRepository bankStatementRepository,
      BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository,
      BankStatementLineDeleteService bankStatementLineDeleteService,
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementLineCreateAFB120Service bankStatementLineCreateAFB120Service) {
    super(bankStatementRepository);
    this.bankPaymentBankStatementLineAFB120Repository =
        bankPaymentBankStatementLineAFB120Repository;
    this.bankStatementLineDeleteService = bankStatementLineDeleteService;
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankStatementLineCreateAFB120Service = bankStatementLineCreateAFB120Service;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
    bankStatementLineCreateAFB120Service.process(bankStatement);

    // The process from bankStatementFileAFB120Service clears the JPA cache, so we need to find the
    // bank statement.
    bankStatement = bankStatementRepository.find(bankStatement.getId());
    checkImport(bankStatement);
    updateBankDetailsBalance(bankStatement);
    setBankStatementImported(bankStatement);
  }

  @Override
  protected void checkImport(BankStatement bankStatement) throws AxelorException {
    try {
      boolean alreadyImported = false;

      List<BankStatementLineAFB120> initialLines;
      List<BankStatementLineAFB120> finalLines;
      List<BankDetails> bankDetails =
          bankStatementLineFetchService.getBankDetailsFromStatementLines(bankStatement);
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
      bankStatementLineDeleteService.deleteBankStatementLines(
          bankStatementRepository.find(bankStatement.getId()));
      throw e;
    }
  }

  @Override
  @Transactional
  protected void updateBankDetailsBalance(BankStatement bankStatement) {
    List<BankDetails> bankDetailsList =
        bankStatementLineFetchService.getBankDetailsFromStatementLines(bankStatement);
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
  }

  protected void checkAmountWithPreviousBankStatement(
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

  protected void checkAmountWithinBankStatement(
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

  protected boolean bankStatementLineAlreadyExists(List<BankStatementLineAFB120> initialLines) {
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
}
