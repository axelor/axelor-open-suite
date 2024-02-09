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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.Query;
import com.google.inject.Inject;

public class BatchContractFactoryInvoicing extends BatchContractFactory {

  protected Batch batch;

  @Inject
  public BatchContractFactoryInvoicing(
      ContractRepository repository, ContractService service, AppBaseService baseService) {
    super(repository, service, baseService);
  }

  @Override
  public Query<Contract> prepare(Batch batch) {
    this.batch = batch;
    return repository
        .all()
        .filter(this.prepareFilter(true))
        .bind("date", batch.getContractBatch().getInvoicingDate())
        .bind("batch", batch)
        .bind("targetTypeSelect", batch.getContractBatch().getTargetTypeSelect())
        .bind("statusSelect", AbstractContractRepository.CLOSED_CONTRACT);
  }

  @Override
  public void process(Contract contract) throws AxelorException {
    Invoice invoice = service.invoicingContract(contract);
    if (invoice != null && batch != null) {
      invoice.addBatchSetItem(batch);
    }
  }

  /**
   * To prepare filter that is to be used while running batch, set considerBatch = true
   *
   * <p>OR
   *
   * <p>To display contracts that would be treated by the batch, set considerBatch = false
   *
   * @param considerBatch
   * @return
   */
  public String prepareFilter(boolean considerBatch) {
    StringBuilder filter = new StringBuilder();
    filter.append(
        "self.isInvoicingManagement = TRUE "
            + "AND self.currentContractVersion.automaticInvoicing = TRUE "
            + "AND self.invoicingDate <= :date "
            + "AND self.statusSelect != :statusSelect "
            + "AND self.targetTypeSelect = :targetTypeSelect ");

    filter.append(
        considerBatch ? "AND :batch NOT MEMBER of self.batchSet" : "AND self.batchSet IS EMPTY");

    return filter.toString();
  }
}
