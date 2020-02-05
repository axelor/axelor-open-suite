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
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.AppBusinessProject;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.service.TeamTaskProjectService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TeamTaskBusinessProjectService extends TeamTaskProjectService {

  TeamTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo) throws AxelorException ;

  TeamTask create(TaskTemplate template, Project project, LocalDateTime date, BigDecimal qty);

  TeamTask updateDiscount(TeamTask teamTask);

  TeamTask compute(TeamTask teamTask);

  List<InvoiceLine> createInvoiceLines(Invoice invoice, List<TeamTask> teamTaskList, int priority)
      throws AxelorException;

  List<InvoiceLine> createInvoiceLine(Invoice invoice, TeamTask teamTask, int priority)
      throws AxelorException;

  TeamTask computeDefaultInformation(TeamTask teamTask) throws AxelorException ;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  TeamTask updateTask(TeamTask teamTask, AppBusinessProject appBusinessProject) throws AxelorException ;

  TeamTask resetTeamTaskValues(TeamTask teamTask);
}
