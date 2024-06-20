package com.axelor.apps.bankpayment.service.bankstatement.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineCAMT53;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineCAMT53Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineCAMT53Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementBankDetailsService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportAbstractService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportCheckService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.bankpayment.service.bankstatementline.camt53.BankStatementLineCreateCAMT53Service;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;

public class BankStatementImportCAMT53Service extends BankStatementImportAbstractService {

  protected CurrencyScaleService currencyScaleService;

  protected BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service;

  protected BankStatementLineFetchService bankStatementLineFetchService;

  protected BankStatementLineDeleteService bankStatementLineDeleteService;

  protected BankPaymentBankStatementLineCAMT53Repository
      bankPaymentBankStatementLineCAMT53Repository;

  @Inject
  public BankStatementImportCAMT53Service(
      BankStatementRepository bankStatementRepository,
      BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service,
      BankStatementLineFetchService bankStatementLineFetchService,
      BankPaymentBankStatementLineCAMT53Repository bankPaymentBankStatementLineCAMT53Repository,
      BankStatementLineDeleteService bankStatementLineDeleteService,
      BankStatementImportCheckService bankStatementImportCheckService,
      BankStatementBankDetailsService bankStatementBankDetailsService,
      BankStatementCreateService bankStatementCreateService,
      CurrencyScaleService currencyScaleService) {
    super(
        bankStatementRepository,
        bankStatementImportCheckService,
        bankStatementLineFetchService,
        bankStatementBankDetailsService,
        bankStatementCreateService);

    this.bankStatementLineCreateCAMT53Service = bankStatementLineCreateCAMT53Service;
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankPaymentBankStatementLineCAMT53Repository =
        bankPaymentBankStatementLineCAMT53Repository;
    this.bankStatementLineDeleteService = bankStatementLineDeleteService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  @Transactional
  public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
    bankStatement = bankStatementLineCreateCAMT53Service.processCAMT53(bankStatement);
    checkImport(bankStatement);
    updateBankDetailsBalance(bankStatement);
    computeBankStatementName(bankStatement);
    setBankStatementImported(bankStatement);
  }

