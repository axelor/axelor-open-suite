/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.batch.SupplychainBatchService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class SupplychainBatchController {

  public void invoiceOutgoingStockMoves(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      supplychainBatch = Beans.get(SupplychainBatchRepository.class).find(supplychainBatch.getId());
      Batch batch =
          Beans.get(SupplychainBatchService.class).invoiceOutgoingStockMoves(supplychainBatch);
      response.setFlash(batch.getComments());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void invoiceOrders(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      supplychainBatch = Beans.get(SupplychainBatchRepository.class).find(supplychainBatch.getId());
      Batch batch = Beans.get(SupplychainBatchService.class).invoiceOrders(supplychainBatch);
      response.setFlash(batch.getComments());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void accountingCutOff(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      supplychainBatch = Beans.get(SupplychainBatchRepository.class).find(supplychainBatch.getId());
      Batch batch = Beans.get(SupplychainBatchService.class).accountingCutOff(supplychainBatch);
      response.setFlash(batch.getComments());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
