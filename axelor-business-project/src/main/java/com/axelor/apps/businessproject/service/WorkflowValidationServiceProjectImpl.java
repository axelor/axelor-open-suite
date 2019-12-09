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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.apps.supplychain.service.workflow.WorkflowValidationServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class WorkflowValidationServiceProjectImpl extends WorkflowValidationServiceSupplychainImpl {

  @Inject InvoicingProjectRepository invoicingProjectRepo;

  @Inject
  public WorkflowValidationServiceProjectImpl(IntercoService intercoService) {
    super(intercoService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterValidation(Invoice invoice) throws AxelorException {
    super.afterValidation(invoice);

    InvoicingProject invoicingProject =
        invoicingProjectRepo.all().filter("self.invoice = ?", invoice.getId()).fetchOne();

    if (invoicingProject != null) {
      invoicingProject.setStatusSelect(InvoicingProjectRepository.STATUS_VALIDATED);
      invoicingProjectRepo.save(invoicingProject);
    }
  }
}
