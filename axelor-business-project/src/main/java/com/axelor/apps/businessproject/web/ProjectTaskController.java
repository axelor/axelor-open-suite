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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTaskController {

  public void updateDiscount(ActionRequest request, ActionResponse response) {

    ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

    if (projectTask.getProduct() == null || projectTask.getProject() == null) {
      return;
    }

    try {
      projectTask = Beans.get(ProjectTaskBusinessProjectService.class).updateDiscount(projectTask);

      response.setValue("discountTypeSelect", projectTask.getDiscountTypeSelect());
      response.setValue("discountAmount", projectTask.getDiscountAmount());
      response.setValue("priceDiscounted", projectTask.getPriceDiscounted());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {
    ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

    try {
      projectTask = Beans.get(ProjectTaskBusinessProjectService.class).compute(projectTask);
      response.setValue("priceDiscounted", projectTask.getPriceDiscounted());
      response.setValue("exTaxTotal", projectTask.getExTaxTotal());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Invert value of 'toInvoice' field and save the record
   *
   * @param request
   * @param response
   */
  @Transactional
  public void updateToInvoice(ActionRequest request, ActionResponse response) {
    ProjectTaskRepository projectTaskRepository = Beans.get(ProjectTaskRepository.class);
    try {
      ProjectTask projectTask = request.getContext().asType(ProjectTask.class);
      projectTask = projectTaskRepository.find(projectTask.getId());
      projectTask.setToInvoice(!projectTask.getToInvoice());
      projectTaskRepository.save(projectTask);
      response.setValue("toInvoice", projectTask.getToInvoice());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onChangeCategory(ActionRequest request, ActionResponse response) {
    try {
      ProjectTask task = request.getContext().asType(ProjectTask.class);
      if (task.getSaleOrderLine() == null && task.getInvoiceLine() == null) {
        ProjectTaskBusinessProjectService projectTaskBusinessProjectService =
            Beans.get(ProjectTaskBusinessProjectService.class);
        task = projectTaskBusinessProjectService.resetProjectTaskValues(task);
        if (task.getProjectTaskCategory() != null) {
          task = projectTaskBusinessProjectService.updateTaskFinancialInfo(task);
        }
        response.setValues(task);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getPercentageOfProgress(ActionRequest request, ActionResponse response) {
    Map<String, Object> data = new HashMap<>();
    data.put("progress", request.getData().get("displayProgress"));
    data.put("label", I18n.get("% of progress"));
    response.setData(List.of(data));
  }

  public void getPercentageOfConsumption(ActionRequest request, ActionResponse response) {
    Map<String, Object> data = new HashMap<>();
    data.put("consumption", request.getData().get("displayConsumption"));
    data.put("label", I18n.get("% of consumption"));
    response.setData(List.of(data));
  }

  public void getRemainingToDo(ActionRequest request, ActionResponse response) {
    Map<String, Object> data = new HashMap<>();
    data.put("remaining", request.getData().get("displayRemaining"));
    data.put("label", I18n.get("Remaining amount to do"));
    response.setData(List.of(data));
  }

  public void getProjectTaskFinancialReportingData(ActionRequest request, ActionResponse response) {
    Map<String, Object> data = new HashMap<>();
    data.put("turnover", request.getData().get("turnover"));
    data.put("initialCosts", request.getData().get("initialCosts"));
    data.put("initialMargin", request.getData().get("initialMargin"));
    data.put("initialMarkup", request.getData().get("initialMarkup"));
    data.put("realTurnover", request.getData().get("realTurnover"));
    data.put("realCosts", request.getData().get("realCosts"));
    data.put("realMargin", request.getData().get("realMargin"));
    data.put("realMarkup", request.getData().get("realMarkup"));
    data.put("forecastCosts", request.getData().get("forecastCosts"));
    data.put("forecastMargin", request.getData().get("forecastMargin"));
    data.put("forecastMarkup", request.getData().get("forecastMarkup"));
    response.setData(List.of(data));
  }
}
