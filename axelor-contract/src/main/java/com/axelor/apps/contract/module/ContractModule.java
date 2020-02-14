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
package com.axelor.apps.contract.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.contract.db.repo.ContractBatchContractRepository;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ConsumptionLineService;
import com.axelor.apps.contract.service.ConsumptionLineServiceImpl;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.contract.service.ContractVersionServiceImpl;

public class ContractModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AbstractContractRepository.class).to(ContractRepository.class);
    bind(ContractService.class).to(ContractServiceImpl.class);
    bind(ContractVersionService.class).to(ContractVersionServiceImpl.class);
    bind(ContractLineService.class).to(ContractLineServiceImpl.class);
    bind(ConsumptionLineService.class).to(ConsumptionLineServiceImpl.class);
    bind(ContractBatchRepository.class).to(ContractBatchContractRepository.class);
  }
}
