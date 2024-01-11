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
package com.axelor.apps.contract.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.contract.db.repo.ContractBatchContractRepository;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractLineManagementRepository;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.AnalyticMoveLineContractServiceImpl;
import com.axelor.apps.contract.service.ConsumptionLineService;
import com.axelor.apps.contract.service.ConsumptionLineServiceImpl;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.contract.service.ContractVersionServiceImpl;
import com.axelor.apps.contract.service.InvoiceLineAnalyticContractServiceImpl;
import com.axelor.apps.contract.service.WorkflowCancelServiceContractImpl;
import com.axelor.apps.supplychain.service.AnalyticMoveLineSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceLineAnalyticSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowCancelServiceSupplychainImpl;

public class ContractModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AbstractContractRepository.class).to(ContractRepository.class);
    bind(ContractService.class).to(ContractServiceImpl.class);
    bind(ContractVersionService.class).to(ContractVersionServiceImpl.class);
    bind(ContractLineService.class).to(ContractLineServiceImpl.class);
    bind(ConsumptionLineService.class).to(ConsumptionLineServiceImpl.class);
    bind(ContractBatchRepository.class).to(ContractBatchContractRepository.class);
    bind(AnalyticMoveLineSupplychainServiceImpl.class)
        .to(AnalyticMoveLineContractServiceImpl.class);
    bind(InvoiceLineAnalyticSupplychainServiceImpl.class)
        .to(InvoiceLineAnalyticContractServiceImpl.class);
    bind(WorkflowCancelServiceSupplychainImpl.class).to(WorkflowCancelServiceContractImpl.class);
    bind(ContractLineRepository.class).to(ContractLineManagementRepository.class);
  }
}
