/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.reconcile.reconcilegroup;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.reconcilegroup.ReconcileGroupFetchService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReconcileGroupServiceImpl implements ReconcileGroupService {

  protected ReconcileGroupRepository reconcileGroupRepository;
  protected ReconcileRepository reconcileRepository;
  protected AppBaseService appBaseService;
  protected ReconcileGroupFetchService reconcileGroupFetchService;
  protected ReconcileGroupToolService reconcileGroupToolService;

  @Inject
  public ReconcileGroupServiceImpl(
      ReconcileGroupRepository reconcileGroupRepository,
      ReconcileRepository reconcileRepository,
      AppBaseService appBaseService,
      ReconcileGroupFetchService reconcileGroupFetchService,
      ReconcileGroupToolService reconcileGroupToolService) {
    this.reconcileGroupRepository = reconcileGroupRepository;
    this.reconcileRepository = reconcileRepository;
    this.appBaseService = appBaseService;
    this.reconcileGroupFetchService = reconcileGroupFetchService;
    this.reconcileGroupToolService = reconcileGroupToolService;
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
    if (reconcileGroupToolService.isBalanced(reconcileList)) {
      reconcileGroupToolService.validate(reconcileGroup, reconcileList);
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
}
