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
package com.axelor.apps.contract.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.batch.BatchStrategy;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.translation.ITranslation;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.List;

public class BatchContract extends BatchStrategy {

  protected ContractRepository repository;

  @Inject
  public BatchContract(ContractRepository repository) {
    this.repository = repository;
  }

  public static BatchContractFactory getFactory(int action) {
    switch (action) {
      case ContractBatchRepository.INVOICING:
        return Beans.get(BatchContractFactoryInvoicing.class);
      case ContractBatchRepository.TERMINATE:
        return Beans.get(BatchContractFactoryTerminate.class);
      case ContractBatchRepository.NEXT_VERSION_ACTIVATION:
        return Beans.get(BatchContractFactoryNextActivation.class);
      case ContractBatchRepository.CURRENT_VERSION_ACTIVATION:
        return Beans.get(BatchContractFactoryCurrentActivation.class);
      default:
        return null;
    }
  }

  @Override
  protected void process() {
    try {
      BatchContractFactory factory = getFactory(batch.getContractBatch().getActionSelect());
      Preconditions.checkNotNull(
          factory,
          String.format(
              I18n.get("Action %s has no Batch implementation."),
              batch.getContractBatch().getActionSelect()));

      Query<Contract> query = factory.prepare(batch);
      List<Contract> contracts;

      while (!(contracts = query.fetch(FETCH_LIMIT)).isEmpty()) {
        findBatch();
        for (Contract contract : contracts) {
          try {
            factory.process(contract);
            incrementDone(contract);
          } catch (Exception e) {
            TraceBackService.trace(e);
            incrementAnomaly(contract);
          }
        }
        JPA.clear();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      LOG.error(e.getMessage());
    }
  }

  protected void incrementDone(Contract contract) {
    contract.addBatchSetItem(batch);
    super.incrementDone();
  }

  protected void incrementAnomaly(Contract contract) {
    findBatch();
    contract = repository.find(contract.getId());
    contract.addBatchSetItem(batch);
    super.incrementAnomaly();
  }

  @Override
  protected void stop() {
    super.stop();
    addComment(
        String.format(
            I18n.get(ITranslation.CONTRACT_BATCH_EXECUTION_RESULT),
            batch.getDone(),
            batch.getAnomaly()));
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_CONTRACT_BATCH);
  }
}
