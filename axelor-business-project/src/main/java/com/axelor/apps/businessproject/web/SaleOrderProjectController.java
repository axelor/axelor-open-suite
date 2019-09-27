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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.businessproject.service.SaleOrderProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.tool.StringTool;
import com.axelor.auth.db.AuditableModel;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Singleton
public class SaleOrderProjectController {

  private static final String CONTEXT_SHOW_RECORD = "_showRecord";

  @Inject protected SaleOrderProjectService saleOrderProjectService;

  @Inject protected InvoicingProjectService invoicingProjectService;

  @Inject protected SaleOrderRepository saleOrderRepo;

  public void generateProject(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    saleOrder = saleOrderRepo.find(saleOrder.getId());
    String genProjTypePerOrderLine = (String) request.getContext().get("genProjTypePerOrderLine");
    try {
      Project project = saleOrderProjectService.generateProject(saleOrder, genProjTypePerOrderLine);
      response.setReload(true);
      response.setView(
          ActionView.define("Project")
              .model(Project.class.getName())
              .add("form", "project-form")
              .param("forceEdit", "true")
              .context(CONTEXT_SHOW_RECORD, String.valueOf(project.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateTypePerOrderLineForProject(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    saleOrder = saleOrderRepo.find(saleOrder.getId());
    try {
      List<? extends AuditableModel> models =
          saleOrderProjectService.generateProjectTypePerOrderLine(saleOrder);
      ActionView.ActionViewBuilder actionView;
      switch (saleOrder.getProject().getGenProjTypePerOrderLine()) {
        case PHASE_BY_LINE:
          actionView =
              ActionView.define(
                      String.format("Project%s generated", (models.size() > 1 ? "s" : "")))
                  .model(Project.class.getName())
                  .add("grid", "project-grid")
                  .add("form", "project-form");
          break;
        case TASK_BY_LINE:
          actionView =
              ActionView.define(String.format("Task%s generated", (models.size() > 1 ? "s" : "")))
                  .model(TeamTask.class.getName())
                  .add("grid", "team-task-grid")
                  .add("form", "team-task-form");
          break;
        default:
          actionView = ActionView.define("Model generated").model(models.getClass().getName());
      }
      if (models.size() == 1) {
        actionView.context(CONTEXT_SHOW_RECORD, String.valueOf(models.get(0).getId()));
      } else {
        actionView.domain(String.format("self.id in (%s)", StringTool.getIdListString(models)));
      }
      actionView.param("forceEdit", "true");
      response.setReload(true);
      response.setView(actionView.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateInvoicingProject(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = saleOrderRepo.find(request.getContext().asType(SaleOrder.class).getId());
    LocalDate deadline = null;
    if (request.getContext().get("deadline") != null) {
      deadline =
          LocalDate.parse(
              request.getContext().get("deadline").toString(), DateTimeFormatter.ISO_DATE);
    }
    InvoicingProject invoicingProject =
        invoicingProjectService.createInvoicingProject(
            saleOrder,
            deadline,
            Integer.valueOf(request.getContext().get("operationSelect").toString()));
    if (invoicingProject != null) {
      response.setCanClose(true);
      response.setFlash(I18n.get(IExceptionMessage.INVOICING_PROJECT_GENERATION));
      response.setView(
          ActionView.define(I18n.get("Invoicing project generated"))
              .model(InvoicingProject.class.getName())
              .add("form", "invoicing-project-form")
              .add("grid", "invoicing-project-grid")
              .context(CONTEXT_SHOW_RECORD, String.valueOf(invoicingProject.getId()))
              .map());
    }
  }
}
