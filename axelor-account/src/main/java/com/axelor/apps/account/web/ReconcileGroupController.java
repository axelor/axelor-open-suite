/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ReconcileGroupController {

  /**
   * Called from ReconcileGroup form view on "valid" button. Call {@link
   * ReconcileGroupService#validateCheckBalanced(ReconcileGroup)}.
   */
  public void validate(ActionRequest request, ActionResponse response) {
    try {
      ReconcileGroup reconcileGroup = request.getContext().asType(ReconcileGroup.class);
      reconcileGroup = Beans.get(ReconcileGroupRepository.class).find(reconcileGroup.getId());
      Beans.get(ReconcileGroupService.class).validateCheckBalanced(reconcileGroup);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
