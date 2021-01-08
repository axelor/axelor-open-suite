/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.AppBusinessProject;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ProjectTaskBusinessProjectService extends ProjectTaskService {

  ProjectTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo)
      throws AxelorException;

  ProjectTask create(TaskTemplate template, Project project, LocalDateTime date, BigDecimal qty);

  ProjectTask updateDiscount(ProjectTask projectTask);

  ProjectTask compute(ProjectTask projectTask);

  List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ProjectTask> projectTaskList, int priority) throws AxelorException;

  List<InvoiceLine> createInvoiceLine(Invoice invoice, ProjectTask projectTask, int priority)
      throws AxelorException;

  ProjectTask computeDefaultInformation(ProjectTask projectTask) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  ProjectTask updateTask(ProjectTask projectTask, AppBusinessProject appBusinessProject)
      throws AxelorException;

  ProjectTask resetProjectTaskValues(ProjectTask projectTask);
}
