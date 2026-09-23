/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.SupplierDispute;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.apps.supplychain.service.supplierdispute.SupplierDisputeWorkflowService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SupplierDisputeController {

  public void investigate(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(SupplierDisputeWorkflowService.class).investigate(findSupplierDispute(request));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void awaitSupplierResponse(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(SupplierDisputeWorkflowService.class)
          .awaitSupplierResponse(findSupplierDispute(request));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resolve(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(SupplierDisputeWorkflowService.class).resolve(findSupplierDispute(request));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void close(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(SupplierDisputeWorkflowService.class).close(findSupplierDispute(request));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(SupplierDisputeWorkflowService.class).cancel(findSupplierDispute(request));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected SupplierDispute findSupplierDispute(ActionRequest request) {
    SupplierDispute supplierDispute = request.getContext().asType(SupplierDispute.class);
    return Beans.get(SupplierDisputeRepository.class).find(supplierDispute.getId());
  }
}
