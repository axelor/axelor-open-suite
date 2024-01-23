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
package com.axelor.apps.contract.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.batch.BatchContractFactoryInvoicing;
import com.axelor.apps.contract.batch.service.BatchContractService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ContractBatchController {

  public void runBatch(ActionRequest request, ActionResponse response) {
    try {
      ContractBatch contractBatch = request.getContext().asType(ContractBatch.class);
      contractBatch = Beans.get(ContractBatchRepository.class).find(contractBatch.getId());
      BatchContractService batchContractService = Beans.get(BatchContractService.class);
      batchContractService.setBatchModel(contractBatch);
      ControllerCallableTool<Batch> controllerCallableTool = new ControllerCallableTool<>();
      Batch batch = controllerCallableTool.runInSeparateThread(batchContractService, response);
      if (batch != null) {
        response.setInfo(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void showContracts(ActionRequest request, ActionResponse response) {
    try {
      ContractBatch contractBatch = request.getContext().asType(ContractBatch.class);
      contractBatch = Beans.get(ContractBatchRepository.class).find(contractBatch.getId());
      String domainFilter = Beans.get(BatchContractFactoryInvoicing.class).prepareFilter(false);
      response.setView(
          ActionView.define(I18n.get("Contracts"))
              .model(Contract.class.getName())
              .add("grid", "contract-grid")
              .add("form", "contract-form")
              .domain(domainFilter)
              .context("date", contractBatch.getInvoicingDate())
              .context("targetTypeSelect", contractBatch.getTargetTypeSelect())
              .context("statusSelect", ContractRepository.CLOSED_CONTRACT)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
