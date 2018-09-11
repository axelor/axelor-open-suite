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

import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.PurchaseOrderLineProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;

public class PurchaseOrderLineProjectController {

  @Inject private PurchaseOrderLineProjectService purchaseOrderLineProjectService;

  @Inject private ProjectRepository projectRepository;

  @Inject private PurchaseOrderLineRepository purchaseOrderLineRepo;

  /**
   * Set project from context selected lines
   *
   * @param request
   * @param response
   */
  public void setProject(ActionRequest request, ActionResponse response) {

    try {
      Map<String, Object> projectMap = (Map<String, Object>) request.getContext().get("_project");

      Project project = null;
      if (projectMap != null && projectMap.get("id") != null) {
        project = projectRepository.find(Long.parseLong(projectMap.get("id").toString()));
      }

      if (project == null) {
        response.setFlash(IExceptionMessage.NO_PROJECT_IN_CONTEXT);
        return;
      }

      List<Long> lineIds = (List<Long>) request.getContext().get("_ids");

      if (lineIds.isEmpty()) {
        response.setFlash(IExceptionMessage.LINES_NOT_SELECTED);
      } else {
        purchaseOrderLineProjectService.setProject(lineIds, project);
        response.setCanClose(true);
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  /**
   * Remove project from selected lines
   *
   * @param request
   * @param response
   */
  public void unsetProject(ActionRequest request, ActionResponse response) {

    try {
      List<Long> lineIds = (List<Long>) request.getContext().get("_ids");

      if (lineIds.isEmpty()) {
        response.setFlash(IExceptionMessage.LINES_NOT_SELECTED);
      } else {
        purchaseOrderLineProjectService.setProject(lineIds, null);
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
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
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      purchaseOrderLine = purchaseOrderLineRepo.find(purchaseOrderLine.getId());
      purchaseOrderLine.setToInvoice(!purchaseOrderLine.getToInvoice());
      purchaseOrderLineRepo.save(purchaseOrderLine);
      response.setValue("toInvoice", purchaseOrderLine.getToInvoice());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
