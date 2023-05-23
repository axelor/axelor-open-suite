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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.db.BusinessProjectBatch;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.BusinessProjectBatchRepository;
import com.axelor.apps.businessproject.service.batch.BusinessProjectBatchService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;
import java.util.Map;

public class BusinessProjectBatchController {

  public void runBatch(ActionRequest request, ActionResponse response) {
    try {
      BusinessProjectBatch businessProjectBatch =
          request.getContext().asType(BusinessProjectBatch.class);

      businessProjectBatch =
          Beans.get(BusinessProjectBatchRepository.class).find(businessProjectBatch.getId());
      BusinessProjectBatchService businessProjectBatchService =
          Beans.get(BusinessProjectBatchService.class);
      businessProjectBatchService.setBatchModel(businessProjectBatch);
      ControllerCallableTool<Batch> controllerCallableTool = new ControllerCallableTool<>();
      Batch batch =
          controllerCallableTool.runInSeparateThread(businessProjectBatchService, response);
      if (batch != null) {
        response.setInfo(batch.getComments());
      }
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
      values.put("model", ProjectTask.class.getName());
      values.put("grid", "business-project-project-task-grid");
      values.put("form", "project-task-form");
      values.put("search-filters", "project-task-filters");

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

  protected void showRecords(
      ActionRequest request, ActionResponse response, Map<String, Object> values)
      throws ClassNotFoundException {

    Batch batch = request.getContext().asType(Batch.class);
    batch = Beans.get(BatchRepository.class).find(batch.getId());

    String ids =
        Beans.get(BusinessProjectBatchService.class)
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
