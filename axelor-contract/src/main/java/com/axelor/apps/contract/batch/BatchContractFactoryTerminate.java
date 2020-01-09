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

public class BatchContractFactoryTerminate extends BatchContractFactory {

  @Inject
  public BatchContractFactoryTerminate(
      ContractRepository repository, ContractService service, AppBaseService baseService) {
    super(repository, service, baseService);
  }

  @Override
  Query<Contract> prepare(Batch batch) {
    return repository
        .all()
        .filter(
            "(self.terminatedDate <= :date "
                + " OR self.currentContractVersion.supposedEndDate <= :date)"
                + " AND self.statusSelect = :status"
                + " AND :batch NOT MEMBER of self.batchSet")
        .bind("date", baseService.getTodayDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .bind("status", ContractRepository.ACTIVE_CONTRACT)
        .bind("batch", batch);
  }

  @Override
  void process(Contract contract) throws AxelorException {
    service.terminateContract(contract, false, baseService.getTodayDate());
  }
}
