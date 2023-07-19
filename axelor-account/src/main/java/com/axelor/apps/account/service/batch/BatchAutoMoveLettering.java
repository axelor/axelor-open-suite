/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchAutoMoveLettering extends BatchStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountingBatchRepository accountingBatchRepository;
  protected MoveLineRepository moveLineRepository;
  protected CompanyRepository companyRepository;

  protected MoveLineControlService moveLineControlService;
  protected PaymentService paymentService;
  protected ReconcileService reconcileService;
  protected ReconcileGroupService reconcileGroupService;

  protected AccountingBatch accountingBatch;
  protected Set<MoveLine> moveLineReconciledSet;

  @Inject
  public BatchAutoMoveLettering(
      AccountingBatchRepository accountingBatchRepository,
      MoveLineRepository moveLineRepository,
      CompanyRepository companyRepository,
      MoveLineControlService moveLineControlService,
      PaymentService paymentService,
      ReconcileService reconcileService,
      ReconcileGroupService reconcileGroupService) {
    super();
    this.accountingBatchRepository = accountingBatchRepository;
    this.moveLineRepository = moveLineRepository;
    this.companyRepository = companyRepository;
    this.moveLineControlService = moveLineControlService;
    this.paymentService = paymentService;
    this.reconcileService = reconcileService;
    this.reconcileGroupService = reconcileGroupService;
  }

  @Override
  protected void process() {
    accountingBatch = batch.getAccountingBatch();
    moveLineReconciledSet = new HashSet<>();

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap = getMoveLinesMap();

    int reconcileMethodSelect = accountingBatch.getReconcileMethodSelect();

    for (Pair<List<MoveLine>, List<MoveLine>> moveLineLists : moveLineMap.values()) {

      List<MoveLine> companyPartnerCreditMoveLineList =
          moveLineLists.getLeft().stream()
              .filter(moveLine -> moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)
              .collect(Collectors.toList());
      List<MoveLine> companyPartnerDebitMoveLineList =
          moveLineLists.getRight().stream()
              .filter(moveLine -> moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)
              .collect(Collectors.toList());

      if (CollectionUtils.isEmpty(companyPartnerCreditMoveLineList)
          || CollectionUtils.isEmpty(companyPartnerDebitMoveLineList)) {
        continue;
      }

      if (reconcileMethodSelect
          == AccountingBatchRepository.AUTO_MOVE_LETTERING_RECONCILE_BY_BALANCED_MOVE) {
        List<MoveLine> moveLines =
            Stream.of(companyPartnerDebitMoveLineList, companyPartnerCreditMoveLineList)
                .flatMap(Collection::stream)
                .sorted(getMoveLineComparator())
                .collect(Collectors.toList());
        reconcileWithBalancedMove(moveLines);
      } else {
        reconcileWithMethod(
            companyPartnerDebitMoveLineList,
            companyPartnerCreditMoveLineList,
            reconcileMethodSelect);
      }
    }
    for (MoveLine moveLine : moveLineReconciledSet) {
      incrementDone();
    }
  }

  protected void reconcileWithBalancedMove(List<MoveLine> moveLines) {

    List<MoveLine> debitMoveLines;
    List<MoveLine> creditMoveLines;
    List<MoveLine> moveLinesReconciled = new ArrayList<>();
    BigDecimal progressiveAmount;
    for (int i = 0; i < moveLines.size(); i++) {
      List<MoveLine> moveLinesToProcess = new ArrayList<>();
      progressiveAmount = BigDecimal.ZERO;
      for (MoveLine moveLine : moveLines.subList(i, moveLines.size())) {
        if (moveLinesReconciled.contains(moveLine)) {
          break;
        }
        moveLinesToProcess.add(moveLine);
        if (moveLine.getDebit().signum() > 0) {
          progressiveAmount = progressiveAmount.subtract(moveLine.getAmountRemaining());
        } else {
          progressiveAmount = progressiveAmount.add(moveLine.getAmountRemaining());
        }
        if (progressiveAmount.signum() == 0) {
          debitMoveLines =
              moveLinesToProcess.stream()
                  .filter(ml -> ml.getDebit().signum() > 0)
                  .collect(Collectors.toList());
          creditMoveLines =
              moveLinesToProcess.stream()
                  .filter(ml -> ml.getCredit().signum() > 0)
                  .collect(Collectors.toList());

          reconcileWithMethod(
              debitMoveLines,
              creditMoveLines,
              AccountingBatchRepository.AUTO_MOVE_LETTERING_RECONCILE_BY_BALANCED_MOVE);

          moveLinesReconciled.addAll(moveLinesToProcess);
        }
      }
    }
  }

  protected void reconcileWithMethod(
      List<MoveLine> debitMoveLines, List<MoveLine> creditMoveLines, int reconcileMethodSelect) {

    BigDecimal debitTotalRemaining =
        debitMoveLines.stream()
            .map(MoveLine::getAmountRemaining)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    BigDecimal creditTotalRemaining =
        creditMoveLines.stream()
            .map(MoveLine::getAmountRemaining)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    boolean isBalanced = debitTotalRemaining.compareTo(creditTotalRemaining) == 0;

    Map<MoveLine, BigDecimal> debitRemaining = new HashMap<>();
    for (MoveLine debitMoveLine : debitMoveLines) {
      debitRemaining.put(debitMoveLine, debitMoveLine.getAmountRemaining());
    }
    for (MoveLine creditMoveLine : creditMoveLines) {
      BigDecimal creditRemaining = creditMoveLine.getAmountRemaining();
      for (MoveLine debitMoveLine : debitMoveLines) {
        BigDecimal debit = debitMoveLine.getDebit();
        BigDecimal credit = creditMoveLine.getCredit();
        BigDecimal nextCreditRemaining = creditRemaining.subtract(debit);
        BigDecimal nextDebitRemaining = debitRemaining.get(debitMoveLine).subtract(credit);
        if (!isBalanced && (nextCreditRemaining.signum() < 0 || nextDebitRemaining.signum() < 0)) {
          continue;
        }

        debitMoveLine = moveLineRepository.find(debitMoveLine.getId());
        creditMoveLine = moveLineRepository.find(creditMoveLine.getId());

        if (canBeReconciled(reconcileMethodSelect, debitMoveLine, creditMoveLine, isBalanced)) {
          try {
            reconcile(debitMoveLine, creditMoveLine, debitTotalRemaining, creditTotalRemaining);
            creditRemaining = nextCreditRemaining;
            debitRemaining.replace(debitMoveLine, nextDebitRemaining);
            moveLineReconciledSet.add(debitMoveLine);
            moveLineReconciledSet.add(creditMoveLine);
          } catch (Exception e) {
            TraceBackService.trace(
                new Exception(
                    String.format(
                        I18n.get("Debit move line %s and Credit move line %s"),
                        debitMoveLine.getName(),
                        creditMoveLine.getName()),
                    e),
                ExceptionOriginRepository.MOVE_LINE_RECONCILE,
                batch.getId());
            incrementAnomaly();
            LOG.error(
                "Anomaly generated while lettering debit move line {} and credit move line {}",
                debitMoveLine.getName(),
                creditMoveLine.getName());
          } finally {
            JPA.clear();
          }
        }
      }
    }
  }

  private boolean canBeReconciled(
      int reconcileMethodSelect,
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean isBalanced) {
    if (reconcileMethodSelect
        == AccountingBatchRepository.AUTO_MOVE_LETTERING_RECONCILE_BY_BALANCED_MOVE) {
      return true;
    }
    BigDecimal debit = debitMoveLine.getDebit();
    BigDecimal credit = creditMoveLine.getCredit();
    boolean reconcileByAmount =
        reconcileMethodSelect == AccountingBatchRepository.AUTO_MOVE_LETTERING_RECONCILE_BY_AMOUNT
            && debit.compareTo(credit) == 0;
    boolean reconcileByOrigin =
        reconcileMethodSelect == AccountingBatchRepository.AUTO_MOVE_LETTERING_RECONCILE_BY_ORIGIN
            && debitMoveLine.getOrigin() != null
            && creditMoveLine.getOrigin() != null
            && debitMoveLine.getOrigin().equals(creditMoveLine.getOrigin())
            && (accountingBatch.getIsPartialReconcile() || debit.compareTo(credit) == 0);
    boolean reconcileByBalancedAccount =
        reconcileMethodSelect
                == AccountingBatchRepository.AUTO_MOVE_LETTERING_RECONCILE_BY_BALANCED_ACCOUNT
            && isBalanced;
    boolean reconcileByExternalIdentifier =
        reconcileMethodSelect
                == AccountingBatchRepository.AUTO_MOVE_LETTERING_RECONCILE_BY_EXTERNAL_IDENTIFIER
            && debitMoveLine.getExternalOrigin() != null
            && creditMoveLine.getExternalOrigin() != null
            && debitMoveLine.getExternalOrigin().equals(creditMoveLine.getExternalOrigin())
            && (accountingBatch.getIsPartialReconcile() || debit.compareTo(credit) == 0);

    return reconcileByAmount
        || reconcileByOrigin
        || reconcileByBalancedAccount
        || reconcileByExternalIdentifier;
  }

  @Transactional
  protected void reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      BigDecimal debitTotalRemaining,
      BigDecimal creditTotalRemaining)
      throws AxelorException {

    BigDecimal amount;
    Reconcile reconcile = null;
    if (debitMoveLine.getMaxAmountToReconcile() != null
        && debitMoveLine.getMaxAmountToReconcile().compareTo(BigDecimal.ZERO) > 0) {
      amount = debitMoveLine.getMaxAmountToReconcile().min(creditMoveLine.getAmountRemaining());
      debitMoveLine.setMaxAmountToReconcile(null);
    } else {
      amount = creditMoveLine.getAmountRemaining().min(debitMoveLine.getAmountRemaining());
    }
    LOG.debug("amount : {}", amount);
    LOG.debug("debitTotalRemaining : {}", debitTotalRemaining);
    LOG.debug("creditTotalRemaining : {}", creditTotalRemaining);
    BigDecimal nextDebitTotalRemaining = debitTotalRemaining.subtract(amount);
    BigDecimal nextCreditTotalRemaining = creditTotalRemaining.subtract(amount);
    findBatch();
    accountingBatch = batch.getAccountingBatch();
    accountingBatch.setCompany(companyRepository.find(accountingBatch.getCompany().getId()));
    // Gestion du passage en 580
    if (nextDebitTotalRemaining.compareTo(BigDecimal.ZERO) <= 0
        || nextCreditTotalRemaining.compareTo(BigDecimal.ZERO) <= 0) {
      LOG.debug("last loop");
      reconcile = reconcileService.createReconcile(debitMoveLine, creditMoveLine, amount, true);
    } else {
      reconcile = reconcileService.createReconcile(debitMoveLine, creditMoveLine, amount, false);
    }
    // End gestion du passage en 580

    if (reconcile != null) {
      ReconcileGroup reconcileGroup =
          reconcileGroupService.findOrMergeGroup(reconcile).orElse(null);
      if (accountingBatch.getIsProposal()) {

        if (reconcileGroup == null) {
          reconcileGroup = reconcileGroupService.createReconcileGroup(accountingBatch.getCompany());
          reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_PROPOSAL);
        }
        reconcileGroup.setIsProposal(true);

        List<Reconcile> reconcileList = reconcileGroupService.getReconcileList(reconcileGroup);
        reconcileList.add(reconcile);
        reconcileGroupService.addToReconcileGroup(reconcileGroup, reconcile);
      } else {
        reconcileService.confirmReconcile(reconcile, true, true);
        reconcileGroupService.removeDraftReconciles(reconcileGroup);
      }
      debitMoveLine.addBatchSetItem(batch);
      creditMoveLine.addBatchSetItem(batch);
      moveLineRepository.save(debitMoveLine);
      moveLineRepository.save(creditMoveLine);

      LOG.debug("Reconcile : {}", reconcile);
    }
  }

  public Query<MoveLine> getMoveLinesQuery() {
    return moveLineRepository
        .all()
        .filter(getMoveLinesToReconcileFilter(accountingBatch))
        .bind(getMoveLinesToReconcileParams(accountingBatch))
        .order("id");
  }

  protected String getMoveLinesToReconcileFilter(AccountingBatch accountingBatch) {
    String filters =
        "self.amountRemaining != 0 AND self.move.statusSelect IN (:moveDaybookStatus, :moveAccountedStatus) AND self.move.company = :company AND self.date >= :startDate AND self.date <= :endDate";

    if (CollectionUtils.isNotEmpty(accountingBatch.getTradingNameSet())) {
      filters += " AND self.move.tradingName IN :tradingNameSet";
    }
    if (accountingBatch.getFromAccount() != null) {
      filters += " AND self.account.code >= :fromAccountCode";
    }
    if (accountingBatch.getToAccount() != null) {
      filters += " AND self.account.code <= :toAccountCode";
    }
    if (CollectionUtils.isNotEmpty(accountingBatch.getPartnerSet())) {
      filters += " AND self.partner.id IN :partnersIds";
    }

    return filters;
  }

  protected Map<String, Object> getMoveLinesToReconcileParams(AccountingBatch accountingBatch) {
    Map<String, Object> params = new HashMap<>();

    params.put("moveDaybookStatus", MoveRepository.STATUS_DAYBOOK);
    params.put("moveAccountedStatus", MoveRepository.STATUS_ACCOUNTED);
    params.put("company", accountingBatch.getCompany());
    params.put("startDate", accountingBatch.getStartDate());
    params.put("endDate", accountingBatch.getEndDate());

    if (CollectionUtils.isNotEmpty(accountingBatch.getTradingNameSet())) {
      params.put("tradingNameSet", accountingBatch.getTradingNameSet());
    }
    if (accountingBatch.getFromAccount() != null) {
      params.put("fromAccountCode", accountingBatch.getFromAccount().getCode());
    }
    if (accountingBatch.getToAccount() != null) {
      params.put("toAccountCode", accountingBatch.getToAccount().getCode());
    }
    if (CollectionUtils.isNotEmpty(accountingBatch.getPartnerSet())) {
      params.put(
          "partnersIds",
          accountingBatch.getPartnerSet().stream()
              .distinct()
              .map(Partner::getId)
              .collect(Collectors.toList()));
    }

    return params;
  }

  public String getMoveLinesAwaitingReconcileFilters(AccountingBatch accountingBatch) {
    return getMoveLinesToReconcileFilter(accountingBatch)
        + " AND self.reconcileGroup IS NOT null AND self.reconcileGroup.isProposal IS true";
  }

  public Map<String, Object> getMoveLinesAwaitingReconcileParams(AccountingBatch accountingBatch) {
    return getMoveLinesToReconcileParams(accountingBatch);
  }

  public boolean getIsMoveLinesAwaitingReconcile(AccountingBatch accountingBatch) {
    return moveLineRepository
            .all()
            .filter(getMoveLinesAwaitingReconcileFilters(accountingBatch))
            .bind(getMoveLinesToReconcileParams(accountingBatch))
            .count()
        > 0;
  }

  protected Comparator<MoveLine> getMoveLineComparator() {
    Comparator<MoveLine> moveLineComparator = null;
    Comparator nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder());

    String[] orderBySelect = accountingBatch.getOrderBySelect().split(", ");

    for (int i = 0; i < orderBySelect.length; i++) {
      switch (orderBySelect[i]) {
        case AccountingBatchRepository.AUTO_MOVE_LETTERING_ORDER_BY_ACCOUNTING_DATE:
          moveLineComparator =
              i == 0
                  ? Comparator.comparing(MoveLine::getDate, nullSafeComparator)
                  : moveLineComparator.thenComparing(MoveLine::getDate, nullSafeComparator);
          break;
        case AccountingBatchRepository.AUTO_MOVE_LETTERING_ORDER_BY_ORIGIN:
          moveLineComparator =
              i == 0
                  ? Comparator.comparing(MoveLine::getOrigin, nullSafeComparator)
                  : moveLineComparator.thenComparing(MoveLine::getOrigin, nullSafeComparator);
          break;
        case AccountingBatchRepository.AUTO_MOVE_LETTERING_ORDER_BY_DUE_DATE:
          moveLineComparator =
              i == 0
                  ? Comparator.comparing(MoveLine::getDueDate, nullSafeComparator)
                  : moveLineComparator.thenComparing(MoveLine::getDueDate, nullSafeComparator);
          break;
        case AccountingBatchRepository.AUTO_MOVE_LETTERING_ORDER_BY_PAYMENT_MODE:
          moveLineComparator =
              i == 0
                  ? Comparator.comparing(moveLine -> moveLine.getMove().getPaymentMode().getId())
                  : moveLineComparator.thenComparing(
                      moveLine -> moveLine.getMove().getPaymentMode().getId());
          break;
        case AccountingBatchRepository.AUTO_MOVE_LETTERING_ORDER_BY_LINE_LABEL:
          moveLineComparator =
              i == 0
                  ? Comparator.comparing(MoveLine::getName)
                  : moveLineComparator.thenComparing(MoveLine::getName);
          break;
      }
    }

    if (moveLineComparator == null) {
      moveLineComparator = Comparator.comparing(MoveLine::getId);
    } else {
      moveLineComparator = moveLineComparator.thenComparing(MoveLine::getId);
    }
    return moveLineComparator;
  }

  protected Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> getMoveLinesMap() {
    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> listPairMap =
        moveLineService.getPopulatedReconcilableMoveLineMap(
            getMoveLinesQuery().fetch().stream()
                .filter(moveLine -> moveLineControlService.canReconcile(moveLine))
                .collect(Collectors.toList()));

    Comparator<MoveLine> moveLineComparator = getMoveLineComparator();

    listPairMap
        .values()
        .forEach(
            listListPair -> {
              listListPair.getLeft().sort(Comparator.nullsLast(moveLineComparator));
              listListPair.getRight().sort(Comparator.nullsLast(moveLineComparator));
            });

    return listPairMap;
  }

  public boolean existsAlreadyRunning(AccountingBatch accountingBatch) {
    Map<String, Object> params = new HashMap<>();
    String filters =
        "self.archived IS true AND self.actionSelect = :autoMoveLetteringActionSelect AND self.company = :company AND (self.startDate > :endDate OR self.endDate < :startDate)";
    params.put(
        "autoMoveLetteringActionSelect", AccountingBatchRepository.ACTION_AUTO_MOVE_LETTERING);
    params.put("company", accountingBatch.getCompany());
    params.put("startDate", accountingBatch.getStartDate());
    params.put("endDate", accountingBatch.getEndDate());
    filters += " AND ";
    if (accountingBatch.getFromAccount() != null && accountingBatch.getToAccount() != null) {
      filters +=
          "((self.fromAccount IS NOT null AND self.toAccount IS NOT null AND self.fromAccount.code <= :toAccountCode AND self.toAccount.code >= :fromAccountCode)";
      filters +=
          " OR (self.fromAccount IS NOT null AND self.toAccount IS null AND self.fromAccount.code <= :toAccountCode)";
      filters +=
          " OR (self.fromAccount IS null AND self.toAccount IS NOT null AND self.toAccount.code >= :fromAccountCode))";
      params.put("fromAccountCode", accountingBatch.getFromAccount().getCode());
      params.put("toAccountCode", accountingBatch.getToAccount().getCode());
    } else if (accountingBatch.getFromAccount() != null) {
      filters += "(self.toAccount IS null OR self.toAccount.code >= :fromAccountCode)";
      params.put("fromAccountCode", accountingBatch.getFromAccount().getCode());
    } else if (accountingBatch.getToAccount() != null) {
      filters += "(self.fromAccount IS null OR self.fromAccount.code <= :toAccountCode)";
      params.put("toAccountCode", accountingBatch.getToAccount().getCode());
    }
    return accountingBatchRepository
        .all()
        .filter(filters)
        .bind(params)
        .fetchStream()
        .anyMatch(
            accountingBatch1 ->
                (CollectionUtils.isEmpty(accountingBatch1.getTradingNameSet())
                        || CollectionUtils.isEmpty(accountingBatch.getTradingNameSet())
                        || CollectionUtils.containsAny(
                            accountingBatch1.getTradingNameSet(),
                            accountingBatch.getTradingNameSet()))
                    && (CollectionUtils.isEmpty(accountingBatch1.getPartnerSet())
                        || CollectionUtils.isEmpty(accountingBatch.getPartnerSet())
                        || CollectionUtils.containsAny(
                            accountingBatch1.getPartnerSet(), accountingBatch.getPartnerSet())));
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {
    StringBuilder comment =
        new StringBuilder(
            String.format(
                "%s\n\t* %s ",
                I18n.get(AccountExceptionMessage.BATCH_AUTO_MOVE_LETTERING_REPORT_TITLE),
                batch.getDone()));

    comment.append(getProcessedMessage());

    comment.append(
        String.format(
            "\n\t" + I18n.get(com.axelor.apps.base.exceptions.BaseExceptionMessage.BASE_BATCH_3),
            batch.getAnomaly()));

    super.stop();
    addComment(comment.toString());
  }

  protected String getProcessedMessage() {
    return I18n.get(AccountExceptionMessage.BATCH_AUTO_MOVE_LETTERING_MOVE_LINE_RECONCILED);
  }
}
