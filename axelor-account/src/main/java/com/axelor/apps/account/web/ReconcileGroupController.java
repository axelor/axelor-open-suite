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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;

public class ReconcileGroupController {

  public void letter(ActionRequest request, ActionResponse response) {
    try {
      ReconcileGroup reconcileGroup = request.getContext().asType(ReconcileGroup.class);
      reconcileGroup = Beans.get(ReconcileGroupRepository.class).find(reconcileGroup.getId());

      Beans.get(MoveLineService.class).letter(reconcileGroup);
      response.setReload(true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void unletter(ActionRequest request, ActionResponse response) {
    try {
      ReconcileGroup reconcileGroup = request.getContext().asType(ReconcileGroup.class);
      reconcileGroup = Beans.get(ReconcileGroupRepository.class).find(reconcileGroup.getId());

      Beans.get(ReconcileService.class).unletter(reconcileGroup);
      response.setReload(true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateProposal(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      ReconcileGroup reconcileGroup =
          ContextHelper.getContextParent(context, ReconcileGroup.class, 0);

      if (reconcileGroup == null) {
        reconcileGroup = ContextHelper.getContextParent(context, ReconcileGroup.class, 1);
      }

      if (reconcileGroup != null && reconcileGroup.getIsProposal()) {
        reconcileGroup = Beans.get(ReconcileGroupRepository.class).find(reconcileGroup.getId());

        Beans.get(MoveLineService.class).validateProposal(reconcileGroup);
      }

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void cancelProposal(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      ReconcileGroup reconcileGroup;

      boolean isReconcileGroupForm =
          ReconcileGroup.class.equals(request.getContext().getContextClass());

      if (isReconcileGroupForm) {
        reconcileGroup = context.asType(ReconcileGroup.class);
      } else {
        reconcileGroup = context.asType(MoveLine.class).getReconcileGroup();
      }

      if (reconcileGroup != null) {
        ReconcileGroupRepository reconcileGroupRepository =
            Beans.get(ReconcileGroupRepository.class);
        ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
        reconcileGroup = reconcileGroupRepository.find(reconcileGroup.getId());

        reconcileGroupService.cancelProposal(reconcileGroup);
      }
      if (isReconcileGroupForm) {
        response.setView(
            ActionView.define(I18n.get("Reconcile groups"))
                .model(ReconcileGroup.class.getName())
                .add("grid", "reconcile-group-grid")
                .add("form", "reconcile-group-form")
                .map());
      } else {
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
