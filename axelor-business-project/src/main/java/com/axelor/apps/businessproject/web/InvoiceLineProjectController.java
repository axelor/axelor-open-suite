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

import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.InvoiceLineProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceLineProjectController {

  /**
   * Set project from context selected lines
   *
   * @param request
   * @param response
   */
  public void setCustomerInvoiceLineProject(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());

      setCustomerInvoiceLineProject(request, response, project);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private void setCustomerInvoiceLineProject(
      ActionRequest request, ActionResponse response, Project project) {

    List<Map<String, Object>> customerInvoiceLineSet =
        (List<Map<String, Object>>) request.getContext().get("customerInvoiceLineSet");

    if (customerInvoiceLineSet == null || customerInvoiceLineSet.isEmpty()) {
      response.setFlash(IExceptionMessage.LINES_NOT_SELECTED);
    } else {
      List<Long> lineIds =
          customerInvoiceLineSet.stream()
              .map(it -> Long.parseLong(it.get("id").toString()))
              .collect(Collectors.toList());
      Beans.get(InvoiceLineProjectService.class).setProject(lineIds, project);
      response.setAttr("$customerInvoiceLineSet", "hidden", true);
      response.setAttr("addSelectedCustomerInvoiceLinesBtn", "hidden", true);
      response.setAttr("unlinkSelectedCustomerInvoiceLinesBtn", "hidden", true);
      response.setAttr("cancelManageCustomerInvoiceLinesBtn", "hidden", true);
      response.setAttr("customerInvoiceLinePanel", "refresh", true);
      response.setAttr("customerInvoicePanel", "refresh", true);
      response.setAttr("selectNewCustomerInvoiceLinesBtn", "readonly", false);
      response.setAttr("manageCustomerInvoiceLinesBtn", "readonly", false);
    }
  }

  /**
   * Remove project from selected lines
   *
   * @param request
   * @param response
   */
  public void unsetCustomerInvoiceLineProject(ActionRequest request, ActionResponse response) {

    try {
      setCustomerInvoiceLineProject(request, response, null);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  /**
   * Set project from context selected lines
   *
   * @param request
   * @param response
   */
  public void setSupplierInvoiceLineProject(ActionRequest request, ActionResponse response) {

    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());

      setSupplierInvoiceLineProject(request, response, project);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private void setSupplierInvoiceLineProject(
      ActionRequest request, ActionResponse response, Project project) {

    List<Map<String, Object>> supplierInvoiceLineSet =
        (List<Map<String, Object>>) request.getContext().get("supplierInvoiceLineSet");

    if (supplierInvoiceLineSet == null || supplierInvoiceLineSet.isEmpty()) {
      response.setFlash(IExceptionMessage.LINES_NOT_SELECTED);
    } else {
      List<Long> lineIds =
          supplierInvoiceLineSet.stream()
              .map(it -> Long.parseLong(it.get("id").toString()))
              .collect(Collectors.toList());
      Beans.get(InvoiceLineProjectService.class).setProject(lineIds, project);
      response.setAttr("$supplierInvoiceLineSet", "hidden", true);
      response.setAttr("addSelectedSupplierInvoiceLinesBtn", "hidden", true);
      response.setAttr("unlinkSelectedSupplierInvoiceLinesBtn", "hidden", true);
      response.setAttr("cancelManageSupplierInvoiceLinesBtn", "hidden", true);
      response.setAttr("supplierInvoiceLinePanel", "refresh", true);
      response.setAttr("supplierInvoicePanel", "refresh", true);
      response.setAttr("selectNewSupplierInvoiceLinesBtn", "readonly", false);
      response.setAttr("manageSupplierInvoiceLinesBtn", "readonly", false);
    }
  }

  /**
   * Remove project from selected lines
   *
   * @param request
   * @param response
   */
  public void unsetSupplierInvoiceLineProject(ActionRequest request, ActionResponse response) {

    try {
      setSupplierInvoiceLineProject(request, response, null);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
