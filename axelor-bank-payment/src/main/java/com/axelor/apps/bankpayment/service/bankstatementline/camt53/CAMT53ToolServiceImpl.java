package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCode;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.report.ITranslation;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.AccountIdentification4Choice;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.BalanceType12;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.BalanceType12Code;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.BalanceType5Choice;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.BankTransactionCodeStructure4;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.BatchInformation2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashAccount16;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CreditDebitCode;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CreditorReferenceInformation2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.DateAndDateTimeChoice;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.DateTimePeriodDetails;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.EntryDetails1;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.EntryTransaction2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.GenericAccountIdentification1;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.OrganisationIdentification4;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.Party6Choice;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.PartyIdentification32;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ProprietaryBankTransactionCodeStructure1;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.RemittanceInformation5;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReportEntry2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReturnReason5Choice;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReturnReasonInformation10;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.StructuredRemittanceInformation7;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.TransactionParty2;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.TransactionReferences2;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppAccount;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
  public String getOrigin(ReportEntry2 ntry) {
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
      TransactionReferences2 refs =
          Optional.ofNullable(ntry)
              .map(ReportEntry2::getNtryDtls)
              .flatMap(
                  ntryDtls ->
                      ntryDtls.stream()
                          .findFirst()) // Convert to Stream and get first element if present
              .map(EntryDetails1::getTxDtls)
              .flatMap(txDtls -> txDtls.stream().findFirst())
              .map(EntryTransaction2::getRefs)
              .orElse(null);
      if (ObjectUtils.notEmpty(refs)) {
        origin = refs.getPmtInfId();

        if (origin == null) {
          origin = refs.getChqNb();
        }
      }
    }
    return origin;
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

  @Override
  public String constructDescriptionFromNtry(ReportEntry2 ntry) {
    /*
    * get the following tag values:
    * 1. TxDtls -> RmtInf -> Ustrd : as the first line
    * 2. TxDtls -> RmtInf -> Strd -> CdtrRefInf -> Ref
    * 3. TxDtls -> AddtlTxInf : remove "/LIB/"
    * 4. RltdPties -> Cdtr -> Nm
    * 5. RltdPties -> Dbtr -> Nm
    * 6. RltdPties -> UltmtDbtr -> Nm
    * 7. RltdPties -> UltmtDbtr -> Id -> OrgId -> BICOrBEI
    * 8. RltdPties -> UltmtCdtr -> Nm
    * 9. NtryDtls -> Btch -> PmtInfId
    * 10. RltdPties -> CdtrAcct -> Id -> IBAN
    * 11. NtryDtls -> Btch -> NbOfTxs

    */
    EntryDetails1 ntryDtls =
        Optional.of(ntry)
            .map(ReportEntry2::getNtryDtls)
            .flatMap(ntryDtlsList -> ntryDtlsList.stream().findFirst())
            .orElse(null);
    EntryTransaction2 txDtl =
        Optional.ofNullable(ntryDtls)
            .map(EntryDetails1::getTxDtls)
            .flatMap(txDtls -> txDtls.stream().findFirst())
            .orElse(null);

    List<String> descriptionLines = new ArrayList<>();
    if (txDtl != null) {
      List<String> ustrdList =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRmtInf)
              .map(RemittanceInformation5::getUstrd)
              .orElse(null);
      String line1 = "";
      if (ustrdList != null && !ustrdList.isEmpty()) {
        line1 = String.join(" ", ustrdList);
      }
      descriptionLines.add(line1);

      String strdStr =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRmtInf)
              .map(RemittanceInformation5::getStrd)
              .flatMap(strd -> strd.stream().findFirst())
              .map(StructuredRemittanceInformation7::getCdtrRefInf)
              .map(CreditorReferenceInformation2::getRef)
              .orElse(null);
      String line2 = "";
      if (strdStr != null && !strdStr.isEmpty()) {
        line2 = String.join(" ", strdStr);
      }
      descriptionLines.add(line2);

      String line3 = "";
      String addtlTxInf = Optional.of(txDtl).map(EntryTransaction2::getAddtlTxInf).orElse(null);
      if (addtlTxInf != null && !addtlTxInf.isEmpty()) {
        line3 = addtlTxInf.replace("/LIB/", "\n");
      }
      descriptionLines.add(line3);

      String line4 = "";
      String cdtrNm =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getCdtr)
              .map(PartyIdentification32::getNm)
              .orElse(null);
      if (cdtrNm != null && !cdtrNm.isEmpty()) {
        line4 = cdtrNm;
      }
      descriptionLines.add(line4);

      String line5 = "";
      String dbtrNm =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getDbtr)
              .map(PartyIdentification32::getNm)
              .orElse(null);
      if (dbtrNm != null && !dbtrNm.isEmpty()) {
        line5 = dbtrNm;
      }
      descriptionLines.add(line5);

      String line6 = "";
      String ultmtDbtrNm =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getUltmtDbtr)
              .map(PartyIdentification32::getNm)
              .orElse(null);
      if (ultmtDbtrNm != null && !ultmtDbtrNm.isEmpty()) {
        line6 = ultmtDbtrNm;
      }
      descriptionLines.add(line6);

      String line7 = "";
      String bicOrBEI =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getUltmtDbtr)
              .map(PartyIdentification32::getId)
              .map(Party6Choice::getOrgId)
              .map(OrganisationIdentification4::getBICOrBEI)
              .orElse(null);
      if (bicOrBEI != null && !bicOrBEI.isEmpty()) {
        line7 = bicOrBEI;
      }
      descriptionLines.add(line7);

      String line8 = "";
      String ultmtCdtrNm =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getUltmtCdtr)
              .map(PartyIdentification32::getNm)
              .orElse(null);
      if (ultmtCdtrNm != null && !ultmtCdtrNm.isEmpty()) {
        line8 = ultmtCdtrNm;
      }
      descriptionLines.add(line8);
    }

    String line9 = "";
    String btchPmtInfId =
        Optional.ofNullable(ntryDtls)
            .map(EntryDetails1::getBtch)
            .map(BatchInformation2::getPmtInfId)
            .orElse(null);
    if (btchPmtInfId != null && !btchPmtInfId.isEmpty()) {
      line9 = btchPmtInfId;
    }
    descriptionLines.add(line9);

    if (txDtl != null) {
      // RltdPties -> CdtrAcct -> Id -> IBAN
      String line10 = "";
      String cdtrAcctIBAN =
          Optional.of(txDtl)
              .map(EntryTransaction2::getRltdPties)
              .map(TransactionParty2::getCdtrAcct)
              .map(CashAccount16::getId)
              .map(AccountIdentification4Choice::getIBAN)
              .orElse(null);
      if (cdtrAcctIBAN != null && !cdtrAcctIBAN.isEmpty()) {
        line10 = cdtrAcctIBAN;
      }
      descriptionLines.add(line10);
    }

    String line11 = "";
    String chqNb =
        Optional.ofNullable(ntryDtls)
            .map(EntryDetails1::getBtch)
            .map(BatchInformation2::getNbOfTxs)
            .orElse(null);
    if (chqNb != null && !chqNb.isEmpty()) {
      line11 = String.format(I18n.get(ITranslation.CAMT053_CHQ_TRANSACTION_LABEL), chqNb);
    }
    descriptionLines.add(line11);

    StringBuilder descriptionSB = new StringBuilder();
    for (int i = 0; i < descriptionLines.size() - 1; i++) {
      String line = descriptionLines.get(i);
      if (line != null && !line.isEmpty()) {
        descriptionSB.append(line);
        descriptionSB.append("\n");
      }
    }
    descriptionSB.append(descriptionLines.get(descriptionLines.size() - 1));
    return descriptionSB.toString();
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
