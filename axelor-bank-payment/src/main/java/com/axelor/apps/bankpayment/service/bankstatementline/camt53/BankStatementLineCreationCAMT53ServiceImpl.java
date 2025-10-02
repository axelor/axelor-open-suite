package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineCAMT53;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineCAMT53Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementDateService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreationService;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class BankStatementLineCreationCAMT53ServiceImpl
    implements BankStatementLineCreationCAMT53Service {

  public static String CREDIT_DEBIT_INDICATOR_CREDIT = "CRDT";
  public static String CREDIT_DEBIT_INDICATOR_DEBIT = "DBIT";

  protected CAMT53ToolService camt53ToolService;
  protected BankStatementLineCreationService bankStatementLineCreationService;
  protected BankStatementDateService bankStatementDateService;
  protected CurrencyRepository currencyRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected BankStatementLineCAMT53Repository bankStatementLineCAMT53Repository;
  protected InterbankCodeLineRepository interBankCodeLineRepository;

  @Inject
  public BankStatementLineCreationCAMT53ServiceImpl(
      CAMT53ToolService camt53ToolService,
      BankStatementLineCreationService bankStatementLineCreationService,
      BankStatementDateService bankStatementDateService,
      CurrencyRepository currencyRepository,
      BankDetailsRepository bankDetailsRepository,
      BankStatementLineCAMT53Repository bankStatementLineCAMT53Repository,
      InterbankCodeLineRepository interBankCodeLineRepository) {
    this.camt53ToolService = camt53ToolService;
    this.bankStatementLineCreationService = bankStatementLineCreationService;
    this.bankStatementDateService = bankStatementDateService;
    this.currencyRepository = currencyRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.bankStatementLineCAMT53Repository = bankStatementLineCAMT53Repository;
    this.interBankCodeLineRepository = interBankCodeLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public int createBalanceLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      CashBalance3 balanceEntry,
      int sequence,
      String balanceTypeRequired,
      String currencyCodeFromStmt) {
    int lineTypeSelect = 0;
    String balanceType = camt53ToolService.getBalanceType(balanceEntry);
    if (!balanceTypeRequired.equals(balanceType)) {
      return sequence;
    }
    if (BankStatementFileFormatRepository.CAMT_BALANCE_TYPE_INITIAL_BALANCE.equals(balanceType)) {
      // Initial balance
      lineTypeSelect = BankStatementLineRepository.LINE_TYPE_INITIAL_BALANCE;
    } else if (BankStatementFileFormatRepository.CAMT_BALANCE_TYPE_FINAL_BALANCE.equals(
        balanceType)) {
      // Final balance
      lineTypeSelect = BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE;
    }

    LocalDate operationDate =
        camt53ToolService.computeLocalDateFromDateTimeChoice(balanceEntry.getDt());

    String currencyCode =
        Optional.of(balanceEntry)
            .map(CashBalance3::getAmt)
            .map(ActiveOrHistoricCurrencyAndAmount::getCcy)
            .orElse(null);

    String description = null;

    Currency currency = currencyRepository.findByCode(currencyCode);

    String creditDebitIndicator =
        camt53ToolService.getCreditDebitIndicatorFromCashEntry(balanceEntry);

    BigDecimal amount = camt53ToolService.getCashEntryValue(balanceEntry);

    description = checkCurrencyChange(currencyCodeFromStmt, currencyCode, amount);

    BigDecimal credit = BigDecimal.ZERO;
    BigDecimal debit = BigDecimal.ZERO;
    if (CREDIT_DEBIT_INDICATOR_CREDIT.equals(creditDebitIndicator)) {
      credit = amount;
    } else if (CREDIT_DEBIT_INDICATOR_DEBIT.equals(creditDebitIndicator)) {
      debit = amount;
    }

    if (bankDetails != null) {
      bankDetails = bankDetailsRepository.find(bankDetails.getId());
    }

    if (description != null && description.endsWith(";")) {
      description = description.substring(0, description.length() - 1);
    }

    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        createBankStatementLine(
            bankStatement,
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
            lineTypeSelect,
            null);

    bankStatementDateService.updateBankStatementDate(bankStatement, operationDate, lineTypeSelect);
    bankStatementLineCAMT53Repository.save(bankStatementLineCAMT53);
    return ++sequence;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public int createEntryLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      ReportEntry2 ntry,
      int sequence,
      String currencyCodeFromStmt) {
    LocalDate operationDate =
        camt53ToolService.computeLocalDateFromDateTimeChoice(ntry.getBookgDt());

    LocalDate valueDate = camt53ToolService.computeLocalDateFromDateTimeChoice(ntry.getValDt());

    String description = Optional.of(ntry).map(ReportEntry2::getAcctSvcrRef).orElse("");

    Integer commissionExemptionIndexSelect =
        camt53ToolService.getCommissionExemptionIndexSelect(ntry);

    String currencyCode =
        Optional.of(ntry)
            .map(ReportEntry2::getAmt)
            .map(ActiveOrHistoricCurrencyAndAmount::getCcy)
            .orElse(null);
    Currency currency = currencyRepository.findByCode(currencyCode);

    String creditDebitIndicator = camt53ToolService.getCreditDebitIndicatorFromReportEntry(ntry);

    BigDecimal amount = camt53ToolService.getReportEntryValue(ntry);

    description += checkCurrencyChange(currencyCodeFromStmt, currencyCode, amount);

    BigDecimal credit = BigDecimal.ZERO;
    BigDecimal debit = BigDecimal.ZERO;
    if (CREDIT_DEBIT_INDICATOR_CREDIT.equals(creditDebitIndicator)) {
      credit = amount;
    } else if (CREDIT_DEBIT_INDICATOR_DEBIT.equals(creditDebitIndicator)) {
      debit = amount;
    }

    String origin = description;

    String reference = camt53ToolService.getReference(ntry);

    InterbankCodeLine operationInterbankCodeLine =
        camt53ToolService.getOperationCodeInterBankCodeLineCode(ntry);
    InterbankCodeLine rejectInterbankCodeLine =
        camt53ToolService.getRejectReturnInterBankCodeLineCode(ntry);

    if (bankDetails != null) {
      bankDetails = bankDetailsRepository.find(bankDetails.getId());
    }

    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        createBankStatementLine(
            bankStatement,
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
            BankStatementLineRepository.LINE_TYPE_MOVEMENT,
            commissionExemptionIndexSelect);

    bankStatementLineCAMT53Repository.save(bankStatementLineCAMT53);
    return ++sequence;
  }

  protected BankStatementLineCAMT53 createBankStatementLine(
      BankStatement bankStatement,
      int sequence,
      BankDetails bankDetails,
      BigDecimal debit,
      BigDecimal credit,
      Currency currency,
      String description,
      LocalDate operationDate,
      LocalDate valueDate,
      InterbankCodeLine operationInterbankCodeLine,
      InterbankCodeLine rejectInterbankCodeLine,
      String origin,
      String reference,
      int lineType,
      Integer commissionExemptionIndexSelect) {
    BankStatementLine bankStatementLine =
        bankStatementLineCreationService.createBankStatementLine(
            bankStatement,
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
            reference);
    BankStatementLineCAMT53 bankStatementLineCAMT53 =
        Mapper.toBean(BankStatementLineCAMT53.class, Mapper.toMap(bankStatementLine));
    bankStatementLineCAMT53.setCommissionExemptionIndexSelect(commissionExemptionIndexSelect);
    bankStatementLineCAMT53.setLineTypeSelect(lineType);

    if (lineType != BankStatementLineRepository.LINE_TYPE_MOVEMENT) {
      bankStatementLineCAMT53.setAmountRemainToReconcile(BigDecimal.ZERO);
    }
    return bankStatementLineCAMT53;
  }

  protected String checkCurrencyChange(
      String currencyCodeFromStmt, String currencyCode, BigDecimal amount) {
    StringBuilder descriptionSB = new StringBuilder();

    if (currencyCodeFromStmt != null && !currencyCodeFromStmt.equals(currencyCode)) {
      descriptionSB.append("Entry.ccy=");
      descriptionSB.append(amount);
      descriptionSB.append(":");
      descriptionSB.append(currencyCode);
    }

    return descriptionSB.toString();
  }
}
