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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.project.db.Project;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class ProjectContractServiceImpl extends ContractServiceImpl {

  @Inject
  public ProjectContractServiceImpl(
      AppBaseService appBaseService,
      ContractVersionService versionService,
      ContractLineService contractLineService,
      DurationService durationService,
      ContractLineRepository contractLineRepo,
      ConsumptionLineRepository consumptionLineRepo,
      ContractRepository contractRepository) {
    super(
        appBaseService,
        versionService,
        contractLineService,
        durationService,
        contractLineRepo,
        consumptionLineRepo,
        contractRepository);
  }

  @Override
  public Invoice generateInvoice(Contract contract) throws AxelorException {
    Invoice invoice = super.generateInvoice(contract);
    Project project = contract.getProject();

    if (project != null && project.getIsBusinessProject()) {
      invoice.setProject(project);
    }

    return invoice;
  }
}
