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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.supplychain.db.repo.InvoiceSupplychainRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import java.util.List;

public class InvoiceProjectRepository extends InvoiceSupplychainRepository {

  @Override
  public void remove(Invoice entity) {

    if (Beans.get(AppBusinessProjectService.class).isApp("business-project")) {
      List<InvoicingProject> invoiceProjectList =
          Beans.get(InvoicingProjectRepository.class)
              .all()
              .filter("self.invoice.id = ?", entity.getId())
              .fetch();
      List<ProjectTask> projectTaskList =
          Beans.get(ProjectTaskRepository.class)
              .all()
              .filter("self.invoiceLine.invoice = ?1", entity)
              .fetch();
      if (ObjectUtils.notEmpty(projectTaskList)) {
        for (ProjectTask projectTask : projectTaskList) {
          projectTask.setInvoiceLine(null);
        }
      }
      for (InvoicingProject invoiceProject : invoiceProjectList) {
        invoiceProject.setInvoice(null);
        invoiceProject.setStatusSelect(InvoicingProjectRepository.STATUS_DRAFT);
      }
    }

    super.remove(entity);
  }
}
