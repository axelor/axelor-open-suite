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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppBusinessProject;
import com.axelor.utils.QueryBuilder;
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

  ProjectTask updateTaskFinancialInfo(ProjectTask projectTask) throws AxelorException;

  QueryBuilder<ProjectTask> getTaskInvoicingFilter();

  void taskInvoicing(Project project, AppBusinessProject appBusinessProject);

  @Transactional
  ProjectTask updateTaskToInvoice(ProjectTask projectTask, AppBusinessProject appBusinessProject);

  ProjectTask resetProjectTaskValues(ProjectTask projectTask);

  @Transactional(rollbackOn = {Exception.class})
  ProjectTask setProjectTaskValues(ProjectTask projectTask) throws AxelorException;
}
