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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.reconcile.UnreconcileService;
import com.axelor.apps.account.util.MoveUtilsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.utils.service.ArchivingService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class MoveRemoveServiceImpl implements MoveRemoveService {

  protected MoveRepository moveRepo;

  protected MoveLineRepository moveLineRepo;

  protected ArchivingService archivingService;

  protected UnreconcileService unReconcileService;

  protected AccountingSituationService accountingSituationService;

  protected AccountCustomerService accountCustomerService;

  protected MoveUtilsService moveUtilsService;

  @Inject
  public MoveRemoveServiceImpl(
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      ArchivingService archivingService,
      UnreconcileService unReconcileService,
      AccountingSituationService accountingSituationService,
      AccountCustomerService accountCustomerService,
      MoveUtilsService moveUtilsService) {
    this.moveRepo = moveRepo;
    this.moveLineRepo = moveLineRepo;
    this.archivingService = archivingService;
    this.unReconcileService = unReconcileService;
    this.accountingSituationService = accountingSituationService;
    this.accountCustomerService = accountCustomerService;
    this.moveUtilsService = moveUtilsService;
  }

  @Override
  public void archiveDaybookMove(Move move) throws Exception {
    if (move.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK)) {
      moveUtilsService.checkMoveBeforeRemove(move);
      this.cleanMoveToArchived(move);
      move = this.updateMoveToArchived(move);
      this.archiveMove(move);
      this.updateSystem(move);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move updateMoveToArchived(Move move) throws AxelorException {

    if (move.getStatusSelect().equals(MoveRepository.STATUS_ACCOUNTED)) {
      throw new AxelorException(
          move,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_CANCEL_4));
    }

    move.setStatusSelect(MoveRepository.STATUS_CANCELED);
    return move;
  }

  protected void cleanMoveToArchived(Move move) throws Exception {
    for (MoveLine moveLine : move.getMoveLineList()) {
      for (Reconcile reconcile : moveLine.getDebitReconcileList()) {
        if (reconcile.getStatusSelect() != ReconcileRepository.STATUS_CANCELED) {
          unReconcileService.unreconcile(reconcile);
        }
      }
      for (Reconcile reconcile : moveLine.getCreditReconcileList()) {
        if (reconcile.getStatusSelect() != ReconcileRepository.STATUS_CANCELED) {
          unReconcileService.unreconcile(reconcile);
        }
      }
    }
  }

  protected void updateSystem(Move move) throws Exception {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getPartner() != null) {
        accountCustomerService.updateAccountingSituationCustomerAccount(
            accountingSituationService.getAccountingSituation(
                moveLine.getPartner(), move.getCompany()),
            true,
            true,
            true);
        accountingSituationService.updateCustomerCredit(moveLine.getPartner());
      }
    }
  }

  @Override
  @Transactional
  public Move archiveMove(Move move) {
    move.setArchived(true);
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setArchived(true);
    }
    return move;
  }

  @Override
  public int deleteMultiple(List<? extends Move> moveList) {
    int errorNB = 0;
    if (moveList == null) {
      return errorNB;
    }
    for (Move move : moveList) {
      try {
        move = moveRepo.find(move.getId());
        if (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
            || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
          this.deleteMove(move);
          JPA.flush();
        } else if (move.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK)) {
          this.archiveDaybookMove(move);
        } else if (move.getStatusSelect().equals(MoveRepository.STATUS_CANCELED)) {
          this.archiveMove(move);
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
        errorNB += 1;
      } finally {
        JPA.clear();
      }
    }
    return errorNB;
  }

  @Override
  @Transactional
  public void deleteMove(Move move) {
    moveRepo.remove(move);
  }
}
