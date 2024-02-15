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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileGroupServiceImpl implements ReconcileGroupService {

  protected ReconcileGroupRepository reconcileGroupRepository;
  protected ReconcileRepository reconcileRepository;
  protected MoveLineRepository moveLineRepository;
  protected AppBaseService appBaseService;
  protected ReconcileGroupSequenceService reconcileGroupSequenceService;
  protected ReconcileGroupFetchService reconcileGroupFetchService;

  @Inject
  public ReconcileGroupServiceImpl(
      ReconcileGroupRepository reconcileGroupRepository,
      ReconcileRepository reconcileRepository,
      MoveLineRepository moveLineRepository,
      AppBaseService appBaseService,
      ReconcileGroupSequenceService reconcileGroupSequenceService,
      ReconcileGroupFetchService reconcileGroupFetchService) {
    this.reconcileGroupRepository = reconcileGroupRepository;
    this.reconcileRepository = reconcileRepository;
    this.moveLineRepository = moveLineRepository;
    this.appBaseService = appBaseService;
    this.reconcileGroupSequenceService = reconcileGroupSequenceService;
    this.reconcileGroupFetchService = reconcileGroupFetchService;
  }

  /**
   * Validate the given reconcile group. A reconcile Group can be validated if it is not empty and
   * its lines are balanced.
   *
   * @param reconcileGroup a reconcile group.
   * @param reconcileList a list of reconcile.
   * @throws AxelorException if the reconcile list is empty.
   */
  protected void validate(ReconcileGroup reconcileGroup, List<Reconcile> reconcileList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(reconcileList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_GROUP_VALIDATION_NO_LINES),
          reconcileGroup);
    }

    reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_BALANCED);
    reconcileGroup.setLetteringDateTime(
        appBaseService.getTodayDateTime(reconcileGroup.getCompany()).toLocalDateTime());

    reconcileGroupSequenceService.fillCodeFromSequence(reconcileGroup);
  }

  /**
   * Check if the given reconcile lines are balanced.
   *
   * @param reconcileList a list of reconcile.
   */
  protected boolean isBalanced(List<Reconcile> reconcileList) {
    List<MoveLine> debitMoveLineList =
        reconcileList.stream()
            .map(Reconcile::getDebitMoveLine)
            .distinct()
            .collect(Collectors.toList());
    List<MoveLine> creditMoveLineList =
        reconcileList.stream()
            .map(Reconcile::getCreditMoveLine)
            .distinct()
            .collect(Collectors.toList());
    List<Account> accountList =
        debitMoveLineList.stream()
            .map(MoveLine::getAccount)
            .distinct()
            .collect(Collectors.toList());
    accountList.addAll(
        creditMoveLineList.stream()
            .map(MoveLine::getAccount)
            .distinct()
            .collect(Collectors.toList()));

    for (Account account : accountList) {
      BigDecimal totalDebit =
          debitMoveLineList.stream()
              .filter(moveLine -> moveLine.getAccount().equals(account))
              .map(MoveLine::getDebit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      BigDecimal totalCredit =
          creditMoveLineList.stream()
              .filter(moveLine -> moveLine.getAccount().equals(account))
              .map(MoveLine::getCredit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      if (totalDebit.compareTo(totalCredit) != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Call {@link this#findOrMergeGroup} to get a reconcile group. If not found, create one with
   * {@link this#createReconcileGroup}
   *
   * @param reconcile a confirmed reconcile
   * @return the created or found group.
   */
  protected ReconcileGroup findOrCreateGroup(Reconcile reconcile) {
    return findOrMergeGroup(reconcile)
        .orElseGet(() -> createReconcileGroup(reconcile.getCompany()));
  }

  /**
   * Find the corresponding group for a given reconcile. If two or more reconcile group are found,
   * then return the merge between them.
   *
   * @param reconcile a confirmed reconcile.
   * @return an optional with the reconcile group if it was found. Else an empty optional.
   */
  protected Optional<ReconcileGroup> findOrMergeGroup(Reconcile reconcile) {
    List<ReconcileGroup> otherReconcileGroupList;
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();
    otherReconcileGroupList = new ArrayList<>();
    if (debitMoveLine.getReconcileGroup() != null) {
      otherReconcileGroupList.add(debitMoveLine.getReconcileGroup());
    }
    if (creditMoveLine.getReconcileGroup() != null) {
      otherReconcileGroupList.add(creditMoveLine.getReconcileGroup());
    }
    otherReconcileGroupList =
        otherReconcileGroupList.stream().distinct().collect(Collectors.toList());
    if (otherReconcileGroupList.isEmpty()) {
      return Optional.empty();
    } else if (otherReconcileGroupList.size() == 1) {
      return Optional.of(otherReconcileGroupList.get(0));
    } else {
      return Optional.of(mergeReconcileGroups(otherReconcileGroupList));
    }
  }

  /**
   * Merge reconcile groups into one. The created reconcile group will have a new sequence and all
   * reconcile lines from the groups.
   *
   * @param reconcileGroupList a non empty list of reconcile group to merge.
   * @return the created reconcile group.
   */
  @Transactional
  protected ReconcileGroup mergeReconcileGroups(List<ReconcileGroup> reconcileGroupList) {
    Company company = reconcileGroupList.get(0).getCompany();
    ReconcileGroup reconcileGroup = createReconcileGroup(company);

    List<Reconcile> reconcileList =
        reconcileRepository
            .all()
            .filter("self.reconcileGroup.id IN (:reconcileGroupIds)")
            .bind(
                "reconcileGroupIds",
                reconcileGroupList.stream().map(ReconcileGroup::getId).collect(Collectors.toList()))
            .fetch();
    reconcileList.forEach(reconcile -> addToReconcileGroup(reconcileGroup, reconcile));

    for (ReconcileGroup toDeleteReconcileGroup : reconcileGroupList) {
      reconcileGroupRepository.remove(toDeleteReconcileGroup);
    }

    return reconcileGroupRepository.save(reconcileGroup);
  }

  /**
   * Create a reconcile group with the given company.
   *
   * @param company a company.
   * @return a new reconcile group.
   */
  @Transactional
  protected ReconcileGroup createReconcileGroup(Company company) {
    ReconcileGroup reconcileGroup = new ReconcileGroup();
    reconcileGroup.setCompany(company);
    return reconcileGroupRepository.save(reconcileGroup);
  }

  @Override
  public void addAndValidateReconcileGroup(Reconcile reconcile) throws AxelorException {
    ReconcileGroup reconcileGroup = this.findOrCreateGroup(reconcile);
    List<Reconcile> reconcileList =
        reconcileGroupFetchService.fetchConfirmedReconciles(reconcileGroup);
    reconcileList.add(reconcile);
    addToReconcileGroup(reconcileGroup, reconcile);
    if (isBalanced(reconcileList)) {
      validate(reconcileGroup, reconcileList);
    } else if (reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_PARTIAL) {
      reconcileGroup.setLetteringDateTime(
          appBaseService.getTodayDateTime(reconcileGroup.getCompany()).toLocalDateTime());
    }
  }

  /**
   * Add the reconcile and its move line to the reconcile group.
   *
   * @param reconcileGroup a reconcileGroup.
   * @param reconcile the confirmed reconcile to be added.
   */
  protected void addToReconcileGroup(ReconcileGroup reconcileGroup, Reconcile reconcile) {
    reconcile.setReconcileGroup(reconcileGroup);
    reconcile.getDebitMoveLine().setReconcileGroup(reconcileGroup);
    reconcile.getCreditMoveLine().setReconcileGroup(reconcileGroup);
  }

  @Override
  public void remove(Reconcile reconcile) throws AxelorException {

    ReconcileGroup reconcileGroup = reconcile.getReconcileGroup();

    if (reconcileGroup == null) {
      return;
    }

    // update move lines
    List<MoveLine> moveLineToRemoveList =
        moveLineRepository.findByReconcileGroup(reconcileGroup).fetch();
    moveLineToRemoveList.forEach(moveLine -> moveLine.setReconcileGroup(null));

    List<Reconcile> reconcileList =
        reconcileGroupFetchService.fetchConfirmedReconciles(reconcileGroup);
    reconcileList.stream()
        .map(Reconcile::getDebitMoveLine)
        .forEach(moveLine -> moveLine.setReconcileGroup(reconcileGroup));
    reconcileList.stream()
        .map(Reconcile::getCreditMoveLine)
        .forEach(moveLine -> moveLine.setReconcileGroup(reconcileGroup));

    // update status
    updateStatus(reconcileGroup);
  }

  /**
   * Update the status and the sequence of a reconcile group.
   *
   * @param reconcileGroup
   */
  @Transactional(rollbackOn = Exception.class)
  protected void updateStatus(ReconcileGroup reconcileGroup) throws AxelorException {
    List<Reconcile> reconcileList =
        reconcileGroupFetchService.fetchConfirmedReconciles(reconcileGroup);
    int status = reconcileGroup.getStatusSelect();
    LocalDateTime todayDateTime =
        appBaseService.getTodayDateTime(reconcileGroup.getCompany()).toLocalDateTime();
    if (CollectionUtils.isNotEmpty(reconcileList)
        && isBalanced(reconcileList)
        && status == ReconcileGroupRepository.STATUS_PARTIAL) {
      validate(reconcileGroup, reconcileList);
    } else if (CollectionUtils.isNotEmpty(reconcileList)
        && !isBalanced(reconcileList)
        && status == ReconcileGroupRepository.STATUS_BALANCED) {
      reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_PARTIAL);
      reconcileGroup.setLetteringDateTime(todayDateTime);
      reconcileGroupSequenceService.fillCodeFromSequence(reconcileGroup);
    } else if (CollectionUtils.isEmpty(reconcileList)
        && (reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_PARTIAL
            || reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_BALANCED)) {
      reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_UNLETTERED);
      reconcileGroup.setUnletteringDateTime(todayDateTime);
      reconcileGroupRepository.save(reconcileGroup);
    }
  }
}
