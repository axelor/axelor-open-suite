package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatementrule.BankStatementRuleService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class BankReconciliationMoveGenerationServiceImpl
    implements BankReconciliationMoveGenerationService {

  protected static final int RETURNED_SCALE = 2;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;
  protected BankStatementRuleRepository bankStatementRuleRepository;
  protected BankReconciliationLineService bankReconciliationLineService;
  protected MoveValidateService moveValidateService;
  protected BankStatementRuleService bankStatementRuleService;
  protected ReconcileService reconcileService;
  protected MoveCreateService moveCreateService;
  protected MoveRepository moveRepository;
  protected AccountingSituationRepository accountingSituationRepository;
  protected TaxAccountService taxAccountService;
  protected MoveLineTaxService moveLineTaxService;
  protected TaxService taxService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineService moveLineService;

  @Inject
  public BankReconciliationMoveGenerationServiceImpl(
      BankReconciliationLineRepository bankReconciliationLineRepository,
      BankStatementRuleRepository bankStatementRuleRepository,
      BankReconciliationLineService bankReconciliationLineService,
      MoveValidateService moveValidateService,
      BankStatementRuleService bankStatementRuleService,
      ReconcileService reconcileService,
      MoveCreateService moveCreateService,
      MoveRepository moveRepository,
      AccountingSituationRepository accountingSituationRepository,
      TaxAccountService taxAccountService,
      MoveLineTaxService moveLineTaxService,
      TaxService taxService,
      MoveLineCreateService moveLineCreateService,
      MoveLineService moveLineService) {
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
    this.bankStatementRuleRepository = bankStatementRuleRepository;
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.moveValidateService = moveValidateService;
    this.bankStatementRuleService = bankStatementRuleService;
    this.reconcileService = reconcileService;
    this.moveCreateService = moveCreateService;
    this.moveRepository = moveRepository;
    this.accountingSituationRepository = accountingSituationRepository;
    this.taxAccountService = taxAccountService;
    this.moveLineTaxService = moveLineTaxService;
    this.taxService = taxService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineService = moveLineService;
  }

  @Override
  public void generateMovesAutoAccounting(BankReconciliation bankReconciliation)
      throws AxelorException {
    Context scriptContext;
    Move move;
    int limit = 10;
    int offset = 0;
    List<BankReconciliationLine> bankReconciliationLines =
        bankReconciliationLineRepository
            .findByBankReconciliation(bankReconciliation)
            .fetch(limit, offset);

    List<BankStatementRule> bankStatementRules;
    while (bankReconciliationLines.size() > 0) {
      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        if (bankReconciliationLine.getMoveLine() != null
            || bankReconciliationLine.getBankStatementLine() == null) {
          continue;
        }
        scriptContext =
            new Context(
                Mapper.toMap(bankReconciliationLine.getBankStatementLine()),
                BankStatementLineAFB120.class);
        bankStatementRules =
            bankStatementRuleRepository
                .all()
                .filter(
                    "self.ruleTypeSelect = :ruleTypeSelect"
                        + " AND self.accountManagement.interbankCodeLine = :interbankCodeLine"
                        + " AND self.accountManagement.company = :company"
                        + " AND self.accountManagement.bankDetails = :bankDetails")
                .bind("ruleTypeSelect", BankStatementRuleRepository.RULE_TYPE_ACCOUNTING_AUTO)
                .bind(
                    "interbankCodeLine",
                    bankReconciliationLine.getBankStatementLine().getOperationInterbankCodeLine())
                .bind("company", bankReconciliationLine.getBankReconciliation().getCompany())
                .bind("bankDetails", bankReconciliationLine.getBankStatementLine().getBankDetails())
                .fetch();

        for (BankStatementRule bankStatementRule : bankStatementRules) {

          if (bankStatementRule != null
              && bankStatementRule.getBankStatementQuery() != null
              && !Strings.isNullOrEmpty(bankStatementRule.getBankStatementQuery().getQuery())
              && Boolean.TRUE.equals(
                  new GroovyScriptHelper(scriptContext)
                      .eval(
                          bankStatementRule
                              .getBankStatementQuery()
                              .getQuery()
                              .replaceAll(
                                  "%s", "\"" + bankStatementRule.getSearchLabel() + "\"")))) {

            checkAccountBeforeAutoAccounting(bankStatementRule, bankReconciliation);

            if (bankStatementRule.getAccountManagement().getJournal() == null) {
              continue;
            }
            if (bankReconciliationLine.getBankStatementLine() != null
                && bankReconciliationLine.getBankStatementLine().getMoveLine() != null) {
              bankReconciliationLineService.reconcileBRLAndMoveLine(
                  bankReconciliationLine,
                  bankReconciliationLine.getBankStatementLine().getMoveLine());
              move = bankReconciliationLine.getBankStatementLine().getMoveLine().getMove();
            } else {
              move = generateMove(bankReconciliationLine, bankStatementRule);
              moveValidateService.accounting(move);
            }
            if (bankStatementRule.getLetterToInvoice()) {
              letterToInvoice(bankStatementRule, bankReconciliationLine, move);
            }
            break;
          }
        }
      }
      offset += limit;
      JPA.clear();
      bankReconciliationLines =
          bankReconciliationLineRepository
              .findByBankReconciliation(bankReconciliation)
              .fetch(limit, offset);
    }
  }

  protected void letterToInvoice(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine, Move move)
      throws AxelorException {

    MoveLine fetchedMoveLine =
        bankStatementRuleService
            .getMoveLine(bankStatementRule, bankReconciliationLine, move)
            .orElse(null);
    // Will reconcile move line that has as account the counterpart account specified in
    // bankstatementrule
    MoveLine generatedMoveLineToLetter =
        move.getMoveLineList().stream()
            .filter(
                moveLine -> moveLine.getAccount().equals(bankStatementRule.getCounterpartAccount()))
            .findFirst()
            .orElse(null);
    if (fetchedMoveLine != null && generatedMoveLineToLetter != null) {
      if (generatedMoveLineToLetter.getDebit().signum() > 0) {
        reconcileService.reconcile(generatedMoveLineToLetter, fetchedMoveLine, false, true);
      } else {
        reconcileService.reconcile(fetchedMoveLine, generatedMoveLineToLetter, false, true);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move generateMove(
      BankReconciliationLine bankReconciliationLine, BankStatementRule bankStatementRule)
      throws AxelorException {
    BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
    String description = "";
    if (bankStatementLine != null) {
      description = description.concat(bankStatementLine.getDescription());
    }
    description = StringHelper.cutTooLongString(description);

    if (!Strings.isNullOrEmpty(bankReconciliationLine.getReference())) {
      String reference = "ref:";
      reference =
          StringHelper.cutTooLongString(reference.concat(bankReconciliationLine.getReference()));
      description = StringHelper.cutTooLongStringWithOffset(description, reference.length());
      description = description.concat(reference);
    }
    AccountManagement accountManagement = bankStatementRule.getAccountManagement();
    Partner partner =
        bankStatementRuleService.getPartner(bankStatementRule, bankReconciliationLine).orElse(null);
    Move move =
        moveCreateService.createMove(
            accountManagement.getJournal(),
            accountManagement.getCompany(),
            bankStatementLine != null ? bankStatementLine.getCurrency() : null,
            partner,
            bankReconciliationLine.getEffectDate(),
            bankReconciliationLine.getEffectDate(),
            accountManagement.getPaymentMode(),
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            bankStatementLine != null ? bankStatementLine.getOrigin() : null,
            description,
            bankReconciliationLine.getBankReconciliation().getBankDetails());

    MoveLine counterPartMoveLine =
        generateMoveLine(bankReconciliationLine, bankStatementRule, move, true);

    MoveLine moveLine = generateMoveLine(bankReconciliationLine, bankStatementRule, move, false);

    generateTaxMoveLine(counterPartMoveLine, moveLine, bankStatementRule);

    bankReconciliationLineService.reconcileBRLAndMoveLine(bankReconciliationLine, moveLine);

    return moveRepository.save(move);
  }

  protected void generateTaxMoveLine(
      MoveLine counterPartMoveLine, MoveLine moveLine, BankStatementRule bankStatementRule)
      throws AxelorException {
    int vatSystemSelect = AccountRepository.VAT_SYSTEM_DEFAULT;
    Move move = counterPartMoveLine.getMove();
    Journal journal = move.getJournal();
    int journalTechnicalType = journal.getJournalType().getTechnicalTypeSelect();
    Company company = counterPartMoveLine.getMove().getCompany();
    Partner partner = null;

    if (journalTechnicalType == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
      partner = counterPartMoveLine.getPartner();
    } else if (journalTechnicalType == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
      partner = company.getPartner();
    }

    if (partner != null) {
      AccountingSituation accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(company, partner);
      if (accountingSituation != null && accountingSituation.getVatSystemSelect() != null) {
        vatSystemSelect = accountingSituation.getVatSystemSelect();
      }
    }

    Account counterPartAccount = bankStatementRule.getCounterpartAccount();
    if (vatSystemSelect == AccountRepository.VAT_SYSTEM_DEFAULT && counterPartAccount != null) {
      vatSystemSelect = counterPartAccount.getVatSystemSelect();
    }

    TaxLine taxLine = counterPartMoveLine.getTaxLine();
    Account account =
        taxAccountService.getAccount(
            taxLine != null ? taxLine.getTax() : null,
            company,
            journal,
            vatSystemSelect,
            false,
            move.getFunctionalOriginSelect());
    moveLineTaxService.autoTaxLineGenerate(move, account, false);

    fixTaxAmountRounding(move, counterPartMoveLine, moveLine);
  }

  protected void fixTaxAmountRounding(Move move, MoveLine counterPartMoveLine, MoveLine moveLine) {
    MoveLine taxMoveLine =
        move.getMoveLineList().stream()
            .filter(
                ml ->
                    ml.getAccount()
                        .getAccountType()
                        .getTechnicalTypeSelect()
                        .equals(AccountTypeRepository.TYPE_TAX))
            .findFirst()
            .orElse(null);
    if (taxMoveLine == null) {
      return;
    }
    BigDecimal taxAmount =
        moveLine
            .getDebit()
            .max(moveLine.getCredit())
            .subtract(counterPartMoveLine.getDebit().max(counterPartMoveLine.getCredit()))
            .abs();
    if (taxMoveLine.getDebit().signum() > 0) {
      taxMoveLine.setDebit(taxAmount);
    } else {
      taxMoveLine.setCredit(taxAmount);
    }
  }

  protected MoveLine generateMoveLine(
      BankReconciliationLine bankReconciliationLine,
      BankStatementRule bankStatementRule,
      Move move,
      boolean isCounterpartLine)
      throws AxelorException {
    MoveLine moveLine;
    LocalDate date = bankReconciliationLine.getEffectDate();
    BigDecimal debit;
    BigDecimal credit;
    Account account;
    String description = move.getDescription();
    String origin = move.getOrigin();
    TaxLine taxLine = null;
    if (isCounterpartLine) {
      debit = bankReconciliationLine.getDebit();
      credit = bankReconciliationLine.getCredit();
      account = bankStatementRule.getCounterpartAccount();
      if (account == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_RULE_COUNTERPART_ACCOUNT_MISSING),
            bankStatementRule.getSearchLabel());
      }
      if (account.getIsTaxRequiredOnMoveLine()) {
        if (bankStatementRule.getSpecificTax() == null) {
          taxLine = taxService.getTaxLine(account.getDefaultTax(), date);
        } else {
          taxLine = taxService.getTaxLine(bankStatementRule.getSpecificTax(), date);
        }
      }
    } else {
      debit = bankReconciliationLine.getCredit();
      credit = bankReconciliationLine.getDebit();
      account = bankStatementRule.getAccountManagement().getCashAccount();
      if (account == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_RULE_CASH_ACCOUNT_MISSING),
            bankStatementRule.getSearchLabel());
      }
    }

    boolean isDebit = debit.compareTo(credit) > 0;

    BigDecimal amount = debit.add(credit);
    if (taxLine != null) {
      BigDecimal taxRate = taxLine.getValue().divide(BigDecimal.valueOf(100));
      amount = amount.divide(BigDecimal.ONE.add(taxRate), RETURNED_SCALE, RoundingMode.HALF_UP);
    }

    moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            account,
            amount,
            isDebit,
            taxLine,
            date,
            move.getMoveLineList().size() + 1,
            origin,
            description);
    if (account.getHasAutomaticApplicationAccountingDate()) {
      moveLineService.applyCutOffDates(moveLine, move, date, date);
      moveLine.setIsCutOffGenerated(true);
    }
    if (account.getAnalyticDistributionRequiredOnMoveLines()) {
      if (account.getAnalyticDistributionTemplate() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(
                BankPaymentExceptionMessage
                    .BANK_RECONCILIATION_NO_DISTRIBUTION_GENERATED_MOVE_LINE),
            account.getCode());
      }
      moveLine.setAnalyticDistributionTemplate(account.getAnalyticDistributionTemplate());
    }
    move.addMoveLineListItem(moveLine);
    return moveLine;
  }

  @Override
  public void checkAccountBeforeAutoAccounting(
      BankStatementRule bankStatementRule, BankReconciliation bankReconciliation)
      throws AxelorException {
    if (bankStatementRule.getAccountManagement() != null
        && bankStatementRule.getAccountManagement().getCashAccount() != null
        && bankReconciliation.getBankDetails() != null
        && bankReconciliation.getBankDetails().getBankAccount() != null
        && !bankStatementRule
            .getAccountManagement()
            .getCashAccount()
            .equals(bankReconciliation.getBankDetails().getBankAccount())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BankPaymentExceptionMessage.BANK_ACCOUNT_DIFFERENT_THAN_CASH_ACCOUNT),
          bankReconciliation.getBankDetails().getIbanBic(),
          bankReconciliation.getBankDetails().getBankAccount().getCode(),
          bankStatementRule.getAccountManagement().getName(),
          bankStatementRule.getAccountManagement().getCashAccount().getCode());
    }
  }
}