  @Override
  protected void checkImport(BankStatement bankStatement) throws AxelorException {
    try {
      boolean alreadyImported = false;

      List<BankStatementLineCAMT53> initialLines;
      List<BankStatementLineCAMT53> finalLines;
      List<BankDetails> bankDetails =
          bankStatementLineFetchService.getBankDetailsFromStatementLines(bankStatement);
      // Load lines
      for (BankDetails bd : bankDetails) {
        initialLines =
            bankPaymentBankStatementLineCAMT53Repository
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineCAMT53Repository.LINE_TYPE_INITIAL_BALANCE)
                .fetch();

        finalLines =
            bankPaymentBankStatementLineCAMT53Repository
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineCAMT53Repository.LINE_TYPE_FINAL_BALANCE)
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
        BankStatementLineCAMT53 finalBankStatementLineCAMT53 =
            bankPaymentBankStatementLineCAMT53Repository
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement,
                    bankDetails,
                    BankStatementLineCAMT53Repository.LINE_TYPE_FINAL_BALANCE)
                .order("-operationDate")
                .order("-sequence")
                .fetchOne();
        bankDetails.setBalance(
            (finalBankStatementLineCAMT53
                .getCredit()
                .subtract(finalBankStatementLineCAMT53.getDebit())));
        bankDetails.setBalanceUpdatedDate(finalBankStatementLineCAMT53.getOperationDate());
      }
    }
  }

  protected boolean bankStatementLineAlreadyExists(List<BankStatementLineCAMT53> initialLines) {
    boolean alreadyImported = false;
    BankStatementLineCAMT53 tempBankStatementLineCAMT53;
    for (BankStatementLineCAMT53 bslCAMT53 : initialLines) {
      tempBankStatementLineCAMT53 =
          bankPaymentBankStatementLineCAMT53Repository
              .all()
              .filter(
                  "self.operationDate = :operationDate"
                      + " AND self.lineTypeSelect = :lineTypeSelect"
                      + " AND self.bankStatement != :bankStatement"
                      + " AND self.bankDetails = :bankDetails")
              .bind("operationDate", bslCAMT53.getOperationDate())
              .bind("lineTypeSelect", bslCAMT53.getLineTypeSelect())
              .bind("bankStatement", bslCAMT53.getBankStatement())
              .bind("bankDetails", bslCAMT53.getBankDetails())
              .fetchOne();
      if (ObjectUtils.notEmpty(tempBankStatementLineCAMT53)) {
        alreadyImported = true;
        break;
      }
    }
    return alreadyImported;
  }

  protected void checkAmountWithPreviousBankStatement(
      BankStatement bankStatement, List<BankDetails> bankDetails) throws AxelorException {
    boolean deleteLines = false;
    for (BankDetails bd : bankDetails) {
      BankStatementLineCAMT53 initialBankStatementLineCAMT53 =
          bankPaymentBankStatementLineCAMT53Repository
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineCAMT53Repository.LINE_TYPE_INITIAL_BALANCE)
              .order("operationDate")
              .order("sequence")
              .fetchOne();
      BankStatementLineCAMT53 finalBankStatementLineCAMT53 =
          bankPaymentBankStatementLineCAMT53Repository
              .findByBankDetailsLineTypeAndOperationDateExcludeBankStatement(
                  bankStatement,
                  bd,
                  BankStatementLineCAMT53Repository.LINE_TYPE_FINAL_BALANCE,
                  initialBankStatementLineCAMT53.getOperationDate())
              .order("-operationDate")
              .order("-sequence")
              .fetchOne();
      if (ObjectUtils.notEmpty(finalBankStatementLineCAMT53)
          && (currencyScaleService
                      .getScaledValue(
                          initialBankStatementLineCAMT53, initialBankStatementLineCAMT53.getDebit())
                      .compareTo(
                          currencyScaleService.getScaledValue(
                              finalBankStatementLineCAMT53,
                              finalBankStatementLineCAMT53.getDebit()))
                  != 0
              || currencyScaleService
                      .getScaledValue(
                          initialBankStatementLineCAMT53,
                          initialBankStatementLineCAMT53.getCredit())
                      .compareTo(
                          currencyScaleService.getScaledValue(
                              finalBankStatementLineCAMT53,
                              finalBankStatementLineCAMT53.getCredit()))
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
      List<BankStatementLineCAMT53> initialBankStatementLineCAMT53 =
          bankPaymentBankStatementLineCAMT53Repository
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineCAMT53Repository.LINE_TYPE_INITIAL_BALANCE)
              .order("sequence")
              .fetch();
      List<BankStatementLineCAMT53> finalBankStatementLineCAMT53 =
          bankPaymentBankStatementLineCAMT53Repository
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineCAMT53Repository.LINE_TYPE_FINAL_BALANCE)
              .order("sequence")
              .fetch();
      initialBankStatementLineCAMT53.remove(0);
      finalBankStatementLineCAMT53.remove(finalBankStatementLineCAMT53.size() - 1);
      if (initialBankStatementLineCAMT53.size() != finalBankStatementLineCAMT53.size()) {
        deleteLines = true;
        break;
      }
      if (!deleteLines) {
        for (int i = 0; i < initialBankStatementLineCAMT53.size(); i++) {
          deleteLines =
              deleteLines
                  || (currencyScaleService
                              .getScaledValue(
                                  initialBankStatementLineCAMT53.get(i),
                                  initialBankStatementLineCAMT53.get(i).getDebit())
                              .compareTo(
                                  currencyScaleService.getScaledValue(
                                      finalBankStatementLineCAMT53.get(i),
                                      finalBankStatementLineCAMT53.get(i).getDebit()))
                          != 0
                      || currencyScaleService
                              .getScaledValue(
                                  initialBankStatementLineCAMT53.get(i),
                                  initialBankStatementLineCAMT53.get(i).getCredit())
                              .compareTo(
                                  currencyScaleService.getScaledValue(
                                      finalBankStatementLineCAMT53.get(i),
                                      finalBankStatementLineCAMT53.get(i).getCredit()))
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
}
