package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineCAMT53;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineCAMT53Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
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
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CreditDebitCode;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateAndDateTimeChoice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateTimePeriodDetails;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.Document;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryDetails1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryTransaction2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.GenericAccountIdentification1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ProprietaryBankTransactionCodeStructure1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReportEntry2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.TransactionReferences2;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
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

  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;

  protected BankStatementLineCAMT53Repository bankStatementLineCAMT53Repository;

  protected InterbankCodeLineRepository interbankCodeLineRepository;

  protected BankDetailsRepository bankDetailsRepository;

  protected BankStatementLineCreationCAMT53Service bankStatementLineCreationCAMT53Service;

  @Inject
  protected BankStatementLineCreateCAMT53Service(
      BankStatementRepository bankStatementRepository,
      BankStatementImportService bankStatementService,
      CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment,
      BankStatementLineCAMT53Repository bankStatementLineCAMT53Repository,
      InterbankCodeLineRepository interbankCodeLineRepository,
      BankDetailsRepository bankDetailsRepository,
      BankStatementLineCreationCAMT53Service bankStatementLineCreationCAMT53Service) {
    super(bankStatementRepository, bankStatementService);
    this.currencyScaleServiceBankPayment = currencyScaleServiceBankPayment;
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

  @Override
  protected void process() throws IOException, AxelorException {
    try {
      /*
      1. find bank details
      2. create bankstatement lines, including:
          a. balance line
          b. statement entry line
       */
      JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Document document = (Document) jaxbUnmarshaller.unmarshal(file);
      if (document == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Error: Cannot read the input file."));
      }
      BankToCustomerStatementV02 bkToCstmrStmt = document.getBkToCstmrStmt();
      if (bkToCstmrStmt == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Error: No bank statement found."));
      }
      List<AccountStatement2> stmtList = bkToCstmrStmt.getStmt();
      if (stmtList == null || stmtList.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Error: No bank statement found."));
      }
      AccountStatement2 stmt = stmtList.get(0);
      if (stmt == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Error: No bank statement found."));
      }
      DateTimePeriodDetails frToDt = stmt.getFrToDt();
      bankStatement = setBankStatementFrToDt(frToDt, bankStatement);

      CashAccount20 acct = stmt.getAcct();
      BankDetails bankDetails = null;
      if (acct != null) {
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
        // find bankDetails
        bankDetails = findBankDetailsByIban(ibanOrOthers);

        if (bankDetails == null) {
          //        throw new AxelorException(
          //            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Bank details is not
          // found."));

        }
      }

      int sequence = 0;

      List<CashBalance3> balList = stmt.getBal();
      if (balList != null && !balList.isEmpty()) {
        for (CashBalance3 balanceEntry : balList) {
          sequence = createBalanceLine(bankDetails, balanceEntry, sequence);
          if (sequence % 10 == 0) {
            JPA.clear();
            findBankStatement();
          }
        }
      }

      List<ReportEntry2> ntryList = stmt.getNtry();
      if (ntryList != null && !ntryList.isEmpty()) {
        for (ReportEntry2 ntry : ntryList) {
          sequence = createEntryLine(bankDetails, ntry, sequence);
          if (sequence % 10 == 0) {
            JPA.clear();
            findBankStatement();
          }
        }
      }

      changeStatusSelectToImported();

    } catch (jakarta.xml.bind.JAXBException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Error: File format unmarshalling process failed."));
    }
  }

  @Transactional
  protected void changeStatusSelectToImported() {
    bankStatement.setStatusSelect(2);
    bankStatementRepository.save(bankStatement);
  }

  @Transactional
  protected int createEntryLine(BankDetails bankDetails, ReportEntry2 ntry, int sequence) {
    XMLGregorianCalendar opDate =
        Optional.of(ntry)
            .map(ReportEntry2::getBookgDt)
            .map(DateAndDateTimeChoice::getDtTm)
            .orElse(null);
    if (opDate == null) { // sometimes the DtTm is null, then check Dt.
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

    XMLGregorianCalendar valDate =
        Optional.of(ntry)
            .map(ReportEntry2::getValDt)
            .map(DateAndDateTimeChoice::getDtTm)
            .orElse(null);
    if (valDate == null) { // sometimes the DtTm is null, then check Dt.
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

    String currencyCode =
        Optional.of(ntry)
            .map(ReportEntry2::getAmt)
            .map(ActiveOrHistoricCurrencyAndAmount::getCcy)
            .orElse(null);
    Currency currency = findCurrencyByCode(currencyCode);

    String creditOrDebit =
        Optional.of(ntry).map(ReportEntry2::getCdtDbtInd).map(CreditDebitCode::value).orElse(null);
    BigDecimal credit = BigDecimal.ZERO;
    BigDecimal debit = BigDecimal.ZERO;
    if (CREDIT_DEBIT_INDICATOR_CREDIT.equals(creditOrDebit)) {
      credit =
          Optional.of(ntry)
              .map(ReportEntry2::getAmt)
              .map(ActiveOrHistoricCurrencyAndAmount::getValue)
              .orElse(null);
    } else if (CREDIT_DEBIT_INDICATOR_DEBIT.equals(creditOrDebit)) {
      debit =
          Optional.of(ntry)
              .map(ReportEntry2::getAmt)
              .map(ActiveOrHistoricCurrencyAndAmount::getValue)
              .orElse(null);
    }

    String origin =
        Optional.ofNullable(ntry)
            .map(ReportEntry2::getNtryDtls)
            .flatMap(
                ntryDtls ->
                    ntryDtls.stream()
                        .findFirst()) // Convert to Stream and get first element if present
            .map(EntryDetails1::getTxDtls)
            .flatMap(txDtls -> txDtls.stream().findFirst()) // Same for TxDtls
            .map(EntryTransaction2::getRefs)
            .map(TransactionReferences2::getAcctSvcrRef)
            .orElse(null);

    String reference =
        Optional.ofNullable(ntry)
            .map(ReportEntry2::getNtryDtls)
            .flatMap(ntryDtls -> ntryDtls.stream().findFirst())
            .map(EntryDetails1::getTxDtls)
            .flatMap(txDtls -> txDtls.stream().findFirst())
            .map(EntryTransaction2::getRefs)
            .map(TransactionReferences2::getEndToEndId)
            .orElse(null);

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

    // Possible type values: int TYPE_OPERATION_CODE = 1;
    //                      int TYPE_REJECT_RETURN_CODE = 2;
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
    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        bankStatementLineCreationCAMT53Service.createBankStatementLine(
            findBankStatement(),
            sequence,
            bankDetails,
            debit,
            credit,
            currency,
            null,
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

  @Transactional
  protected int createBalanceLine(
      BankDetails bankDetails, CashBalance3 balanceEntry, int sequence) {
    int lineTypeSelect = 0;
    String balanceType =
        Optional.of(balanceEntry)
            .map(CashBalance3::getTp)
            .map(BalanceType12::getCdOrPrtry)
            .map(BalanceType5Choice::getCd)
            .map(BalanceType12Code::value)
            .orElse(null);
    if (BALANCE_TYPE_INITIAL_BALANCE.equals(balanceType)) {
      // Initial balance
      lineTypeSelect = 1;
    } else if (BALANCE_TYPE_FINAL_BALANCE.equals(balanceType)) {
      // Final balance
      lineTypeSelect = 3;
    }

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

    String currencyCode =
        Optional.of(balanceEntry)
            .map(CashBalance3::getAmt)
            .map(ActiveOrHistoricCurrencyAndAmount::getCcy)
            .orElse(null);
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

    } else if (CREDIT_DEBIT_INDICATOR_DEBIT.equals(creditOrDebit)) {
      debit =
          Optional.of(balanceEntry)
              .map(CashBalance3::getAmt)
              .map(ActiveOrHistoricCurrencyAndAmount::getValue)
              .orElse(null);
    }
    if (bankDetails != null) {
      bankDetails = bankDetailsRepository.find(bankDetails.getId());
    }
    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        bankStatementLineCreationCAMT53Service.createBankStatementLine(
            findBankStatement(),
            sequence,
            bankDetails,
            debit,
            credit,
            currency,
            null,
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
  protected BankStatement setBankStatementFrToDt(
      DateTimePeriodDetails frToDt, BankStatement bankStatement) {
    // Bank Statement From Date
    if (frToDt == null) {
      return bankStatement;
    }
    XMLGregorianCalendar frDtTm = frToDt.getFrDtTm();
    if (frDtTm == null) {
      return bankStatement;
    }
    LocalDate fromDate = LocalDate.of(frDtTm.getYear(), frDtTm.getMonth(), frDtTm.getDay());
    // Bank Statement To Date
    XMLGregorianCalendar toDtTm = frToDt.getToDtTm();
    if (toDtTm == null) {
      return bankStatement;
    }
    LocalDate toDate = LocalDate.of(toDtTm.getYear(), toDtTm.getMonth(), toDtTm.getDay());
    bankStatement.setFromDate(fromDate);
    bankStatement.setToDate(toDate);
    bankStatementRepository.save(bankStatement);
    return bankStatement;
  }

  protected BankDetails findBankDetailsByIban(String ibanOrOthers) {
    return bankDetailsRepository.all().filter("self.iban = ?1", ibanOrOthers).fetchOne();
  }
}
