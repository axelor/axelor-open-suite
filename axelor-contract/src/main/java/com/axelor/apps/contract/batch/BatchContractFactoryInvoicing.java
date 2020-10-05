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
package com.axelor.apps.contract.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.time.format.DateTimeFormatter;

public class BatchContractFactoryInvoicing extends BatchContractFactory {

  @Inject
  public BatchContractFactoryInvoicing(
      ContractRepository repository, ContractService service, AppBaseService baseService) {
    super(repository, service, baseService);
  }

  @Override
  public Query<Contract> prepare(Batch batch) {
    return repository
        .all()
        .filter(
            "self.isInvoicingManagement = TRUE "
                + "AND self.currentContractVersion.automaticInvoicing = TRUE "
                + "AND self.invoicingDate <= :date "
                + "AND :batch NOT MEMBER of self.batchSet")
        .bind(
            "date",
            baseService
                .getTodayDate(batch.getContractBatch().getCompany())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .bind("batch", batch);
  }

  @Override
  public void process(Contract contract) throws AxelorException {
    service.invoicingContract(contract);
  }
}
