package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineCAMT53;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineCAMT53Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreateAbstractService;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.StructuredContentLine;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountIdentification4Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType12;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType12Code;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType5Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BankToCustomerStatementV02;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BankTransactionCodeStructure4;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BankTransactionCodeStructure5;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BatchInformation2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CreditDebitCode;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CreditorReferenceInformation2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateAndDateTimeChoice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateTimePeriodDetails;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.Document;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryDetails1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryTransaction2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.GenericAccountIdentification1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.Party6Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.PartyIdentification32;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ProprietaryBankTransactionCodeStructure1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.RemittanceInformation5;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReportEntry2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.StructuredRemittanceInformation7;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.TransactionParty2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.TransactionReferences2;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.persistence.Query;
import javax.xml.datatype.XMLGregorianCalendar;

public class BankStatementLineCreateCAMT53Service extends BankStatementLineCreateAbstractService {
  public static String CREDIT_DEBIT_INDICATOR_CREDIT = "CRDT";
  public static String CREDIT_DEBIT_INDICATOR_DEBIT = "DBIT";
  public static String BALANCE_TYPE_INITIAL_BALANCE = "OPBD";
  public static String BALANCE_TYPE_FINAL_BALANCE = "CLBD";
  protected BankStatementLineCAMT53Repository bankStatementLineCAMT53Repository;
  protected InterbankCodeLineRepository interbankCodeLineRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected BankStatementLineCreationCAMT53Service bankStatementLineCreationCAMT53Service;
  private int differentCurrencyOccurrence = 0;
  private int sequence = 0;

  @Inject
  protected BankStatementLineCreateCAMT53Service(
      BankStatementRepository bankStatementRepository,
      BankStatementImportService bankStatementService,
      BankStatementLineCAMT53Repository bankStatementLineCAMT53Repository,
      InterbankCodeLineRepository interbankCodeLineRepository,
      BankDetailsRepository bankDetailsRepository,
      BankStatementLineCreationCAMT53Service bankStatementLineCreationCAMT53Service) {
    super(bankStatementRepository, bankStatementService);
    this.bankStatementLineCAMT53Repository = bankStatementLineCAMT53Repository;
    this.interbankCodeLineRepository = interbankCodeLineRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.bankStatementLineCreationCAMT53Service = bankStatementLineCreationCAMT53Service;
  }

  @Override
  protected List<StructuredContentLine> readFile() throws IOException, AxelorException {
    return null;
  }

  @Override
  protected BankStatementLine createBankStatementLine(
      StructuredContentLine structuredContentLine, int sequence) {
    return null;
  }

  public BankStatement processCAMT53(BankStatement bankStatement) throws AxelorException {
    setBankStatement(bankStatement);
    return processCAMT53();
  }

