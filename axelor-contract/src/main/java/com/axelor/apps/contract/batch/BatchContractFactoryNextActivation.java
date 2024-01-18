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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.time.format.DateTimeFormatter;

public class BatchContractFactoryNextActivation extends BatchContractFactory {

  @Inject
  public BatchContractFactoryNextActivation(
      ContractRepository repository, ContractService service, AppBaseService baseService) {
    super(repository, service, baseService);
  }

  @Override
  Query<Contract> prepare(Batch batch) {
    return repository
        .all()
        .filter(
            "self.nextVersion.supposedActivationDate <= :date "
                + "AND self.nextVersion.statusSelect = :status "
                + "AND :batch NOT MEMBER of self.batchSet")
        .bind(
            "date",
            baseService
                .getTodayDate(batch.getContractBatch().getCompany())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .bind("status", ContractVersionRepository.WAITING_VERSION)
        .bind("batch", batch);
  }

  @Override
  void process(Contract contract) throws AxelorException {
    service.activeNextVersion(contract, baseService.getTodayDate(contract.getCompany()));
  }
}
