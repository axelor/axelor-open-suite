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
package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;

public class ReconcileGroupProposalServiceImpl implements ReconcileGroupProposalService {

  protected ReconcileRepository reconcileRepository;
  protected ReconcileGroupRepository reconcileGroupRepository;
  protected MoveLineRepository moveLineRepository;
  protected ReconcileGroupLetterService reconcileGroupLetterService;

  @Inject
  public ReconcileGroupProposalServiceImpl(
      ReconcileRepository reconcileRepository,
      ReconcileGroupRepository reconcileGroupRepository,
      MoveLineRepository moveLineRepository,
      ReconcileGroupLetterService reconcileGroupLetterService) {
    this.reconcileRepository = reconcileRepository;
    this.reconcileGroupRepository = reconcileGroupRepository;
    this.moveLineRepository = moveLineRepository;
    this.reconcileGroupLetterService = reconcileGroupLetterService;
  }

  @Override
  @Transactional
  public void createProposal(List<MoveLine> moveLineList) {
    ReconcileGroup reconcileGroup =
        moveLineList.stream()
            .map(MoveLine::getReconcileGroup)
            .filter(Objects::nonNull)
            .filter(r -> r.getStatusSelect() == ReconcileGroupRepository.STATUS_PARTIAL)
            .findFirst()
            .orElseGet(
                () -> createProposalReconcileGroup(moveLineList.get(0).getMove().getCompany()));
    reconcileGroup.setIsProposal(true);

    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getReconcileGroup() == null) {
        moveLine.setReconcileGroup(reconcileGroup);
        moveLineRepository.save(moveLine);
      }
    }
  }

  @Transactional
  protected ReconcileGroup createProposalReconcileGroup(Company company) {
    ReconcileGroup reconcileGroup = new ReconcileGroup();
    reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_PROPOSAL);
    reconcileGroup.setCompany(company);
    return reconcileGroupRepository.save(reconcileGroup);
  }

  @Override
  public void validateProposal(ReconcileGroup reconcileGroup) throws AxelorException {
    if (reconcileGroup != null && reconcileGroup.getIsProposal()) {
      reconcileGroup = reconcileGroupRepository.find(reconcileGroup.getId());
      reconcileGroupLetterService.letter(reconcileGroup);
      reconcileGroup.setIsProposal(false);
      removeDraftReconciles(reconcileGroup);
    }
  }

  @Transactional
  protected void removeDraftReconciles(ReconcileGroup reconcileGroup) {
    List<Reconcile> reconcilesToRemove =
        reconcileRepository
            .all()
            .filter("self.reconcileGroup.id = :reconcileGroupId AND self.statusSelect = :draft")
            .bind("reconcileGroupId", reconcileGroup.getId())
            .bind("draft", ReconcileRepository.STATUS_DRAFT)
            .fetch();

    for (Reconcile reconcile : reconcilesToRemove) {
      reconcileRepository.remove(reconcile);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelProposal(ReconcileGroup reconcileGroup) {
    if (reconcileGroup != null) {
      if (reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_PROPOSAL) {
        remove(reconcileGroup);
      } else if (reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_PARTIAL) {
        List<Reconcile> reconcileList =
            reconcileRepository
                .all()
                .filter(
                    "self.reconcileGroup.id = :reconcileGroupId AND self.statusSelect = :statusDraft")
                .bind("reconcileGroupId", reconcileGroup.getId())
                .bind("statusDraft", ReconcileRepository.STATUS_DRAFT)
                .fetch();
        for (Reconcile reconcile : reconcileList) {
          reconcile.getCreditMoveLine().setReconcileGroup(null);
          reconcile.getDebitMoveLine().setReconcileGroup(null);
          reconcileGroup.setIsProposal(false);
          reconcileRepository.remove(reconcile);
        }
      }
    }
  }

  protected void remove(ReconcileGroup reconcileGroup) {
    List<Reconcile> reconcileList =
        reconcileRepository
            .all()
            .filter("self.reconcileGroup.id = :reconcileGroupId")
            .bind("reconcileGroupId", reconcileGroup.getId())
            .fetch();
    for (Reconcile reconcile : reconcileList) {
      reconcile.getDebitMoveLine().setReconcileGroup(null);
      reconcile.getCreditMoveLine().setReconcileGroup(null);
      reconcileRepository.remove(reconcile);
    }
    reconcileGroupRepository.remove(reconcileGroup);
  }
}
