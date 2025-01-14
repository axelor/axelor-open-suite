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
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.service.reconcilegroup.ReconcileGroupFetchService;
import com.axelor.apps.account.service.reconcilegroup.ReconcileGroupSequenceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class UnreconcileGroupServiceImpl implements UnreconcileGroupService {

  protected ReconcileGroupFetchService reconcileGroupFetchService;
  protected AppBaseService appBaseService;
  protected ReconcileGroupSequenceService reconcileGroupSequenceService;
  protected ReconcileGroupToolService reconcileGroupToolService;
  protected MoveLineRepository moveLineRepository;
  protected ReconcileGroupRepository reconcileGroupRepository;

  @Inject
  public UnreconcileGroupServiceImpl(
      ReconcileGroupFetchService reconcileGroupFetchService,
      AppBaseService appBaseService,
      ReconcileGroupSequenceService reconcileGroupSequenceService,
      ReconcileGroupToolService reconcileGroupToolService,
      MoveLineRepository moveLineRepository,
      ReconcileGroupRepository reconcileGroupRepository) {
    this.reconcileGroupFetchService = reconcileGroupFetchService;
    this.appBaseService = appBaseService;
    this.reconcileGroupSequenceService = reconcileGroupSequenceService;
    this.reconcileGroupToolService = reconcileGroupToolService;
    this.moveLineRepository = moveLineRepository;
    this.reconcileGroupRepository = reconcileGroupRepository;
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
        && reconcileGroupToolService.isBalanced(reconcileList)
        && status == ReconcileGroupRepository.STATUS_PARTIAL) {
      reconcileGroupToolService.validate(reconcileGroup, reconcileList);
    } else if (CollectionUtils.isNotEmpty(reconcileList)
        && !reconcileGroupToolService.isBalanced(reconcileList)
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
