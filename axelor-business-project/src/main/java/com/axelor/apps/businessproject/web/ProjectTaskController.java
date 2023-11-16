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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.PurchaseOrderProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderFromSaleOrderLinesService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
      response.setValue("totalCosts", projectTask.getTotalCosts());
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

  public void getProjectTaskTimeFollowUpData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_TASK_REPORT_NO_ID_FOUND);
    }
    Map<String, Object> data =
        Beans.get(ProjectTaskBusinessProjectService.class)
            .processRequestToDisplayTimeReporting(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void getProjectTaskFinancialReportingData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_TASK_REPORT_NO_ID_FOUND);
    }
    Map<String, Object> data =
        Beans.get(ProjectTaskBusinessProjectService.class)
            .processRequestToDisplayFinancialReporting(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void generatePurchaseOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTask projectTask = null;
    if (request.getContext().get("_projectTaskId") != null) {
      projectTask =
          Beans.get(ProjectTaskRepository.class)
              .find(Long.valueOf((Integer) request.getContext().get("_projectTaskId")));
    } else {
      projectTask = request.getContext().asType(ProjectTask.class);
    }
    SaleOrderLine saleOrderLine = projectTask.getSaleOrderLine();
    Partner supplierPartner = null;
    String saleOrderLinesIdStr = null;

    if (request.getContext().get("supplierPartnerSelect") != null) {
      supplierPartner =
          JPA.em()
              .find(
                  Partner.class,
                  Long.valueOf(
                      (Integer)
                          ((Map) request.getContext().get("supplierPartnerSelect")).get("id")));
      saleOrderLinesIdStr = (String) request.getContext().get("saleOrderLineIdSelected");
    }
    Map<String, Object> view = null;

    if (saleOrderLine != null) {
      SaleOrder saleOrder = saleOrderLine.getSaleOrder();
      List<SaleOrderLine> saleOrderLines = List.of(saleOrderLine);
      view =
          Beans.get(PurchaseOrderFromSaleOrderLinesService.class)
              .generatePurchaseOrdersFromSOLines(
                  saleOrder, saleOrderLines, supplierPartner, saleOrderLinesIdStr);
    } else {
      view =
          Beans.get(PurchaseOrderProjectService.class)
              .generateEmptyPurchaseOrderFromProjectTask(projectTask, supplierPartner);
    }

    ((Map) view.get("context")).put("_projectTaskId", projectTask.getId());
    response.setView(view);
    if (supplierPartner != null) {
      Long purchaseOrderId =
          Long.parseLong((String) ((Map) view.get("context")).get("_showRecord"));
      Beans.get(PurchaseOrderProjectService.class)
          .setProjectAndProjectTask(purchaseOrderId, projectTask.getProject(), projectTask);
      response.setCanClose(true);
    }
  }
}
