/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.apps.account.service.batch.BatchControlMovesConsistency;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
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
      BatchRepository batchRepository = Beans.get(BatchRepository.class);
      MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
      ReconcileGroupRepository reconcileGroupRepo = Beans.get(ReconcileGroupRepository.class);
      ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
      List<MoveLine> moveLineList =
          moveLineRepository
              .all()
              .filter(":batch MEMBER OF self.batchSet")
              .bind("batch", ((Integer) request.getContext().get("_id")).longValue())
              .fetchStream()
              .filter(moveLine -> moveLine.getReconcileGroup() != null)
              .collect(Collectors.toList());

      for (MoveLine moveLine : moveLineList) {
        ReconcileGroup reconcileGroup =
            reconcileGroupRepo.find(moveLine.getReconcileGroup().getId());
        if (reconcileGroup != null && reconcileGroup.getIsProposal()) {
          try {
            reconcileGroupService.letter(reconcileGroup);
            reconcileGroup = reconcileGroupRepo.find(reconcileGroup.getId());
            reconcileGroup.setIsProposal(false);
            reconcileGroupService.updateStatus(reconcileGroup);
          } catch (AxelorException e) {
            TraceBackService.trace(response, e, ResponseMessageType.ERROR);
          }
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
      for (Integer id : idList) {
        ReconcileGroup reconcileGroup =
            reconcileGroupRepo.find(
                moveLineRepository.find(id.longValue()).getReconcileGroup().getId());
        if (reconcileGroup != null && reconcileGroup.getIsProposal()) {
          try {
            reconcileGroupService.letter(reconcileGroup);
            reconcileGroup = reconcileGroupRepo.find(reconcileGroup.getId());
            reconcileGroup.setIsProposal(false);
            reconcileGroupService.updateStatus(reconcileGroup);
          } catch (AxelorException e) {
            TraceBackService.trace(response, e, ResponseMessageType.ERROR);
          }
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
      for (Integer id : idList) {
        MoveLine moveLine = moveLineRepository.find(id.longValue());
        reconcileGroupService.cancelProposal(moveLine);
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
        response.setError("Some selected MoveLines already have a proposal ReconcileGroup");
      }
      reconcileGroupService.createProposal(moveLineList);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateReconcileGroup(ActionRequest request, ActionResponse response) {
    try {
      ReconcileGroupRepository reconcileGroupRepo = Beans.get(ReconcileGroupRepository.class);
      ReconcileGroup reconcileGroup =
          reconcileGroupRepo.find(
              request.getContext().asType(MoveLine.class).getReconcileGroup().getId());

      if (reconcileGroup != null && reconcileGroup.getIsProposal()) {
        try {
          ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
          reconcileGroupService.letter(reconcileGroup);
          reconcileGroup = reconcileGroupRepo.find(reconcileGroup.getId());
          reconcileGroup.setIsProposal(false);
          reconcileGroupService.updateStatus(reconcileGroup);
        } catch (AxelorException e) {
          TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        }
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelReconcileGroup(ActionRequest request, ActionResponse response) {
    try {
      MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
      MoveLine moveLine =
          moveLineRepository.find(request.getContext().asType(MoveLine.class).getId());

      if (moveLine != null) {
        ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
        reconcileGroupService.cancelProposal(moveLine);
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
