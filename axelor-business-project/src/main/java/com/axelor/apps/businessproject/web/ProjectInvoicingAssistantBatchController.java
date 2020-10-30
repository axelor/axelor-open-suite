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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.ProjectInvoicingAssistantBatch;
import com.axelor.apps.businessproject.db.repo.ProjectInvoicingAssistantBatchRepository;
import com.axelor.apps.businessproject.service.batch.ProjectInvoicingAssistantBatchService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.TeamTask;
import java.util.HashMap;
import java.util.Map;

public class ProjectInvoicingAssistantBatchController {

  public void actionUpdateTask(ActionRequest request, ActionResponse response) {
    try {
      ProjectInvoicingAssistantBatch projectInvoicingAssistantBatch =
          request.getContext().asType(ProjectInvoicingAssistantBatch.class);

      projectInvoicingAssistantBatch =
          Beans.get(ProjectInvoicingAssistantBatchRepository.class)
              .find(projectInvoicingAssistantBatch.getId());

      Batch batch =
          Beans.get(ProjectInvoicingAssistantBatchService.class)
              .updateTask(projectInvoicingAssistantBatch);
      response.setFlash(batch.getComments());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void showUpdatedTask(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("field", "updatedTaskSet");
      values.put("title", I18n.get("Updated tasks"));
      values.put("model", TeamTask.class.getName());
      values.put("grid", "business-project-team-task-grid");
      values.put("form", "team-task-form");
      values.put("search-filters", "team-task-filters");

      this.showRecords(request, response, values);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showUpdatedTimesheetLine(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("field", "updatedTimesheetLineSet");
      values.put("title", I18n.get("Updated timesheet lines"));
      values.put("model", TimesheetLine.class.getName());
      values.put("grid", "timesheet-line-project-grid");
      values.put("form", "timesheet-line-project-form");

      this.showRecords(request, response, values);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void actionGenerateInvoicingProject(ActionRequest request, ActionResponse response) {
    try {
      ProjectInvoicingAssistantBatch projectInvoicingAssistantBatch =
          request.getContext().asType(ProjectInvoicingAssistantBatch.class);

      projectInvoicingAssistantBatch =
          Beans.get(ProjectInvoicingAssistantBatchRepository.class)
              .find(projectInvoicingAssistantBatch.getId());

      Batch batch =
          Beans.get(ProjectInvoicingAssistantBatchService.class)
              .generateInvoicingProject(projectInvoicingAssistantBatch);

      response.setFlash(batch.getComments());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void showGeneratedInvoicingProject(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("field", "generatedInvoicingProjectSet");
      values.put("title", I18n.get("Generated invoicing projects"));
      values.put("model", InvoicingProject.class.getName());
      values.put("grid", "invoicing-project-grid");
      values.put("form", "invoicing-project-form");
      values.put("search-filters", "invoicing-project-filters");

      this.showRecords(request, response, values);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private void showRecords(
      ActionRequest request, ActionResponse response, Map<String, Object> values)
      throws ClassNotFoundException {

    Batch batch = request.getContext().asType(Batch.class);
    batch = Beans.get(BatchRepository.class).find(batch.getId());

    String ids =
        Beans.get(ProjectInvoicingAssistantBatchService.class)
            .getShowRecordIds(batch, values.get("field").toString());

    response.setView(
        ActionView.define(values.get("title").toString())
            .model(values.get("model").toString())
            .add("grid", values.get("grid").toString())
            .add("form", values.get("form").toString())
            .domain("self.id IN (" + ids + ")")
            .map());
  }
}
