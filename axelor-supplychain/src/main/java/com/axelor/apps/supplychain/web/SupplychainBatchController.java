/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SupplychainBatchController {

  @Inject protected SupplychainBatchService supplychainBatchService;

  @Inject protected SupplychainBatchRepository supplychainBatchRepo;

  public void invoiceOutgoingStockMoves(ActionRequest request, ActionResponse response) {
    SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
    supplychainBatch = supplychainBatchRepo.find(supplychainBatch.getId());
    Batch batch = supplychainBatchService.invoiceOutgoingStockMoves(supplychainBatch);
    response.setFlash(batch.getComments());
    response.setReload(true);
  }

  public void invoiceOrders(ActionRequest request, ActionResponse response) {
    SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
    supplychainBatch = supplychainBatchRepo.find(supplychainBatch.getId());
    Batch batch = supplychainBatchService.invoiceOrders(supplychainBatch);
    response.setFlash(batch.getComments());
    response.setReload(true);
  }
}