  public BankStatement processCAMT53() throws AxelorException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Document document = (Document) jaxbUnmarshaller.unmarshal(file);
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
      if (stmtList == null || stmtList.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(
                BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_NO_BANK_STATEMENT_FOUND_ERROR));
      }

      // initial the counter
      sequence = 0;
      differentCurrencyOccurrence = 0;

      for (int i = 0; i < stmtList.size(); i++) {
        // handle each AccountStatement2 object in list
        AccountStatement2 curBankStatement = stmtList.get(i);
        if (i == 0) {
          // the first statement, set fromDate
          setBankStatementFromDate(curBankStatement.getFrToDt());
        }
        if (i == stmtList.size() - 1) {
          // the last statement, set toDate
          setBankStatementToDate(curBankStatement.getFrToDt());
        }

        createBankStatement(curBankStatement);
      }
      return bankStatement;

      /* TODO: warn users when the currency symbol is different from the company's setting.
       */

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
  @Transactional
  protected void createBankStatement(AccountStatement2 stmt) throws AxelorException {
    CashAccount20 acct = stmt.getAcct();
    BankDetails bankDetails = null;
    String currencyCodeFromStmt = null;
    if (acct != null) {
      String ibanOrOthers = getIBANOrOtherAccountIdentification(acct);
      // find bankDetails
      bankDetails = findBankDetailsByIBAN(ibanOrOthers);

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

    List<CashBalance3> balList = stmt.getBal();
    if (balList != null && !balList.isEmpty()) {
      for (CashBalance3 balanceEntry : balList) {
        sequence =
            createBalanceLine(
                bankDetails,
                balanceEntry,
                sequence,
                BALANCE_TYPE_INITIAL_BALANCE,
                currencyCodeFromStmt);
        if (sequence % 10 == 0) {
          JPA.clear();
          findBankStatement();
        }
      }
    }

    List<ReportEntry2> ntryList = stmt.getNtry();
    if (ntryList != null && !ntryList.isEmpty()) {
      for (ReportEntry2 ntry : ntryList) {
        sequence = createEntryLine(bankDetails, ntry, sequence, currencyCodeFromStmt);
        if (sequence % 10 == 0) {
          JPA.clear();
          findBankStatement();
        }
      }
    }

    if (balList != null && !balList.isEmpty()) {
      for (CashBalance3 balanceEntry : balList) {
        sequence =
            createBalanceLine(
                bankDetails,
                balanceEntry,
                sequence,
                BALANCE_TYPE_FINAL_BALANCE,
                currencyCodeFromStmt);
        if (sequence % 10 == 0) {
          JPA.clear();
          findBankStatement();
        }
      }
    }
  }

  protected String getIBANOrOtherAccountIdentification(CashAccount20 acct) {
    String ibanOrOthers =
        Optional.of(acct)
            .map(CashAccount20::getId)
            .map(AccountIdentification4Choice::getIBAN)
            .orElse(null);
    if (ibanOrOthers == null) {
      // others
      ibanOrOthers =
          Optional.of(acct)
              .map(CashAccount20::getId)
              .map(AccountIdentification4Choice::getOthr)
              .map(GenericAccountIdentification1::getId)
              .orElse(null);
    }
    return ibanOrOthers;
  }

  @Transactional
  protected int createEntryLine(
      BankDetails bankDetails, ReportEntry2 ntry, int sequence, String currencyCodeFromStmt) {
    LocalDate operationDate = getOperationDateFromEntry(ntry);

    LocalDate valueDate = getValDate(ntry);

    String description = addNtryInfoIntoDescription(ntry);

    String currencyCode = getCurrencyCode(ntry);
    Currency currency = findCurrencyByCode(currencyCode);

    String creditDebitIndicator = getCreditDebitIndicatorFromEntry(ntry);
    BigDecimal credit = BigDecimal.ZERO;
    BigDecimal debit = BigDecimal.ZERO;
    if (CREDIT_DEBIT_INDICATOR_CREDIT.equals(creditDebitIndicator)) {
      credit = getCreditOrDebitValue(ntry);
      if (currencyCodeFromStmt != null && !currencyCodeFromStmt.equals(currencyCode)) {
        StringBuilder descriptionSB = new StringBuilder();
        descriptionSB.append("Entry.ccy=");
        descriptionSB.append(credit);
        descriptionSB.append(":");
        descriptionSB.append(currencyCode);
        descriptionSB.append(";");
        description += descriptionSB.toString();
        differentCurrencyOccurrence++;
      }

    } else if (CREDIT_DEBIT_INDICATOR_DEBIT.equals(creditDebitIndicator)) {
      debit = getCreditOrDebitValue(ntry);
      if (currencyCodeFromStmt != null && !currencyCodeFromStmt.equals(currencyCode)) {
        StringBuilder descriptionSB = new StringBuilder();
        descriptionSB.append("Entry.ccy=");
        descriptionSB.append(debit);
        descriptionSB.append(":");
        descriptionSB.append(currencyCode);
        descriptionSB.append(";");
        description += descriptionSB.toString();
        differentCurrencyOccurrence++;
      }
    }

    String origin = getOrigin(ntry);

    String reference = getReference(ntry);

    String interBankCodeLineCode = getInterBankCodeLineCode(ntry);
    InterbankCodeLine interbankCodeLine = findInterBankCodeLineByCode(interBankCodeLineCode);
    InterbankCodeLine operationInterbankCodeLine = null;
    InterbankCodeLine rejectInterbankCodeLine = null;
    if (interbankCodeLine != null) {
      if (interbankCodeLine.getInterbankCode().getTypeSelect()
          == BankStatementLineCAMT53Repository.TYPE_OPERATION_CODE) {
        operationInterbankCodeLine = interbankCodeLine;
      } else {
        rejectInterbankCodeLine = interbankCodeLine;
      }
    }

    if (bankDetails != null) {
      bankDetails = bankDetailsRepository.find(bankDetails.getId());
    }

    if (description != null
        && !description.isEmpty()
        && description.charAt(description.length() - 1) == ';') {
      description = description.substring(0, description.length() - 1);
    }

    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        bankStatementLineCreationCAMT53Service.createBankStatementLine(
            findBankStatement(),
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
            reference,
            BankStatementLineCAMT53Repository.LINE_TYPE_MOVEMENT);

    bankStatementLineCAMT53Repository.save(bankStatementLineCAMT53);
    return ++sequence;
  }

  protected LocalDate getOperationDateFromEntry(ReportEntry2 ntry) {
    XMLGregorianCalendar opDate =
        Optional.of(ntry)
            .map(ReportEntry2::getBookgDt)
            .map(DateAndDateTimeChoice::getDtTm)
            .orElse(null);
    // sometimes the DtTm is null, then check Dt.
    if (opDate == null) {
      opDate =
          Optional.of(ntry)
              .map(ReportEntry2::getBookgDt)
              .map(DateAndDateTimeChoice::getDt)
              .orElse(null);
    }
    LocalDate operationDate = null;
    if (opDate != null) {
      operationDate = LocalDate.of(opDate.getYear(), opDate.getMonth(), opDate.getDay());
    }
    return operationDate;
  }

  protected LocalDate getValDate(ReportEntry2 ntry) {
    XMLGregorianCalendar valDate =
        Optional.of(ntry)
            .map(ReportEntry2::getValDt)
            .map(DateAndDateTimeChoice::getDtTm)
            .orElse(null);
    // sometimes the DtTm is null, then check Dt.
    if (valDate == null) {
      valDate =
          Optional.of(ntry)
              .map(ReportEntry2::getValDt)
              .map(DateAndDateTimeChoice::getDt)
              .orElse(null);
    }
    LocalDate valueDate = null;
    if (valDate != null) {
      valueDate = LocalDate.of(valDate.getYear(), valDate.getMonth(), valDate.getDay());
    }
    return valueDate;
  }

  protected String getCurrencyCode(ReportEntry2 ntry) {
    return Optional.of(ntry)
        .map(ReportEntry2::getAmt)
        .map(ActiveOrHistoricCurrencyAndAmount::getCcy)
        .orElse(null);
  }

  protected String getCreditDebitIndicatorFromEntry(ReportEntry2 ntry) {
    return Optional.of(ntry)
        .map(ReportEntry2::getCdtDbtInd)
        .map(CreditDebitCode::value)
        .orElse(null);
  }

  protected BigDecimal getCreditOrDebitValue(ReportEntry2 ntry) {
    return Optional.of(ntry)
        .map(ReportEntry2::getAmt)
        .map(ActiveOrHistoricCurrencyAndAmount::getValue)
        .orElse(null);
  }

  protected String getOrigin(ReportEntry2 ntry) {
    String origin =
        Optional.ofNullable(ntry)
            .map(ReportEntry2::getNtryDtls)
            .flatMap(
                ntryDtls ->
                    ntryDtls.stream()
                        .findFirst()) // Convert to Stream and get first element if present
            .map(EntryDetails1::getBtch)
            .map(BatchInformation2::getPmtInfId)
            .orElse(null);
    if (origin == null) {
      origin =
          Optional.ofNullable(ntry)
              .map(ReportEntry2::getNtryDtls)
              .flatMap(
                  ntryDtls ->
                      ntryDtls.stream()
                          .findFirst()) // Convert to Stream and get first element if present
              .map(EntryDetails1::getTxDtls)
              .flatMap(txDetls -> txDetls.stream().findFirst())
              .map(EntryTransaction2::getRefs)
              .map(TransactionReferences2::getPmtInfId)
              .orElse(null);
    }
    return origin;
  }

  protected String getReference(ReportEntry2 ntry) {
    return Optional.ofNullable(ntry)
        .map(ReportEntry2::getNtryDtls)
        .flatMap(ntryDtls -> ntryDtls.stream().findFirst())
        .map(EntryDetails1::getTxDtls)
        .flatMap(txDtls -> txDtls.stream().findFirst())
        .map(EntryTransaction2::getRefs)
        .map(TransactionReferences2::getEndToEndId)
        .orElse(null);
  }

  protected String getInterBankCodeLineCode(ReportEntry2 ntry) {
    String interBankCodeLineCode = null;
    BankTransactionCodeStructure4 bkTxCd = ntry.getBkTxCd();
    BankTransactionCodeStructure5 domn = bkTxCd.getDomn();
    if (domn != null) {
      interBankCodeLineCode = domn.getCd();
    } else {
      ProprietaryBankTransactionCodeStructure1 prtry = bkTxCd.getPrtry();
      if (prtry != null) {
        interBankCodeLineCode = prtry.getCd();
      }
    }
    return interBankCodeLineCode;
  }

  @Transactional
  protected int createBalanceLine(
      BankDetails bankDetails,
      CashBalance3 balanceEntry,
      int sequence,
      String balanceTypeRequired,
      String currencyCodeFromStmt) {
    int lineTypeSelect = 0;
    String balanceType = getBalanceType(balanceEntry);
    if (!balanceTypeRequired.equals(balanceType)) {
      return sequence;
    }
    if (BALANCE_TYPE_INITIAL_BALANCE.equals(balanceType)) {
      // Initial balance
      lineTypeSelect = 1;
    } else if (BALANCE_TYPE_FINAL_BALANCE.equals(balanceType)) {
      // Final balance
      lineTypeSelect = 3;
    }

    LocalDate operationDate = getOperationDateFromBalanceEntry(balanceEntry);

    String currencyCode =
        Optional.of(balanceEntry)
            .map(CashBalance3::getAmt)
            .map(ActiveOrHistoricCurrencyAndAmount::getCcy)
            .orElse(null);

    String description = null;

    Currency currency = findCurrencyByCode(currencyCode);

    // set credit or debit
    String creditOrDebit =
        Optional.of(balanceEntry)
            .map(CashBalance3::getCdtDbtInd)
            .map(CreditDebitCode::value)
            .orElse(null);
    BigDecimal credit = BigDecimal.ZERO;
    BigDecimal debit = BigDecimal.ZERO;
    if (CREDIT_DEBIT_INDICATOR_CREDIT.equals(creditOrDebit)) {
      credit =
          Optional.of(balanceEntry)
              .map(CashBalance3::getAmt)
              .map(ActiveOrHistoricCurrencyAndAmount::getValue)
              .orElse(null);
      /*
        Check if currencyCode equals currencyCodeFromStmt.
        If not, record the foreign currency amount + the currency code in the description.
      */
      if (currencyCodeFromStmt != null && !currencyCodeFromStmt.equals(currencyCode)) {
        StringBuilder descriptionSB = new StringBuilder();
        descriptionSB.append("Entry.ccy=");
        descriptionSB.append(credit);
        descriptionSB.append(":");
        descriptionSB.append(currencyCode);
        description = descriptionSB.toString();
        differentCurrencyOccurrence++;
      }

    } else if (CREDIT_DEBIT_INDICATOR_DEBIT.equals(creditOrDebit)) {
      debit =
          Optional.of(balanceEntry)
              .map(CashBalance3::getAmt)
              .map(ActiveOrHistoricCurrencyAndAmount::getValue)
              .orElse(null);
      if (currencyCodeFromStmt != null && !currencyCodeFromStmt.equals(currencyCode)) {
        StringBuilder descriptionSB = new StringBuilder();
        descriptionSB.append("Entry.ccy=");
        descriptionSB.append(debit);
        descriptionSB.append(":");
        descriptionSB.append(currencyCode);
        description = descriptionSB.toString();
        differentCurrencyOccurrence++;
      }
    }
    if (bankDetails != null) {
      bankDetails = bankDetailsRepository.find(bankDetails.getId());
    }

    if (description != null
        && !description.isEmpty()
        && description.charAt(description.length() - 1) == ';') {
      description = description.substring(0, description.length() - 1);
    }

    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        bankStatementLineCreationCAMT53Service.createBankStatementLine(
            findBankStatement(),
            sequence,
            bankDetails,
            debit,
            credit,
            currency,
            description,
            operationDate,
            null,
            null,
            null,
            null,
            null,
            lineTypeSelect);

    updateBankStatementDate(operationDate, lineTypeSelect);
    bankStatementLineCAMT53Repository.save(bankStatementLineCAMT53);
    return ++sequence;
  }

  protected String getBalanceType(CashBalance3 balanceEntry) {
    return Optional.of(balanceEntry)
        .map(CashBalance3::getTp)
        .map(BalanceType12::getCdOrPrtry)
        .map(BalanceType5Choice::getCd)
        .map(BalanceType12Code::value)
        .orElse(null);
  }

  protected LocalDate getOperationDateFromBalanceEntry(CashBalance3 balanceEntry) {
    XMLGregorianCalendar date =
        Optional.of(balanceEntry)
                    .map(CashBalance3::getDt)
                    .map(DateAndDateTimeChoice::getDt)
                    .orElse(null)
                != null
            ? Optional.of(balanceEntry)
                .map(CashBalance3::getDt)
                .map(DateAndDateTimeChoice::getDt)
                .orElse(null)
            : Optional.of(balanceEntry)
                .map(CashBalance3::getDt)
                .map(DateAndDateTimeChoice::getDtTm)
                .orElse(null);
    LocalDate operationDate = null;
    if (date != null) {
      operationDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }
    return operationDate;
  }

  /**
   * Find the interbankCodeLineCodeLine by the input code.
   *
   * @param interbankCodeLineCode
   * @return InterbankCodeLine obj
   */
  protected InterbankCodeLine findInterBankCodeLineByCode(String interbankCodeLineCode) {
    return interbankCodeLineRepository
        .all()
        .filter("self.code = :code")
        .bind("code", interbankCodeLineCode)
        .fetchOne();
  }

  protected void updateBankStatementDate(LocalDate operationDate, int lineType) {
    if (operationDate == null) {
      return;
    }

    if (ObjectUtils.notEmpty(bankStatement.getFromDate())
        && lineType == BankStatementLineCAMT53Repository.LINE_TYPE_INITIAL_BALANCE) {
      if (operationDate.isBefore(bankStatement.getFromDate()))
        bankStatement.setFromDate(operationDate);
    } else if (lineType == BankStatementLineCAMT53Repository.LINE_TYPE_INITIAL_BALANCE) {
      bankStatement.setFromDate(operationDate);
    }

    if (ObjectUtils.notEmpty(bankStatement.getToDate())
        && lineType == BankStatementLineCAMT53Repository.LINE_TYPE_FINAL_BALANCE) {
      if (operationDate.isAfter(bankStatement.getToDate())) {
        bankStatement.setToDate(operationDate);
      }
    } else {
      if (lineType == BankStatementLineCAMT53Repository.LINE_TYPE_FINAL_BALANCE) {
        bankStatement.setToDate(operationDate);
      }
    }
  }

  protected Currency findCurrencyByCode(String currencyCode) {
    Currency currency = null;
    Query query =
        JPA.em()
            .createQuery(
                "select self.id " + "from Currency as self " + "where self.code = :currencyCode")
            .setParameter("currencyCode", currencyCode);
    List resultList = query.getResultList();
    if (!resultList.isEmpty()) {
      long currencyId = (long) resultList.get(0);
      currency = Beans.get(CurrencyRepository.class).find(currencyId);
    }
    return currency;
  }

  @Transactional
  protected void setBankStatementFromDate(DateTimePeriodDetails frToDt) {
    if (frToDt == null) {
      return;
    }
    XMLGregorianCalendar frDtTm = frToDt.getFrDtTm();
    if (frDtTm == null) {
      return;
    }
    LocalDate fromDate = LocalDate.of(frDtTm.getYear(), frDtTm.getMonth(), frDtTm.getDay());
    bankStatement.setFromDate(fromDate);
    bankStatementRepository.save(bankStatement);
  }

  @Transactional
  protected void setBankStatementToDate(DateTimePeriodDetails frToDt) {
    if (frToDt == null) {
      return;
    }
    XMLGregorianCalendar toDtTm = frToDt.getToDtTm();
    if (toDtTm == null) {
      return;
    }
    LocalDate toDate = LocalDate.of(toDtTm.getYear(), toDtTm.getMonth(), toDtTm.getDay());
    bankStatement.setToDate(toDate);
    bankStatementRepository.save(bankStatement);
  }

  /**
   * @param ibanOrOthers now we only support IBAN. Find BankDetails by IBAN.
   * @return
   */
  protected BankDetails findBankDetailsByIBAN(String ibanOrOthers) {
    return bankDetailsRepository.all().filter("self.iban = ?1", ibanOrOthers).fetchOne();
  }

  protected String addNtryInfoIntoDescription(ReportEntry2 ntry) {
    // <Ntry> -> <NtryDtls> -> <TxDtls>
    EntryTransaction2 txDtl =
        Optional.of(ntry)
            .map(ReportEntry2::getNtryDtls)
            .flatMap(
                ntryDtls ->
                    ntryDtls.stream()
                        .findFirst()) // Convert to Stream and get first element if present
            .map(EntryDetails1::getTxDtls)
            .flatMap(txDtls -> txDtls.stream().findFirst())
            .orElse(null);

    StringBuilder descriptionSb = new StringBuilder();
    // <Ntry> -> <BkTxCd> -> <Prtry> -> <Issr>
    String issr =
        Optional.of(ntry)
            .map(ReportEntry2::getBkTxCd)
            .map(BankTransactionCodeStructure4::getPrtry)
            .map(ProprietaryBankTransactionCodeStructure1::getIssr)
            .orElse(null);
    if (issr != null && !issr.isEmpty()) {
      descriptionSb.append("Ntry.BkTxCd.Prtry.Issr=");
      descriptionSb.append(issr);
      descriptionSb.append(";");
    }
    if (txDtl == null) {
      return descriptionSb.toString();
    } else {
      // <TxDtls> -> <Refs> -> <ChqNb>
      String cheqNb =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRefs)
              .map(TransactionReferences2::getChqNb)
              .orElse(null);
      if (cheqNb != null && !cheqNb.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.Refs.ChqNb=");
        descriptionSb.append(cheqNb);
        descriptionSb.append(";");
      }

      // <TxDtls> -> <Refs> -> <InstrId>
      String instrId =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRefs)
              .map(TransactionReferences2::getInstrId)
              .orElse(null);
      if (instrId != null && !instrId.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.Refs.InstrId=");
        descriptionSb.append(instrId);
        descriptionSb.append(";");
      }

      // <TxDtls> -> <Refs> -> <MndtId>
      String mndtId =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRefs)
              .map(TransactionReferences2::getMndtId)
              .orElse(null);
      if (mndtId != null && !mndtId.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.Refs.MndtId=");
        descriptionSb.append(mndtId);
        descriptionSb.append(";");
      }

      // <TxDtls> -> <RltdPties> -> <Cdtr> -> <Nm>
      String cdtrNm =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getCdtr)
              .map(PartyIdentification32::getNm)
              .orElse(null);
      if (cdtrNm != null && !cdtrNm.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RltdPties.Cdtr.Nm=");
        descriptionSb.append(cdtrNm);
        descriptionSb.append(";");
      }

      /*
      TODO: To be discussed:
            The "cdtrId" is still an object, but it doesn't have a toString method.
            It has the following sub-structure:
              <Id> ---
                  <OrgId>
                      ...
                  <PrvtId>
                      ...
            Check Payments_Maintenance_2009.pdf page 1008.
       */
      // <TxDtls> -> <RltdPties> -> <Cdtr> -> <Id>
      String cdtrIdString = null;
      Party6Choice cdtrId =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getCdtr)
              .map(PartyIdentification32::getId)
              .orElse(null);
      if (cdtrId != null) {
        cdtrIdString = cdtrId.toString();
      }
      if (cdtrIdString != null && !cdtrIdString.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RltdPties.Cdtr.Id=");
        descriptionSb.append(cdtrIdString);
        descriptionSb.append(";");
      }

      // <TxDtls> -> <RltdPties> -> <UltmtCdtr> -> <Nm>
      String ultmtCdtrNm =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getUltmtCdtr)
              .map(PartyIdentification32::getNm)
              .orElse(null);
      if (ultmtCdtrNm != null && !ultmtCdtrNm.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RltdPties.UltmtCdtr.Nm=");
        descriptionSb.append(ultmtCdtrNm);
        descriptionSb.append(";");
      }

      /*
      TODO: It has the same issue as the "cdtrId".
       */
      // <TxDtls> -> <RltdPties> -> <UltmtCdtr> -> <Id>
      Party6Choice ultmtCdtrId =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getUltmtCdtr)
              .map(PartyIdentification32::getId)
              .orElse(null);
      String ultmtCdtrIdString = null;
      if (ultmtCdtrId != null) {
        ultmtCdtrIdString = ultmtCdtrId.toString();
      }
      if (ultmtCdtrIdString != null && !ultmtCdtrIdString.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RltdPties.UltmtCdtr.Id=");
        descriptionSb.append(ultmtCdtrIdString);
        descriptionSb.append(";");
      }

      // <TxDtls> -> <RltdPties> -> <Dbtr> -> <Nm>
      String dbtrNm =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getDbtr)
              .map(PartyIdentification32::getNm)
              .orElse(null);
      if (dbtrNm != null && !dbtrNm.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RltdPties.Dbtr.Nm=");
        descriptionSb.append(dbtrNm);
        descriptionSb.append(";");
      }

      /*
      TODO: It has the same issue as the "cdtrId".
       */
      // <TxDtls> -> <RltdPties> -> <Dbtr> -> <Id>
      Party6Choice dbtrId =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getDbtr)
              .map(PartyIdentification32::getId)
              .orElse(null);
      String dbtrIdString = null;
      if (dbtrId != null) {
        dbtrIdString = dbtrId.toString();
      }
      if (dbtrIdString != null && !dbtrIdString.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RltdPties.Dbtr.Id=");
        descriptionSb.append(dbtrId);
        descriptionSb.append(";");
      }

      // <TxDtls> -> <RmtInf> -> <Ustrd>
      List<String> unstructuredLines =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRmtInf)
              .map(RemittanceInformation5::getUstrd)
              .orElse(null);
      if (unstructuredLines != null && !unstructuredLines.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RmtInf.Ustrd=");
        for (String unstructuredLine : unstructuredLines) {
          descriptionSb.append(unstructuredLine);
          descriptionSb.append(",");
        }
        if (descriptionSb.length() >= 1
            && descriptionSb.charAt(descriptionSb.length() - 1) == ',') {
          descriptionSb.deleteCharAt(descriptionSb.length() - 1);
        }
        descriptionSb.append(";");
      }

      // <TxDtls> -> <Rmtlnf> -> <Strd> -> <CdtrReflnf> -><Ref>
      String strdCdtrRefInfref =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRmtInf)
              .map(RemittanceInformation5::getStrd)
              .flatMap(strds -> strds.stream().findFirst())
              .map(StructuredRemittanceInformation7::getCdtrRefInf)
              .map(CreditorReferenceInformation2::getRef)
              .orElse(null);
      if (strdCdtrRefInfref != null && !strdCdtrRefInfref.isEmpty()) {
        descriptionSb.append("Ntry.TxDtls.RmtInf.Strd.CdtrRefInf.Ref=");
        descriptionSb.append(strdCdtrRefInfref);
        descriptionSb.append(";");
      }

      // <TxDtls> -> <AddtlTxInf>
      String addtlTxInf = Optional.of(txDtl).map(EntryTransaction2::getAddtlTxInf).orElse(null);
      if (addtlTxInf != null && !addtlTxInf.isEmpty()) {
        descriptionSb.append("RmtInf.AddtlTxInf=");
        descriptionSb.append(addtlTxInf);
        descriptionSb.append(";");
      }
    }

    return descriptionSb.toString();
  }
}
