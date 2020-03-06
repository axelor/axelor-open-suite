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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class InvoicingProjectController {

  @Inject private InvoicingProjectRepository invoicingProjectRepository;

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

  @SuppressWarnings("unchecked")
  public void generateInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Invoice invoice;
    List<Long> invoiceIdList = new ArrayList<Long>();
    List<InvoicingProject> projects = new ArrayList<InvoicingProject>();
    String ids = null;
    if (request.getContext().get("_ids") != null) {
      projects =
          invoicingProjectRepository
              .all()
              .filter(
                  "self.id in ? and self.invoice = null",
                  (List<Integer>) request.getContext().get("_ids"))
              .fetch();
    } else if (request.getContext().asType(InvoicingProject.class).getId() != null) {
      projects.add(
          invoicingProjectRepository.find(
              request.getContext().asType(InvoicingProject.class).getId()));
    } else {
      response.setError(IExceptionMessage.LINES_NOT_SELECTED);
      return;
    }
    if (projects.size() > 0) {
      for (InvoicingProject invProject : projects) {
        invoice = Beans.get(InvoicingProjectService.class).generateInvoice(invProject);
        if (invoice != null) {
          invoiceIdList.add(invoice.getId());
          try {
            Beans.get(InvoicingProjectService.class).generateAnnex(invProject);
          } catch (IOException e) {
            TraceBackService.trace(response, e);
          }
        }
      }
      ids = StringUtils.join(invoiceIdList, ",");
      ActionViewBuilder view =
          ActionView.define(I18n.get("Invoice"))
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .add("form", "invoice-form");
      response.setReload(true);
      response.setView(
          (ids.contains(","))
              ? view.domain("self.id IN (" + ids + ")").map()
              : view.context("_showRecord", ids).map());
    }
  }
}
