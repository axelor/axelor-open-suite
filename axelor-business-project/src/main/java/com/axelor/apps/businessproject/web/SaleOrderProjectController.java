/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.businessproject.service.SaleOrderProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.List;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SaleOrderProjectController {

  @Inject protected SaleOrderProjectService saleOrderProjectService;

  @Inject protected InvoicingProjectService invoicingProjectService;

  @Inject protected SaleOrderRepository saleOrderRepo;

  public void generateProject(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    saleOrder = saleOrderRepo.find(saleOrder.getId());
    ProjectTask project = saleOrderProjectService.generateProject(saleOrder);

    response.setReload(true);
    response.setView(
        ActionView.define("Project")
            .model(ProjectTask.class.getName())
            .add("form", "project-form")
            .param("forceEdit", "true")
            .context("_showRecord", String.valueOf(project.getId()))
            .map());
  }

  public void generateTasks(ActionRequest request, ActionResponse response) throws AxelorException {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    saleOrder = saleOrderRepo.find(saleOrder.getId());
    if (saleOrder.getProject() == null) {
      throw new AxelorException(
          String.format(I18n.get(IExceptionMessage.SALE_ORDER_NO_PROJECT)),
          IException.CONFIGURATION_ERROR);
    }
    List<Long> listId = saleOrderProjectService.generateTasks(saleOrder);
    if (listId == null || listId.isEmpty()) {
      throw new AxelorException(
          String.format(I18n.get(IExceptionMessage.SALE_ORDER_NO_LINES)),
          IException.CONFIGURATION_ERROR);
    }
    response.setReload(true);
    if (listId.size() == 1) {
      response.setReload(true);
      response.setView(
          ActionView.define("Tasks generated")
              .model(ProjectTask.class.getName())
              .add("grid", "task-grid")
              .add("form", "task-form")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(listId.get(0)))
              .map());
    } else {
      response.setView(
          ActionView.define("Tasks generated")
              .model(ProjectTask.class.getName())
              .add("grid", "task-grid")
              .add("form", "task-form")
              .param("forceEdit", "true")
              .domain("self.id in (" + Joiner.on(",").join(listId) + ")")
              .map());
    }
  }

  public void generateInvoicingProject(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder =
        saleOrderRepo.find(Long.valueOf(request.getContext().get("_id").toString()));

    final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
    LocalDate deadline = dtf.parseLocalDate(request.getContext().get("deadline").toString());

    InvoicingProject invoicingProject =
        invoicingProjectService.createInvoicingProject(
            saleOrder,
            deadline,
            Integer.valueOf(request.getContext().get("invoicingTypeSelect").toString()));

    if (invoicingProject != null) {
      response.setCanClose(true);
      response.setFlash(I18n.get(IExceptionMessage.INVOICING_PROJECT_GENERATION));
      response.setView(
          ActionView.define(I18n.get("Invoicing project generated"))
              .model(InvoicingProject.class.getName())
              .add("form", "invoicing-project-form")
              .add("grid", "invoicing-project-grid")
              .context("_showRecord", String.valueOf(invoicingProject.getId()))
              .map());
    }
  }
}
