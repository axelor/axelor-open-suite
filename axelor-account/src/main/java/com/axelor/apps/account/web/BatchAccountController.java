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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.apps.account.service.batch.BatchControlMovesConsistency;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BatchAccountController {

  public void showMoveError(ActionRequest request, ActionResponse response) {
    try {
      Long batchId = request.getContext().asType(Batch.class).getId();

      List<Long> idList = Beans.get(BatchControlMovesConsistency.class).getAllMovesId(batchId);
      if (!CollectionUtils.isEmpty(idList)) {
        response.setView(
            ActionView.define(I18n.get("Moves"))
                .model(Move.class.getName())
                .add("grid", "move-grid")
                .add("form", "move-form")
                .domain(
                    "self.id in ("
                        + idList.stream().map(id -> id.toString()).collect(Collectors.joining(","))
                        + ")")
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateAllReconcileGroups(ActionRequest request, ActionResponse response) {
    try {
      MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
      ReconcileGroupRepository reconcileGroupRepo = Beans.get(ReconcileGroupRepository.class);
      ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
      Map batchMap = (Map<String, Object>) request.getContext().get("batch");
      List<MoveLine> moveLineList =
          moveLineRepository
              .all()
              .filter(":batch MEMBER OF self.batchSet")
              .bind("batch", ((Integer) batchMap.get("id")).longValue())
              .fetchStream()
              .filter(moveLine -> moveLine.getReconcileGroup() != null)
              .collect(Collectors.toList());

      for (MoveLine moveLine : moveLineList) {
        ReconcileGroup reconcileGroup =
            reconcileGroupRepo.find(moveLine.getReconcileGroup().getId());
        try {
          reconcileGroupService.validateProposal(reconcileGroup);
        } catch (AxelorException e) {
          TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        }
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateSelectedReconcileGroups(ActionRequest request, ActionResponse response) {
    try {
      MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
      ReconcileGroupRepository reconcileGroupRepo = Beans.get(ReconcileGroupRepository.class);
      ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
      if (CollectionUtils.isEmpty(idList)) {
        return;
      }
      for (Integer id : idList) {
        ReconcileGroup reconcileGroup =
            reconcileGroupRepo.find(
                moveLineRepository.find(id.longValue()).getReconcileGroup().getId());
        try {
          reconcileGroupService.validateProposal(reconcileGroup);
        } catch (AxelorException e) {
          TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        }
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelSelectedReconcileGroups(ActionRequest request, ActionResponse response) {
    try {
      MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
      ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
      if (CollectionUtils.isEmpty(idList)) {
        return;
      }
      for (Integer id : idList) {
        MoveLine moveLine = moveLineRepository.find(id.longValue());
        reconcileGroupService.cancelProposal(moveLine.getReconcileGroup());
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void partialReconcileSelectedMoveLines(ActionRequest request, ActionResponse response) {
    try {
      MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
      ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
      if (CollectionUtils.isEmpty(idList)) {
        return;
      }
      List<MoveLine> moveLineList =
          idList.stream()
              .map(id -> moveLineRepository.find(id.longValue()))
              .collect(Collectors.toList());
      if (CollectionUtils.isEmpty(moveLineList)) {
        return;
      }
      if (moveLineList.stream()
          .anyMatch(
              moveLine ->
                  moveLine.getReconcileGroup() != null
                      && moveLine.getReconcileGroup().getIsProposal())) {
        response.setError(I18n.get(AccountExceptionMessage.ALREADY_HAVE_PROPOSAL_RECONCILE));
      }
      reconcileGroupService.createProposal(moveLineList);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
