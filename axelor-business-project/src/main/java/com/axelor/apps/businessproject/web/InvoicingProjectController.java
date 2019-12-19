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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class InvoicingProjectController {

  @SuppressWarnings("unchecked")
  public void generateInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException {
    List<Integer> ids = new ArrayList<Integer>();
    List<Long> invoiceIdsList = new ArrayList<Long>();
    Invoice invoice = null;
    int checkInvoice = 0;

    if (request.getContext().get("_ids") != null) {
      ids = (List<Integer>) request.getContext().get("_ids");
    }

    InvoicingProject invoicingProject = request.getContext().asType(InvoicingProject.class);

    if (ObjectUtils.isEmpty(ids) && invoicingProject.getId() != null) {
      ids.add(Math.toIntExact(invoicingProject.getId()));
    }

    if (ObjectUtils.isEmpty(ids) && ObjectUtils.isEmpty(invoicingProject.getId())) {
      response.setError(com.axelor.apps.base.exceptions.IExceptionMessage.RECORD_NONE_SELECTED);
      return;
    }

    for (Integer id : ids) {
      invoicingProject = Beans.get(InvoicingProjectRepository.class).find((long) id);
      try {
        if (invoicingProject.getStatusSelect() == 1) {
          checkInvoice++;
          if (invoicingProject.getSaleOrderLineSet().isEmpty()
              && invoicingProject.getPurchaseOrderLineSet().isEmpty()
              && invoicingProject.getLogTimesSet().isEmpty()
              && invoicingProject.getExpenseLineSet().isEmpty()
              && invoicingProject.getProjectSet().isEmpty()
              && invoicingProject.getTeamTaskSet().isEmpty()) {
            throw new AxelorException(
                invoicingProject,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(IExceptionMessage.INVOICING_PROJECT_EMPTY));
          }
          if (invoicingProject.getProject() == null) {
            throw new AxelorException(
                invoicingProject,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT));
          }
          if (invoicingProject.getProject().getClientPartner() == null) {
            throw new AxelorException(
                invoicingProject,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT_PARTNER));
          }

          invoice = Beans.get(InvoicingProjectService.class).generateInvoice(invoicingProject);
          invoiceIdsList.add(invoice.getId());
          try {
            if (invoice != null) {
              Beans.get(InvoicingProjectService.class).generateAnnex(invoicingProject);
            }
          } catch (IOException e) {
            TraceBackService.trace(e);
          }
          response.setReload(true);
        }
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    if (checkInvoice == 0) {
      response.setError(IExceptionMessage.RECORD_NONE_GENRATE);
      return;
    }
    if (request.getContext().get("_ids") != null) {
      String invoiceIds = invoiceIdsList.toString().replace("[", "(").replace("]", ")");

      response.setView(
          ActionView.define("Invoice")
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .param("forceEdit", "true")
              .domain("self.id in " + invoiceIds)
              .map());
    } 
    else if(invoice != null && invoice.getId() != null){
      response.setView(
          ActionView.define("Invoice")
              .model(Invoice.class.getName())
              .add("form", "invoice-form")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(invoice.getId()))
              .map());
    }
  }

  public void fillIn(ActionRequest request, ActionResponse response) throws AxelorException {
    InvoicingProject invoicingProject = request.getContext().asType(InvoicingProject.class);
    InvoicingProjectService invoicingProjectService = Beans.get(InvoicingProjectService.class);
    Project project = invoicingProject.getProject();
    if (project == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVOICING_PROJECT_PROJECT));
    }
    invoicingProjectService.clearLines(invoicingProject);
    invoicingProjectService.setLines(invoicingProject, project, 0);
    response.setValues(invoicingProject);
  }
}
