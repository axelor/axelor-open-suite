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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.db.Query;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class AccountingCutOffServiceImpl implements AccountingCutOffService {

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected MoveToolService moveToolService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected TaxAccountService taxAccountService;
  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected MoveRepository moveRepository;
  protected MoveValidateService moveValidateService;
  protected UnitConversionService unitConversionService;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected ReconcileService reconcileService;
  protected AccountConfigService accountConfigService;
  protected MoveSimulateService moveSimulateService;
  protected MoveLineService moveLineService;
  protected CurrencyService currencyService;
  protected TaxAccountToolService taxAccountToolService;
  protected int counter = 0;

  @Inject
  public AccountingCutOffServiceImpl(
      MoveCreateService moveCreateService,
      MoveToolService moveToolService,
      AccountManagementAccountService accountManagementAccountService,
      TaxAccountService taxAccountService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      MoveRepository moveRepository,
      MoveValidateService moveValidateService,
      UnitConversionService unitConversionService,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      ReconcileService reconcileService,
      AccountConfigService accountConfigService,
      MoveLineCreateService moveLineCreateService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveSimulateService moveSimulateService,
      MoveLineService moveLineService,
      CurrencyService currencyService,
      TaxAccountToolService taxAccountToolService) {

    this.moveCreateService = moveCreateService;
    this.moveToolService = moveToolService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.taxAccountService = taxAccountService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.moveRepository = moveRepository;
    this.moveValidateService = moveValidateService;
    this.unitConversionService = unitConversionService;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.reconcileService = reconcileService;
    this.accountConfigService = accountConfigService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.moveSimulateService = moveSimulateService;
    this.moveLineService = moveLineService;
    this.currencyService = currencyService;
    this.taxAccountToolService = taxAccountToolService;
  }

  @Override
  public Query<Move> getMoves(
      Company company,
      Journal researchJournal,
      LocalDate moveDate,
      int accountingCutOffTypeSelect) {
    String queryStr =
        "((:researchJournal > 0 AND self.journal.id = :researchJournal) "
            + "  OR (:researchJournal = 0 AND self.journal.journalType.technicalTypeSelect = :journalType))"
            + "AND self.date <= :date "
            + "AND self.statusSelect IN (2, 3, 5) "
            + "AND EXISTS(SELECT 1 FROM MoveLine ml "
            + " WHERE ml.move = self "
            + " AND ml.account.manageCutOffPeriod IS TRUE "
            + " AND ml.cutOffStartDate != null AND ml.cutOffEndDate != null "
            + " AND ml.cutOffEndDate > :date)";

    if (company != null) {
      queryStr += " AND self.company = :company";
    }

    Query<Move> moveQuery =
        moveRepository
            .all()
            .filter(queryStr)
            .bind("researchJournal", researchJournal == null ? 0 : researchJournal.getId())
            .bind(
                "journalType",
                accountingCutOffTypeSelect
                        == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_PREPAID_EXPENSES
                    ? JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
                    : JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)
            .bind("date", moveDate);

    if (company != null) {
      moveQuery.bind("company", company.getId());
    }

    return moveQuery.order("id");
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Move> generateCutOffMovesFromMove(
      Move move,
      Journal journal,
      LocalDate moveDate,
      LocalDate reverseMoveDate,
      String moveDescription,
      String reverseMoveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean automaticReverse,
      boolean automaticReconcile,
      String prefixOrigin)
      throws AxelorException {
    List<Move> cutOffMoveList = new ArrayList<>();

    Move cutOffMove =
        this.generateCutOffMoveFromMove(
            move,
            journal,
            moveDate,
            moveDate,
            moveDescription,
            accountingCutOffTypeSelect,
            cutOffMoveStatusSelect,
            false,
            prefixOrigin);

    if (cutOffMove == null) {
      return null;
    }

    cutOffMoveList.add(cutOffMove);

    if (automaticReverse) {
      Move reverseCutOffMove =
          this.generateCutOffMoveFromMove(
              move,
              journal,
              reverseMoveDate,
              moveDate,
              reverseMoveDescription,
              accountingCutOffTypeSelect,
              cutOffMoveStatusSelect,
              true,
              prefixOrigin);

      if (reverseCutOffMove == null) {
        return null;
      }

      cutOffMoveList.add(reverseCutOffMove);

      if (automaticReconcile && cutOffMoveStatusSelect != MoveRepository.STATUS_SIMULATED) {
        reconcile(cutOffMove, reverseCutOffMove);
      }
    }

    if (!move.getCutOffMoveGenerated()) {
      move.setCutOffMoveGenerated(true);
    }

    return cutOffMoveList;
  }

  @Override
  public Move generateCutOffMoveFromMove(
      Move move,
      Journal journal,
      LocalDate moveDate,
      LocalDate originMoveDate,
      String moveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean isReverse,
      String prefixOrigin)
      throws AxelorException {
    Company company = move.getCompany();
    Partner partner = move.getPartner();
    LocalDate originDate = move.getOriginDate();
    String origin = prefixOrigin + move.getReference();

    Move cutOffMove =
        moveCreateService.createMove(
            journal,
            company,
            move.getCurrency(),
            partner,
            moveDate,
            originDate,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_CUT_OFF,
            origin,
            moveDescription,
            move.getCompanyBankDetails());

    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      this.generateMoveLinesFromMove(
          move,
          cutOffMove,
          company,
          partner,
          moveDate,
          originMoveDate,
          originDate,
          origin,
          moveDescription,
          accountingCutOffTypeSelect,
          isReverse);
    }

    // Status
    if (CollectionUtils.isNotEmpty(cutOffMove.getMoveLineList())) {

      cutOffMove.setCutOffOriginMove(move);
      this.updateStatus(cutOffMove, cutOffMoveStatusSelect);
    } else {
      moveRepository.remove(cutOffMove);
      return null;
    }

    return cutOffMove;
  }

  protected void updateStatus(Move move, int cutOffMoveStatusSelect) throws AxelorException {
    switch (cutOffMoveStatusSelect) {
      case MoveRepository.STATUS_SIMULATED:
        moveSimulateService.simulate(move);
        break;
      case MoveRepository.STATUS_ACCOUNTED:
        moveValidateService.checkPreconditions(move);
        move.setStatusSelect(MoveRepository.STATUS_DAYBOOK);
        moveValidateService.accounting(move);
        break;
      case MoveRepository.STATUS_DAYBOOK:
        moveValidateService.accounting(move);
        break;
    }
  }

  protected void generateMoveLinesFromMove(
      Move move,
      Move cutOffMove,
      Company company,
      Partner partner,
      LocalDate moveDate,
      LocalDate originMoveDate,
      LocalDate originDate,
      String origin,
      String moveDescription,
      int accountingCutOffTypeSelect,
      boolean isReverse)
      throws AxelorException {
    Account moveLineAccount;
    BigDecimal amountInCurrency;
    MoveLine cutOffMoveLine;
    Map<Account, MoveLine> cutOffMoveLineMap = new HashMap<>();

    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(
            move.getCurrency(), move.getCompanyCurrency(), moveDate);

    // Sorting so that move lines with analytic move lines are computed first
    List<MoveLine> sortedMoveLineList = new ArrayList<>(move.getMoveLineList());
    sortedMoveLineList.sort(
        (t1, t2) -> {
          if ((CollectionUtils.isNotEmpty(t1.getAnalyticMoveLineList())
                  && CollectionUtils.isNotEmpty(t2.getAnalyticMoveLineList()))
              || (CollectionUtils.isEmpty(t1.getAnalyticMoveLineList())
                  && CollectionUtils.isEmpty(t2.getAnalyticMoveLineList()))) {
            return 0;
          } else if (CollectionUtils.isNotEmpty(t1.getAnalyticMoveLineList())) {
            return -1;
          } else {
            return 1;
          }
        });

    for (MoveLine moveLine : sortedMoveLineList) {
      if (moveLine.getAccount().getManageCutOffPeriod()
          && moveLine.getCutOffStartDate() != null
          && moveLine.getCutOffEndDate() != null
          && (moveLine.getCutOffEndDate().isAfter(moveDate) || isReverse)) {
        moveLineAccount = moveLine.getAccount();
        amountInCurrency = moveLineService.getCutOffProrataAmount(moveLine, originMoveDate);
        BigDecimal convertedAmount =
            currencyService.getAmountCurrencyConvertedUsingExchangeRate(
                amountInCurrency, currencyRate);

        // Check if move line already exists with that account
        if (cutOffMoveLineMap.containsKey(moveLineAccount)) {
          cutOffMoveLine = cutOffMoveLineMap.get(moveLineAccount);
          cutOffMoveLine.setCurrencyAmount(
              cutOffMoveLine.getCurrencyAmount().add(amountInCurrency));
          if (isReverse
              != (accountingCutOffTypeSelect
                  == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_DEFERRED_INCOMES)) {
            cutOffMoveLine.setDebit(cutOffMoveLine.getDebit().add(convertedAmount));
          } else {
            cutOffMoveLine.setCredit(cutOffMoveLine.getCredit().add(convertedAmount));
          }

        } else {
          cutOffMoveLine =
              moveLineCreateService.createMoveLine(
                  cutOffMove,
                  partner,
                  moveLineAccount,
                  amountInCurrency,
                  isReverse
                      != (accountingCutOffTypeSelect
                          == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_DEFERRED_INCOMES),
                  cutOffMove.getDate(),
                  ++counter,
                  origin,
                  moveDescription);
          cutOffMoveLine.setTaxLine(moveLine.getTaxLine());
          cutOffMoveLine.setOriginDate(originDate);

          cutOffMoveLineMap.put(moveLineAccount, cutOffMoveLine);
        }

        List<AnalyticMoveLine> analyticMoveLineList =
            CollectionUtils.isEmpty(cutOffMoveLine.getAnalyticMoveLineList())
                ? new ArrayList<>()
                : new ArrayList<>(cutOffMoveLine.getAnalyticMoveLineList());
        cutOffMoveLine.clearAnalyticMoveLineList();

        // Copy analytic move lines
        this.copyAnalyticMoveLines(moveLine, cutOffMoveLine, amountInCurrency);

        if (CollectionUtils.isEmpty(cutOffMoveLine.getAnalyticMoveLineList())) {
          cutOffMoveLine.setAnalyticMoveLineList(analyticMoveLineList);
        }
      }
    }

    cutOffMoveLineMap.values().forEach(cutOffMove::addMoveLineListItem);

    // Partner move line
    Account account =
        accountConfigService.getPartnerAccount(
            company.getAccountConfig(), accountingCutOffTypeSelect);

    this.generatePartnerMoveLine(cutOffMove, origin, account, moveDescription, originDate);
    counter = 0;
  }

  protected void copyAnalyticMoveLines(
      MoveLine moveLine, MoveLine cutOffMoveLine, BigDecimal newAmount) {
    if (CollectionUtils.isNotEmpty(moveLine.getAnalyticMoveLineList())) {
      if (CollectionUtils.isNotEmpty(cutOffMoveLine.getAnalyticMoveLineList())) {
        AnalyticMoveLine existingAnalyticMoveLine;
        List<AnalyticMoveLine> toComputeAnalyticMoveLineList =
            new ArrayList<>(cutOffMoveLine.getAnalyticMoveLineList());

        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
          existingAnalyticMoveLine =
              this.getExistingAnalyticMoveLine(cutOffMoveLine, analyticMoveLine);

          if (existingAnalyticMoveLine == null) {
            this.copyAnalyticMoveLine(cutOffMoveLine, analyticMoveLine, newAmount);
          } else {
            this.computeAnalyticMoveLine(
                cutOffMoveLine,
                existingAnalyticMoveLine,
                analyticMoveLine.getPercentage(),
                newAmount,
                false);

            toComputeAnalyticMoveLineList.remove(existingAnalyticMoveLine);
          }
        }

        for (AnalyticMoveLine toComputeAnalyticMoveLine : toComputeAnalyticMoveLineList) {
          this.computeAnalyticMoveLine(
              cutOffMoveLine, toComputeAnalyticMoveLine, BigDecimal.ZERO, newAmount, false);
        }
      } else {
        if (cutOffMoveLine.getAnalyticMoveLineList() == null) {
          cutOffMoveLine.setAnalyticMoveLineList(new ArrayList<>());
        }

        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
          this.copyAnalyticMoveLine(cutOffMoveLine, analyticMoveLine, newAmount);
        }
      }
    } else if (CollectionUtils.isNotEmpty(cutOffMoveLine.getAnalyticMoveLineList())) {
      for (AnalyticMoveLine analyticMoveLine : cutOffMoveLine.getAnalyticMoveLineList()) {
        this.computeAnalyticMoveLine(
            cutOffMoveLine, analyticMoveLine, analyticMoveLine.getPercentage(), newAmount, false);
      }
    }
  }

  protected AnalyticMoveLine getExistingAnalyticMoveLine(
      MoveLine moveLine, AnalyticMoveLine analyticMoveLine) {
    return moveLine.getAnalyticMoveLineList().stream()
        .filter(
            it ->
                it.getAnalyticAxis().equals(analyticMoveLine.getAnalyticAxis())
                    && it.getAnalyticAccount().equals(analyticMoveLine.getAnalyticAccount()))
        .findFirst()
        .orElse(null);
  }

  protected void copyAnalyticMoveLine(
      MoveLine moveLine, AnalyticMoveLine analyticMoveLine, BigDecimal newAmount) {
    AnalyticMoveLine analyticMoveLineCopy =
        analyticMoveLineRepository.copy(analyticMoveLine, false);

    this.computeAnalyticMoveLine(
        moveLine, analyticMoveLineCopy, analyticMoveLineCopy.getPercentage(), newAmount, true);

    moveLine.addAnalyticMoveLineListItem(analyticMoveLineCopy);
  }

  protected void computeAnalyticMoveLine(
      MoveLine moveLine,
      AnalyticMoveLine analyticMoveLine,
      BigDecimal newPercentage,
      BigDecimal newAmount,
      boolean newLine) {
    BigDecimal amount =
        newAmount.multiply(newPercentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));

    if (!newLine) {
      amount = analyticMoveLine.getAmount().add(amount);
    }

    BigDecimal percentage =
        amount
            .multiply(BigDecimal.valueOf(100))
            .divide(moveLine.getCurrencyAmount(), 2, RoundingMode.HALF_UP);

    analyticMoveLine.setPercentage(percentage);
    analyticMoveLine.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
  }

  protected void generateTaxMoveLine(
      Move move,
      MoveLine productMoveLine,
      String origin,
      boolean isPurchase,
      String moveDescription)
      throws AxelorException {

    TaxLine taxLine = productMoveLine.getTaxLine();

    Tax tax = taxLine.getTax();

    Account taxAccount =
        taxAccountService.getVatRegulationAccount(tax, move.getCompany(), isPurchase);

    BigDecimal currencyTaxAmount =
        InvoiceLineManagement.computeAmount(
            productMoveLine.getCurrencyAmount(), taxLine.getValue().divide(new BigDecimal(100)));
    Integer vatSystem =
        taxAccountToolService.calculateVatSystem(
            move.getPartner(),
            move.getCompany(),
            productMoveLine.getAccount(),
            isPurchase,
            !isPurchase);

    MoveLine moveLine = this.getMoveLineWithSameTax(move, taxAccount, taxLine, vatSystem);

    if (moveLine != null && (moveLine.getDebit().compareTo(new BigDecimal(0)) > 0)) {
      moveLine.setDebit(moveLine.getDebit().add(currencyTaxAmount));
    } else if (moveLine != null) {
      moveLine.setCredit(moveLine.getCredit().add(currencyTaxAmount));
    } else {
      MoveLine taxMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              move.getPartner(),
              taxAccount,
              currencyTaxAmount,
              productMoveLine.getDebit().signum() > 0,
              move.getDate(),
              ++counter,
              origin,
              moveDescription);

      if (taxLine != null) {
        taxMoveLine.setTaxLine(taxLine);
        taxMoveLine.setTaxRate(taxLine.getValue());
        taxMoveLine.setTaxCode(taxLine.getTax().getCode());
        taxMoveLine.setVatSystemSelect(vatSystem);
      }

      taxMoveLine.setOriginDate(productMoveLine.getOriginDate());

      move.addMoveLineListItem(taxMoveLine);
    }
  }

  protected MoveLine generatePartnerMoveLine(
      Move move, String origin, Account account, String moveDescription, LocalDate originDate)
      throws AxelorException {
    LocalDate moveDate = move.getDate();

    BigDecimal currencyBalance = moveToolService.getBalanceCurrencyAmount(move.getMoveLineList());
    BigDecimal balance = moveToolService.getBalanceAmount(move.getMoveLineList());

    if (balance.signum() == 0) {
      return null;
    }

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            account,
            currencyBalance.abs(),
            balance.abs(),
            null,
            balance.signum() < 0,
            moveDate,
            moveDate,
            originDate,
            ++counter,
            origin,
            moveDescription);
    move.addMoveLineListItem(moveLine);

    return moveLine;
  }

  protected void getAndComputeAnalyticDistribution(
      Product product, Move move, MoveLine moveLine, boolean isPurchase) throws AxelorException {

    if (accountConfigService.getAccountConfig(move.getCompany()).getAnalyticDistributionTypeSelect()
        == AccountConfigRepository.DISTRIBUTION_TYPE_FREE) {
      return;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            move.getPartner(), product, move.getCompany(), isPurchase);

    moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    List<AnalyticMoveLine> analyticMoveLineList =
        moveLineComputeAnalyticService
            .createAnalyticDistributionWithTemplate(moveLine)
            .getAnalyticMoveLineList();
    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      analyticMoveLine.setMoveLine(moveLine);
    }
    analyticMoveLineList.stream().forEach(analyticMoveLineRepository::save);
  }

  protected void reconcile(Move move, Move reverseMove) throws AxelorException {
    List<MoveLine> moveLineList = Lists.newArrayList(move.getMoveLineList());
    moveLineList.addAll(reverseMove.getMoveLineList());
    moveLineService.reconcileMoveLines(moveLineList);
  }

  protected MoveLine getMoveLineWithSameTax(
      Move move, Account taxAccount, TaxLine taxLine, Integer vatSystem) {
    if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      for (MoveLine line : move.getMoveLineList()) {
        if (line.getAccount().equals(taxAccount)
            && line.getTaxLine() != null
            && line.getTaxLine().equals(taxLine)
            && line.getVatSystemSelect() != null
            && line.getVatSystemSelect().equals(vatSystem)) {
          return line;
        }
      }
    }
    return null;
  }
}
