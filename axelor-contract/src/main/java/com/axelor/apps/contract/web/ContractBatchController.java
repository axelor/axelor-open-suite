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
package com.axelor.apps.contract.web;

import com.axelor.apps.base.db.AppContract;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.contract.batch.BatchContract;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.exception.IExceptionMessage;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ContractBatchController {

  public void runBatch(ActionRequest request, ActionResponse response) {
    try {
      ContractBatch contractBatch = request.getContext().asType(ContractBatch.class);
      contractBatch = Beans.get(ContractBatchRepository.class).find(contractBatch.getId());
      if (contractBatch.getActionSelect() == ContractBatchRepository.REMINDER_END_OF_CONTRACTS
          && ((AppContract) Beans.get(AppService.class).getApp("contract"))
                  .getContractEndReminderTemplate()
              == null) {
        response.setError(I18n.get(IExceptionMessage.CONTRACT_END_REMINDER_TEMPLATE_NOT_DEFINED));
      } else {
        Batch batch = Beans.get(BatchContract.class).run(contractBatch);
        response.setFlash(batch.getComments());
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
