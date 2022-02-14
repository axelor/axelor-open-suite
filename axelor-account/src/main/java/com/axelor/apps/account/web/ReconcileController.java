/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ReconcileController {

  // Unreconcile button
  public void unreconcile(ActionRequest request, ActionResponse response) {

    Reconcile reconcile = request.getContext().asType(Reconcile.class);

    try {
      Beans.get(ReconcileService.class)
          .unreconcile(Beans.get(ReconcileRepository.class).find(reconcile.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // Reconcile button
  public void reconcile(ActionRequest request, ActionResponse response) {

    Reconcile reconcile = request.getContext().asType(Reconcile.class);

    try {
      Beans.get(ReconcileService.class)
          .confirmReconcile(Beans.get(ReconcileRepository.class).find(reconcile.getId()), true);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
