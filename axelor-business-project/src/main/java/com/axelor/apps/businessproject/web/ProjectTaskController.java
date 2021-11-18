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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectTaskController {

  @Inject private ProjectTaskBusinessProjectService businessProjectService;

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
    ProjectTask task = request.getContext().asType(ProjectTask.class);
    ProjectTaskCategory projectTaskCategory = task.getProjectTaskCategory();
    try {
      task = businessProjectService.resetProjectTaskValues(task);
      if (projectTaskCategory != null) {
        task = businessProjectService.computeDefaultInformation(task);
      }

      if (task.getInvoicingType() == ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT) {
        task.setToInvoice(true);
      }
      response.setValues(task);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
