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

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
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
          .confirmReconcile(
              Beans.get(ReconcileRepository.class).find(reconcile.getId()), true, true);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkReconcile(ActionRequest request, ActionResponse response) {
    Reconcile reconcile = request.getContext().asType(Reconcile.class);

    try {
      Beans.get(ReconcileService.class)
          .checkReconcile(Beans.get(ReconcileRepository.class).find(reconcile.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setCreditMoveLineDomain(ActionRequest request, ActionResponse response) {
    try {
      Reconcile reconcile = request.getContext().asType(Reconcile.class);

      String moveLineIds =
          Beans.get(ReconcileService.class).getStringAllowedCreditMoveLines(reconcile);

      response.setAttr(
          "creditMoveLine",
          "domain",
          String.format("self.id IN (%s)", moveLineIds.isEmpty() ? "0" : moveLineIds));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDebitMoveLineDomain(ActionRequest request, ActionResponse response) {
    try {
      Reconcile reconcile = request.getContext().asType(Reconcile.class);

      String moveLineIds =
          Beans.get(ReconcileService.class).getStringAllowedDebitMoveLines(reconcile);

      response.setAttr(
          "debitMoveLine",
          "domain",
          String.format("self.id IN (%s)", moveLineIds.isEmpty() ? "0" : moveLineIds));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
