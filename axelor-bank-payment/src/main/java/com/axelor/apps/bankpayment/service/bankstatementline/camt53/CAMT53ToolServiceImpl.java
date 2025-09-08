package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCode;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountIdentification4Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType12;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType12Code;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType5Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BankTransactionCodeStructure4;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CreditDebitCode;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateAndDateTimeChoice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateTimePeriodDetails;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryDetails1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryTransaction2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.GenericAccountIdentification1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ProprietaryBankTransactionCodeStructure1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReportEntry2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReturnReason5Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReturnReasonInformation10;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.TransactionReferences2;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.AppAccount;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.xml.datatype.XMLGregorianCalendar;

public class CAMT53ToolServiceImpl implements CAMT53ToolService {

  protected AppAccountService appAccountService;
  protected BankStatementRepository bankStatementRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected InterbankCodeLineRepository interBankCodeLineRepository;

  @Inject
  public CAMT53ToolServiceImpl(
      AppAccountService appAccountService,
      BankStatementRepository bankStatementRepository,
      BankDetailsRepository bankDetailsRepository,
      InterbankCodeLineRepository interBankCodeLineRepository) {
    this.appAccountService = appAccountService;
    this.bankStatementRepository = bankStatementRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.interBankCodeLineRepository = interBankCodeLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeBankStatementDates(
      BankStatement bankStatement, List<AccountStatement2> stmtList) {
    bankStatement.setFromDate(
        computeLocalDate(
            Optional.ofNullable(stmtList.get(0))
                .map(AccountStatement2::getFrToDt)
                .map(DateTimePeriodDetails::getFrDtTm)
                .orElse(null)));
    bankStatement.setToDate(
        computeLocalDate(
            Optional.ofNullable(stmtList.get(stmtList.size() - 1))
                .map(AccountStatement2::getFrToDt)
                .map(DateTimePeriodDetails::getToDtTm)
                .orElse(null)));

    bankStatementRepository.save(bankStatement);
  }

  @Override
  public LocalDate computeLocalDateFromDateTimeChoice(DateAndDateTimeChoice dateTimeChoice) {
    if (dateTimeChoice == null) {
      return null;
    }

    return computeLocalDates(dateTimeChoice.getDtTm(), dateTimeChoice.getDt());
  }

  protected LocalDate computeLocalDates(
      XMLGregorianCalendar firstDate, XMLGregorianCalendar secondDate) {
    return computeLocalDate(firstDate != null ? firstDate : secondDate);
  }

  protected LocalDate computeLocalDate(XMLGregorianCalendar date) {
    if (date == null) {
      return null;
    }

    return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
  }

  @Override
  public String getBalanceType(CashBalance3 balanceEntry) {
    return Optional.of(balanceEntry)
        .map(CashBalance3::getTp)
        .map(BalanceType12::getCdOrPrtry)
        .map(BalanceType5Choice::getCd)
        .map(BalanceType12Code::value)
        .orElse(null);
  }

  @Override
  public String getCreditDebitIndicatorFromReportEntry(ReportEntry2 ntry) {
    return Optional.of(ntry)
        .map(ReportEntry2::getCdtDbtInd)
        .map(CreditDebitCode::value)
        .orElse(null);
  }

  @Override
  public String getCreditDebitIndicatorFromCashEntry(CashBalance3 balanceEntry) {
    return Optional.of(balanceEntry)
        .map(CashBalance3::getCdtDbtInd)
        .map(CreditDebitCode::value)
        .orElse(null);
  }

  @Override
  public BigDecimal getReportEntryValue(ReportEntry2 ntry) {
    return Optional.of(ntry)
        .map(ReportEntry2::getAmt)
        .map(ActiveOrHistoricCurrencyAndAmount::getValue)
        .orElse(BigDecimal.ZERO);
  }

  @Override
  public BigDecimal getCashEntryValue(CashBalance3 balance) {
    return Optional.of(balance)
        .map(CashBalance3::getAmt)
        .map(ActiveOrHistoricCurrencyAndAmount::getValue)
        .orElse(BigDecimal.ZERO);
  }

  @Override
  public String getReference(ReportEntry2 ntry) {
    return Optional.ofNullable(ntry)
        .map(ReportEntry2::getNtryDtls)
        .flatMap(ntryDtls -> ntryDtls.stream().findFirst())
        .map(EntryDetails1::getTxDtls)
        .flatMap(txDtls -> txDtls.stream().findFirst())
        .map(EntryTransaction2::getRefs)
        .map(TransactionReferences2::getEndToEndId)
        .orElse("");
  }

  @Override
  public Integer getCommissionExemptionIndexSelect(ReportEntry2 ntry) {
    if (ntry == null) {
      return null;
    }
    String addtlNtryInf = Optional.of(ntry).map(ReportEntry2::getAddtlNtryInf).orElse(null);
    if (addtlNtryInf == null || addtlNtryInf.isEmpty()) {
      return null;
    }
    if ("YES".equalsIgnoreCase(addtlNtryInf.replace("/ECM/", ""))) {
      return BankStatementLineAFB120Repository.COMISSION_EXEMPTION_INDEX_EXEMPT;
    }
    if ("No".equalsIgnoreCase(addtlNtryInf.replace("/ECM/", ""))) {
      return BankStatementLineAFB120Repository.COMISSION_EXEMPTION_INDEX_NOT_EXEMPT;
    }
    return null;
  }

  /**
   * @param ibanOrOthers now we only support IBAN. Find BankDetails by IBAN.
   * @return
   */
  @Override
  public BankDetails findBankDetailsByIBAN(CashAccount20 acct) {
    if (acct == null) {
      return null;
    }

    BankDetails bankDetails = null;
    String ibanOrOthers = getIBANOrOtherAccountIdentification(acct);
    if (StringUtils.notEmpty(ibanOrOthers)) {
      bankDetails = bankDetailsRepository.all().filter("self.iban = ?1", ibanOrOthers).fetchOne();
    }
    return bankDetails;
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

  @Override
  public InterbankCodeLine getOperationCodeInterBankCodeLineCode(ReportEntry2 reportEntry2) {
    String interBankCodeLineCodeStr = getOperationCodeInterBankCodeLineCodeStr(reportEntry2);

    if (StringUtils.isEmpty(interBankCodeLineCodeStr)) {
      return null;
    }

    return interBankCodeLineRepository.findOperationCodeByCode(interBankCodeLineCodeStr);
  }

  @Override
  public InterbankCodeLine getRejectReturnInterBankCodeLineCode(ReportEntry2 reportEntry2) {
    String interBankCodeLineCodeStr = getRejectReturnInterBankCodeLineCodeStr(reportEntry2);
    InterbankCode chequeInterbankCode =
        Optional.ofNullable(appAccountService.getAppAccount())
            .map(AppAccount::getChequeInterbankCode)
            .orElse(null);

    if (StringUtils.isEmpty(interBankCodeLineCodeStr) || chequeInterbankCode == null) {
      return null;
    }

    return interBankCodeLineRepository.findOperationCodeByCodeAndInterBankCode(
        interBankCodeLineCodeStr, chequeInterbankCode);
  }

  protected String getOperationCodeInterBankCodeLineCodeStr(ReportEntry2 reportEntry2) {
    String interBankCodeLineCode = null;
    String code =
        Optional.ofNullable(reportEntry2)
            .map(ReportEntry2::getBkTxCd)
            .map(BankTransactionCodeStructure4::getPrtry)
            .map(ProprietaryBankTransactionCodeStructure1::getCd)
            .orElse("");

    if (StringUtils.isEmpty(code)) {
      return "";
    }

    return (code.split("/"))[0];
  }

  protected String getRejectReturnInterBankCodeLineCodeStr(ReportEntry2 reportEntry2) {
    String interBankCodeLineCode = null;
    String code =
        Optional.ofNullable(reportEntry2)
            .map(ReportEntry2::getNtryDtls)
            .flatMap(ntryDtls -> ntryDtls.stream().findFirst())
            .map(EntryDetails1::getTxDtls)
            .flatMap(txDtls -> txDtls.stream().findFirst())
            .map(EntryTransaction2::getRtrInf)
            .map(ReturnReasonInformation10::getRsn)
            .map(ReturnReason5Choice::getPrtry)
            .orElse("");

    if (StringUtils.isEmpty(code)) {
      return "";
    }

    return (code.split("/"))[0];
  }
}
