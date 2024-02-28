package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;

public class BankStatementImportCheckServiceImpl implements BankStatementImportCheckService {
  protected BankStatementLineFetchService bankStatementLineFetchService;
  protected BankStatementLineDeleteService bankStatementLineDeleteService;
  protected BankStatementRepository bankStatementRepository;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;

  @Inject
  public BankStatementImportCheckServiceImpl(
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementLineDeleteService bankStatementLineDeleteService,
      BankStatementRepository bankStatementRepository,
      BankStatementLineRepository bankStatementLineRepository,
      CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment) {
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankStatementLineDeleteService = bankStatementLineDeleteService;
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.currencyScaleServiceBankPayment = currencyScaleServiceBankPayment;
  }

  @Override
  public void checkImport(BankStatement bankStatement) throws AxelorException {
    try {
      boolean alreadyImported = false;

      List<BankStatementLine> initialLines;
      List<BankStatementLine> finalLines;
      List<BankDetails> bankDetails =
          bankStatementLineFetchService.getBankDetailsFromStatementLines(bankStatement);
      // Load lines
      for (BankDetails bd : bankDetails) {
        initialLines =
            bankStatementLineFetchService
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE)
                .fetch();

        finalLines =
            bankStatementLineFetchService
                .findByBankStatementBankDetailsAndLineType(
                    bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
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

  protected void checkAmountWithPreviousBankStatement(
      BankStatement bankStatement, List<BankDetails> bankDetails) throws AxelorException {
    boolean deleteLines = false;
    for (BankDetails bd : bankDetails) {
      BankStatementLine initialBankStatementLine =
          bankStatementLineFetchService
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE)
              .order("operationDate")
              .order("sequence")
              .fetchOne();
      BankStatementLine finalBankStatementLine =
          bankStatementLineFetchService
              .findByBankDetailsLineTypeExcludeBankStatement(
                  bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
              .order("-operationDate")
              .order("-sequence")
              .fetchOne();
      if (ObjectUtils.notEmpty(finalBankStatementLine)
          && (currencyScaleServiceBankPayment
                      .getScaledValue(initialBankStatementLine, initialBankStatementLine.getDebit())
                      .compareTo(
                          currencyScaleServiceBankPayment.getScaledValue(
                              finalBankStatementLine, finalBankStatementLine.getDebit()))
                  != 0
              || currencyScaleServiceBankPayment
                      .getScaledValue(
                          initialBankStatementLine, initialBankStatementLine.getCredit())
                      .compareTo(
                          currencyScaleServiceBankPayment.getScaledValue(
                              finalBankStatementLine, finalBankStatementLine.getCredit()))
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
      List<BankStatementLine> initialBankStatementLine =
          bankStatementLineFetchService
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE)
              .order("sequence")
              .fetch();
      List<BankStatementLine> finalBankStatementLine =
          bankStatementLineFetchService
              .findByBankStatementBankDetailsAndLineType(
                  bankStatement, bd, BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
              .order("sequence")
              .fetch();
      initialBankStatementLine.remove(0);
      finalBankStatementLine.remove(finalBankStatementLine.size() - 1);
      if (initialBankStatementLine.size() != finalBankStatementLine.size()) {
        deleteLines = true;
        break;
      }
      if (!deleteLines) {
        for (int i = 0; i < initialBankStatementLine.size(); i++) {
          deleteLines =
              deleteLines
                  || (currencyScaleServiceBankPayment
                              .getScaledValue(
                                  initialBankStatementLine.get(i),
                                  initialBankStatementLine.get(i).getDebit())
                              .compareTo(
                                  currencyScaleServiceBankPayment.getScaledValue(
                                      finalBankStatementLine.get(i),
                                      finalBankStatementLine.get(i).getDebit()))
                          != 0
                      || currencyScaleServiceBankPayment
                              .getScaledValue(
                                  initialBankStatementLine.get(i),
                                  initialBankStatementLine.get(i).getCredit())
                              .compareTo(
                                  currencyScaleServiceBankPayment.getScaledValue(
                                      finalBankStatementLine.get(i),
                                      finalBankStatementLine.get(i).getCredit()))
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

  protected boolean bankStatementLineAlreadyExists(List<BankStatementLine> initialLines) {
    boolean alreadyImported = false;
    BankStatementLine tempBankStatementLine;
    for (BankStatementLine bsl : initialLines) {
      tempBankStatementLine =
          bankStatementLineRepository
              .all()
              .filter(
                  "self.operationDate = :operationDate"
                      + " AND self.lineTypeSelect = :lineTypeSelect"
                      + " AND self.bankStatement != :bankStatement"
                      + " AND self.bankDetails = :bankDetails")
              .bind("operationDate", bsl.getOperationDate())
              .bind("lineTypeSelect", bsl.getLineTypeSelect())
              .bind("bankStatement", bsl.getBankStatement())
              .bind("bankDetails", bsl.getBankDetails())
              .fetchOne();
      if (ObjectUtils.notEmpty(tempBankStatementLine)) {
        alreadyImported = true;
        break;
      }
    }
    return alreadyImported;
  }
}
