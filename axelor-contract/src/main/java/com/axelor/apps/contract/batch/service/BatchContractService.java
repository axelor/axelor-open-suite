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
package com.axelor.apps.contract.batch.service;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.contract.batch.BatchContract;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class BatchContractService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return ContractBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {
    ContractBatch contractBatch = (ContractBatch) model;
    return Beans.get(BatchContract.class).run(contractBatch);
  }
}
