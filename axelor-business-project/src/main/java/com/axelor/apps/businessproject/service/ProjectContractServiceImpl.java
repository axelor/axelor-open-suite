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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;

public class ProjectContractServiceImpl extends ContractServiceImpl {

  @Inject
  public ProjectContractServiceImpl(
      AppBaseService appBaseService,
      ContractVersionService versionService,
      ContractLineService contractLineService,
      DurationService durationService,
      ContractLineRepository contractLineRepo,
      ContractRepository contractRepository,
      TaxService taxService,
      ContractVersionRepository contractVersionRepository,
      InvoiceRepository invoiceRepository,
      InvoiceService invoiceService,
      AnalyticLineModelService analyticLineModelService) {
    super(
        appBaseService,
        versionService,
        contractLineService,
        durationService,
        contractLineRepo,
        contractRepository,
        taxService,
        contractVersionRepository,
        invoiceRepository,
        invoiceService,
        analyticLineModelService);
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
