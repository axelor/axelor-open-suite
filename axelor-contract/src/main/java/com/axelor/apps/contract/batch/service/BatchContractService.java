/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.batch.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.contract.batch.BatchContract;
import com.axelor.apps.contract.batch.BatchContractRevaluate;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.db.Model;
import com.axelor.inject.Beans;

public class BatchContractService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return ContractBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {
    Batch batch;
    ContractBatch contractBatch = (ContractBatch) model;
    switch (contractBatch.getActionSelect()) {
      case ContractBatchRepository.REVALUATE_CONTRACTS:
        batch = revaluateContracts(contractBatch);
        break;
      default:
        batch = Beans.get(BatchContract.class).run(contractBatch);
    }

    return batch;
  }

  protected Batch revaluateContracts(ContractBatch contractBatch) {
    return Beans.get(BatchContractRevaluate.class).run(contractBatch);
  }
}
