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

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ReconcileGroupController {

  public void letter(ActionRequest request, ActionResponse response) {

    ReconcileGroupRepository reconcileGroupRepo = Beans.get(ReconcileGroupRepository.class);
    ReconcileGroup reconcileGroup =
        reconcileGroupRepo.find(request.getContext().asType(ReconcileGroup.class).getId());

    if (reconcileGroup != null) {
      try {
        ReconcileGroupService reconcileGroupService = Beans.get(ReconcileGroupService.class);
        reconcileGroupService.letter(reconcileGroup);
        reconcileGroup = reconcileGroupRepo.find(reconcileGroup.getId());
        reconcileGroupService.updateStatus(reconcileGroup);
      } catch (AxelorException e) {
        TraceBackService.trace(response, e, ResponseMessageType.ERROR);
      }
    }
    response.setReload(true);
  }

  public void unletter(ActionRequest request, ActionResponse response) {

    ReconcileGroup reconcileGroup =
        Beans.get(ReconcileGroupRepository.class)
            .find(request.getContext().asType(ReconcileGroup.class).getId());

    if (reconcileGroup != null) {
      try {
        Beans.get(ReconcileGroupService.class).unletter(reconcileGroup);
      } catch (AxelorException e) {
        TraceBackService.trace(response, e, ResponseMessageType.ERROR);
      }
    }
    response.setReload(true);
  }
}
