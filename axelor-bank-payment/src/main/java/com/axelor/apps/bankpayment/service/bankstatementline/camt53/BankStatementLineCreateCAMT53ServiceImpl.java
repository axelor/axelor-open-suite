package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.BankToCustomerStatementV02;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.Document;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.util.List;
import java.util.Optional;

public class BankStatementLineCreateCAMT53ServiceImpl
    implements BankStatementLineCreateCAMT53Service {

  private int sequence = 0;
  protected BankStatement bankStatement;

  protected CAMT53ToolService camt53ToolService;
  protected BankStatementLineCreationCAMT53Service bankStatementLineCreationCAMT53Service;
  protected BankStatementRepository bankStatementRepository;
  protected BankDetailsRepository bankDetailsRepository;

  @Inject
  public BankStatementLineCreateCAMT53ServiceImpl(
      CAMT53ToolService camt53ToolService,
      BankStatementLineCreationCAMT53Service bankStatementLineCreationCAMT53Service,
      BankStatementRepository bankStatementRepository,
      BankDetailsRepository bankDetailsRepository) {
    this.camt53ToolService = camt53ToolService;
    this.bankStatementLineCreationCAMT53Service = bankStatementLineCreationCAMT53Service;
    this.bankStatementRepository = bankStatementRepository;
    this.bankDetailsRepository = bankDetailsRepository;
  }

  @Override
  public void processCAMT53(BankStatement bankStatement) throws AxelorException {
    this.bankStatement = bankStatement;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Document document =
          (Document)
              jaxbUnmarshaller.unmarshal(
                  MetaFiles.getPath(bankStatement.getBankStatementFile()).toFile());
      if (document == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_READ_ERROR));
      }
      BankToCustomerStatementV02 bkToCstmrStmt = document.getBkToCstmrStmt();
      if (bkToCstmrStmt == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(
                BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_NO_BANK_STATEMENT_FOUND_ERROR));
      }

      // May be multiple bank statements.
      List<AccountStatement2> stmtList = bkToCstmrStmt.getStmt();
      if (ObjectUtils.isEmpty(stmtList)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(
                BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_NO_BANK_STATEMENT_FOUND_ERROR));
      }

      // initial the counter
      sequence = 0;

      camt53ToolService.computeBankStatementDates(bankStatement, stmtList);

      for (int i = 0; i < stmtList.size(); i++) {
        // handle each AccountStatement2 object in list
        AccountStatement2 curBankStatement = stmtList.get(i);

        fillBankStatement(curBankStatement, bankStatement);
      }
    } catch (jakarta.xml.bind.JAXBException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_UNMARSHAL_ERROR));
    }
  }

  /**
   * This method takes the input AccountStatement2 object and generates related bankStatementLines
   * under the current BankStatement Object.
   *
   * @param stmt the input AccountStatement2 object from the file
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void fillBankStatement(AccountStatement2 stmt, BankStatement bankStatement)
      throws AxelorException {
    CashAccount20 acct = stmt.getAcct();
    BankDetails bankDetails = null;
    String currencyCodeFromStmt = null;
    if (acct != null) {
      bankDetails = camt53ToolService.findBankDetailsByIBAN(acct);

      if (bankDetails == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_BANK_DETAILS_NOT_EXIST_ERROR));
      }

      currencyCodeFromStmt = acct.getCcy();
      if (currencyCodeFromStmt == null) {
        currencyCodeFromStmt =
            Optional.of(bankDetails)
                .map(BankDetails::getCompany)
                .map(Company::getCurrency)
                .map(Currency::getCode)
                .orElse(null);
      }
    }

    sequence =
        createBalBalanceLine(
            stmt,
            bankDetails,
            sequence,
            currencyCodeFromStmt,
            BankStatementFileFormatRepository.CAMT_BALANCE_TYPE_INITIAL_BALANCE);

    sequence = createReportEntry2BalanceLine(stmt, bankDetails, sequence, currencyCodeFromStmt);

    sequence =
        createBalBalanceLine(
            stmt,
            bankDetails,
            sequence,
            currencyCodeFromStmt,
            BankStatementFileFormatRepository.CAMT_BALANCE_TYPE_FINAL_BALANCE);
  }

  protected int createBalBalanceLine(
      AccountStatement2 stmt,
      BankDetails bankDetails,
      int sequence,
      String currencyCodeFromStmt,
      String balanceType) {
    List<CashBalance3> balList = stmt.getBal();
    if (balList != null && !balList.isEmpty()) {
      for (CashBalance3 balanceEntry : balList) {
        sequence =
            bankStatementLineCreationCAMT53Service.createBalanceLine(
                bankStatement,
                bankDetails,
                balanceEntry,
                sequence,
                balanceType,
                currencyCodeFromStmt);
      }
    }

    return sequence;
  }

  protected int createReportEntry2BalanceLine(
      AccountStatement2 stmt, BankDetails bankDetails, int sequence, String currencyCodeFromStmt) {
    List<ReportEntry2> ntryList = stmt.getNtry();
    if (ntryList != null && !ntryList.isEmpty()) {
      for (ReportEntry2 ntry : ntryList) {
        sequence =
            bankStatementLineCreationCAMT53Service.createEntryLine(
                bankStatement, bankDetails, ntry, sequence, currencyCodeFromStmt);
      }
    }

    return sequence;
  }

  protected BankStatement findBankStatement() {
    bankStatement =
        JPA.em().contains(bankStatement)
            ? bankStatement
            : bankStatementRepository.find(bankStatement.getId());
    return bankStatement;
  }
}
