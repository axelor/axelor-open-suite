/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
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
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.text.GroovyTemplates;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BankReconciliationMoveGenerationServiceImpl
    implements BankReconciliationMoveGenerationService {

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
  protected CurrencyScaleService currencyScaleService;
  protected MoveLineToolService moveLineToolService;

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
      MoveLineService moveLineService,
      CurrencyScaleService currencyScaleService,
      MoveLineToolService moveLineToolService) {
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
    this.currencyScaleService = currencyScaleService;
    this.moveLineToolService = moveLineToolService;
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
                .bind("company", bankReconciliation.getCompany())
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

            MoveLine moveLine =
                Optional.of(bankReconciliationLine)
                    .map(BankReconciliationLine::getBankStatementLine)
                    .map(BankStatementLine::getMoveLine)
                    .orElse(null);
            if (moveLine != null) {
              bankReconciliationLineService.reconcileBRLAndMoveLine(
                  bankReconciliationLine, moveLine);
              move = moveLine.getMove();
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
        if (bankReconciliationLine.getMoveLine() == null
            && bankReconciliationLine.getAccount() != null
            && bankReconciliation.getCashAccount() != null
            && bankReconciliation.getJournal() != null) {
          move = generateMove(bankReconciliationLine, null);
          moveValidateService.accounting(move);
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

    BankReconciliation bankReconciliation = bankReconciliationLine.getBankReconciliation();

    Move move =
        generateMove(
            bankReconciliation, bankReconciliationLine, bankStatementLine, bankStatementRule);

    LocalDate originDate =
        Optional.ofNullable(bankStatementLine)
            .map(BankStatementLine::getOperationDate)
            .orElse(move.getDate());

    MoveLine counterPartMoveLine =
        generateMoveLine(bankReconciliationLine, bankStatementRule, move, true, originDate);

    MoveLine moveLine =
        generateMoveLine(bankReconciliationLine, bankStatementRule, move, false, originDate);

    generateTaxMoveLine(counterPartMoveLine, moveLine);

    bankReconciliationLineService.reconcileBRLAndMoveLine(bankReconciliationLine, moveLine);

    return moveRepository.save(move);
  }

  protected String computeOrigin(
      BankReconciliationLine bankReconciliationLine,
      BankStatementLine bankStatementLine,
      String rule) {
    try {
      Object origin = computeLabel(bankReconciliationLine, rule);
      if (ObjectUtils.notEmpty(origin)) {
        return origin.toString();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return bankStatementLine != null ? bankStatementLine.getOrigin() : null;
  }

  protected String computeDescription(
      BankReconciliationLine bankReconciliationLine,
      BankStatementLine bankStatementLine,
      String rule) {
    String description = "";

    try {
      Object desc = computeLabel(bankReconciliationLine, rule);
      if (ObjectUtils.notEmpty(desc)) {
        return desc.toString();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

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
    return description;
  }

  protected Object computeLabel(Model model, String rule) {
    if (StringUtils.isEmpty(rule)) {
      return null;
    }
    Context scriptContext = new Context(Mapper.toMap(model), model.getClass());
    return Beans.get(GroovyTemplates.class).fromText(rule).make(scriptContext).render();
  }

  protected void generateTaxMoveLine(MoveLine counterPartMoveLine, MoveLine moveLine)
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

    Account counterPartAccount = counterPartMoveLine.getAccount();
    if (vatSystemSelect == AccountRepository.VAT_SYSTEM_DEFAULT && counterPartAccount != null) {
      vatSystemSelect = counterPartAccount.getVatSystemSelect();
    }

    Set<TaxLine> taxLineSet = counterPartMoveLine.getTaxLineSet();
    if (ObjectUtils.notEmpty(taxLineSet)) {
      for (TaxLine taxLine : taxLineSet) {
        Account account =
            taxAccountService.getAccount(
                taxLine != null ? taxLine.getTax() : null,
                company,
                journal,
                moveLine.getAccount(),
                vatSystemSelect,
                false,
                move.getFunctionalOriginSelect());
        moveLineTaxService.autoTaxLineGenerate(move, account, false);
      }
    }

    fixTaxAmountRounding(move, counterPartMoveLine, moveLine);
  }

  protected void fixTaxAmountRounding(Move move, MoveLine counterPartMoveLine, MoveLine moveLine) {
    MoveLine taxMoveLine =
        move.getMoveLineList().stream()
            .filter(moveLineToolService::isMoveLineTaxAccount)
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
      taxMoveLine.setDebit(currencyScaleService.getScaledValue(move, taxAmount));
    } else {
      taxMoveLine.setCredit(currencyScaleService.getScaledValue(move, taxAmount));
    }
  }

  protected MoveLine generateMoveLine(
      BankReconciliationLine bankReconciliationLine,
      BankStatementRule bankStatementRule,
      Move move,
      boolean isCounterpartLine,
      LocalDate originDate)
      throws AxelorException {
    MoveLine moveLine;
    LocalDate date = bankReconciliationLine.getEffectDate();
    BigDecimal debit;
    BigDecimal credit;
    Account account = bankReconciliationLine.getAccount();
    String description = move.getDescription();
    String origin = move.getOrigin();
    Set<TaxLine> taxLineSet = new HashSet<>();
    if (isCounterpartLine) {
      debit =
          currencyScaleService.getScaledValue(
              bankReconciliationLine, bankReconciliationLine.getDebit());
      credit =
          currencyScaleService.getScaledValue(
              bankReconciliationLine, bankReconciliationLine.getCredit());

      if (account == null && bankStatementRule != null) {
        account = bankStatementRule.getCounterpartAccount();
      }
      if (account == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_RULE_COUNTERPART_ACCOUNT_MISSING),
            bankStatementRule.getSearchLabel());
      }
      if (account.getIsTaxRequiredOnMoveLine()) {
        if (bankStatementRule == null || bankStatementRule.getSpecificTax() == null) {
          taxLineSet = taxService.getTaxLineSet(account.getDefaultTaxSet(), date);
        } else {
          Sets.newHashSet(taxService.getTaxLine(bankStatementRule.getSpecificTax(), date));
        }
      }
    } else {
      debit =
          currencyScaleService.getScaledValue(
              bankReconciliationLine, bankReconciliationLine.getCredit());
      credit =
          currencyScaleService.getScaledValue(
              bankReconciliationLine, bankReconciliationLine.getDebit());

      account =
          Optional.of(bankReconciliationLine)
              .map(BankReconciliationLine::getBankReconciliation)
              .map(BankReconciliation::getCashAccount)
              .orElse(null);

      if (account == null && bankStatementRule != null) {
        account = bankStatementRule.getAccountManagement().getCashAccount();
        if (account == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_RULE_CASH_ACCOUNT_MISSING),
              bankStatementRule.getSearchLabel());
        }
      }
    }

    boolean isDebit = debit.compareTo(credit) > 0;

    BigDecimal amount =
        currencyScaleService.getScaledValue(bankReconciliationLine, debit.add(credit));
    if (ObjectUtils.notEmpty(taxLineSet)) {
      BigDecimal taxRate = taxService.getTotalTaxRate(taxLineSet);
      amount =
          amount.divide(
              BigDecimal.ONE.add(taxRate),
              currencyScaleService.getScale(bankReconciliationLine),
              RoundingMode.HALF_UP);
    }

    moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            account,
            amount,
            isDebit,
            taxLineSet,
            date,
            move.getMoveLineList().size() + 1,
            origin,
            description);
    moveLine.setOriginDate(originDate);

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

  protected Move generateMove(
      BankReconciliation bankReconciliation,
      BankReconciliationLine bankReconciliationLine,
      BankStatementLine bankStatementLine,
      BankStatementRule bankStatementRule)
      throws AxelorException {
    Map<String, Object> moveFieldsMap =
        initializeMoveFields(
            bankReconciliation, bankReconciliationLine, bankStatementLine, bankStatementRule);

    LocalDate effectDate = (LocalDate) moveFieldsMap.get("effectDate");
    Move move =
        moveCreateService.createMove(
            (Journal) moveFieldsMap.get("journal"),
            (Company) moveFieldsMap.get("company"),
            (Currency) moveFieldsMap.get("currency"),
            (Partner) moveFieldsMap.get("partner"),
            effectDate,
            effectDate,
            (PaymentMode) moveFieldsMap.get("paymentMode"),
            (FiscalPosition) moveFieldsMap.get("fiscalPosition"),
            (Integer) moveFieldsMap.get("technicalOriginSelect"),
            (Integer) moveFieldsMap.get("functionalOriginSelect"),
            (String) moveFieldsMap.get("origin"),
            (String) moveFieldsMap.get("description"),
            (BankDetails) moveFieldsMap.get("companyBankDetails"));
    move.setPaymentCondition(null);

    return move;
  }

  protected Map<String, Object> initializeMoveFields(
      BankReconciliation bankReconciliation,
      BankReconciliationLine bankReconciliationLine,
      BankStatementLine bankStatementLine,
      BankStatementRule bankStatementRule)
      throws AxelorException {
    Map<String, Object> moveFieldMap = new HashMap<>();
    if (bankReconciliationLine == null) {
      return moveFieldMap;
    }

    Company company = null;
    Journal journal = null;
    PaymentMode paymentMode = null;
    String description = "";
    String origin = bankReconciliationLine.getReference();
    BankDetails companyBankDetails = null;
    LocalDate effectDate = bankReconciliationLine.getEffectDate();
    Currency currency = null;
    FiscalPosition fiscalPosition = null;
    Partner partner = bankReconciliationLine.getPartner();

    if (bankReconciliation != null) {
      company = bankReconciliation.getCompany();
      journal = bankReconciliation.getJournal();
      description = bankReconciliation.getName();
      currency = bankReconciliation.getCurrency();
      companyBankDetails = bankReconciliation.getBankDetails();
    }

    if (bankStatementRule != null) {
      if (bankStatementRule.getAccountManagement() != null) {
        AccountManagement accountManagement = bankStatementRule.getAccountManagement();

        paymentMode = accountManagement.getPaymentMode();
        if (company == null) {
          company = accountManagement.getCompany();
        }
        if (journal == null) {
          journal = accountManagement.getJournal();
        }
      }
      if (partner == null) {
        partner =
            bankStatementRuleService
                .getPartner(bankStatementRule, bankReconciliationLine)
                .orElse(null);
      }

      description =
          computeDescription(
              bankReconciliationLine,
              bankStatementLine,
              bankStatementRule.getEntryDescriptionComputation());
      origin =
          computeOrigin(
              bankReconciliationLine,
              bankStatementLine,
              bankStatementRule.getEntryOriginComputation());
    }

    if (bankStatementLine != null && currency == null) {
      currency = bankStatementLine.getCurrency();
    }

    if (partner != null) {
      fiscalPosition = partner.getFiscalPosition();
    }

    moveFieldMap.put("journal", journal);
    moveFieldMap.put("company", company);
    moveFieldMap.put("currency", currency);
    moveFieldMap.put("partner", partner);
    moveFieldMap.put("effectDate", effectDate);
    moveFieldMap.put("paymentMode", paymentMode);
    moveFieldMap.put("fiscalPosition", fiscalPosition);
    moveFieldMap.put("technicalOriginSelect", MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);
    moveFieldMap.put("functionalOriginSelect", MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT);
    moveFieldMap.put("origin", origin);
    moveFieldMap.put("description", description);
    moveFieldMap.put("companyBankDetails", companyBankDetails);
    return moveFieldMap;
  }

  protected void manageDynamicSearchOnMoveLines(BankReconciliationLine bankReconciliationLine)
      throws AxelorException {
    if (bankReconciliationLine == null
        || bankReconciliationLine.getBankStatementLine() == null
        || bankReconciliationLine.getBankReconciliation() == null) {
      return;
    }

    List<MoveLine> fetchedMoveLineList = getMoveLineFetchedByMoveLines(bankReconciliationLine);
    if (ObjectUtils.isEmpty(fetchedMoveLineList)) {
      return;
    }

    Move move =
        generateMove(
            bankReconciliationLine.getBankReconciliation(),
            bankReconciliationLine,
            bankReconciliationLine.getBankStatementLine(),
            null);
  }

  protected List<MoveLine> getMoveLineFetchedByMoveLines(
      BankReconciliationLine bankReconciliationLine) throws AxelorException {
    List<MoveLine> fetchedMoveLineList = new ArrayList<>();
    List<BankStatementRule> bankStatementRuleList =
        bankStatementRuleRepository
            .all()
            .filter(
                "self.ruleTypeSelect = :ruleTypeSelect"
                    + " AND self.accountManagement.interbankCodeLine = :interbankCodeLine"
                    + " AND self.accountManagement.company = :company"
                    + " AND self.accountManagement.bankDetails = :bankDetails")
            .bind("ruleTypeSelect", BankStatementRuleRepository.RULE_TYPE_MOVE_LINE_FETCHING)
            .bind(
                "partnerFetchMethodSelect",
                BankStatementRuleRepository.PARTNER_FETCH_METHOD_MOVE_LINE)
            .bind(
                "company",
                Optional.of(bankReconciliationLine)
                    .map(BankReconciliationLine::getBankReconciliation)
                    .map(BankReconciliation::getCompany)
                    .orElse(null))
            .bind(
                "bankDetails",
                Optional.of(bankReconciliationLine)
                    .map(BankReconciliationLine::getBankStatementLine)
                    .map(BankStatementLine::getBankDetails)
                    .orElse(null))
            .fetch();

    if (ObjectUtils.isEmpty(bankStatementRuleList)) {
      return fetchedMoveLineList;
    }

    for (BankStatementRule bankStatementRule : bankStatementRuleList) {
      MoveLine fetchedMoveLine =
          bankStatementRuleService
              .getMoveLine(bankStatementRule, bankReconciliationLine, null)
              .orElse(null);
      if (fetchedMoveLine != null) {
        fetchedMoveLineList.add(fetchedMoveLine);
      }
    }

    return fetchedMoveLineList;
  }
}
