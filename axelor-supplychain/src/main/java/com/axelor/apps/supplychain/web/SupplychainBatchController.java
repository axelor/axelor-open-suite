/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

  public void updateStockHistory(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      supplychainBatch = Beans.get(SupplychainBatchRepository.class).find(supplychainBatch.getId());
      Batch batch = Beans.get(SupplychainBatchService.class).updateStockHistory(supplychainBatch);
      response.setFlash(batch.getComments());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateDates(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      Beans.get(SupplychainBatchService.class).checkDates(supplychainBatch);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void previewRecordsToProcess(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      SupplychainBatchService supplychainBatchService = Beans.get(SupplychainBatchService.class);
      String idList;

      if (supplychainBatch.getAccountingCutOffTypeSelect()
              == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES
          || supplychainBatch.getAccountingCutOffTypeSelect()
              == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_CUSTOMER_INVOICES) {
        idList =
            supplychainBatchService.getStockMoveLinesToProcessIdList(
                supplychainBatch.getCompany(),
                supplychainBatch.getMoveDate(),
                supplychainBatch.getAccountingCutOffTypeSelect(),
                null,
                null);

        response.setView(
            ActionView.define("Stock move lines concerned by cut off")
                .model(StockMoveLine.class.getName())
                .add("grid", "stock-move-line-cut-off-grid")
                .add("form", "stock-move-line-form")
                .domain(
                    String.format("self.id IN (%s)", Strings.isNullOrEmpty(idList) ? 0 : idList))
                .map());
      } else {
        idList =
            supplychainBatchService.getMoveLinesToProcessIdList(
                supplychainBatch.getCompany(),
                supplychainBatch.getResearchJournal(),
                supplychainBatch.getMoveDate(),
                null,
                null);

        response.setView(
            ActionView.define("Move lines concerned by cut off")
                .model(MoveLine.class.getName())
                .add("grid", "move-line-cut-off-grid")
                .add("form", "move-line-form")
                .domain(
                    String.format("self.id IN (%s)", Strings.isNullOrEmpty(idList) ? 0 : idList))
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
