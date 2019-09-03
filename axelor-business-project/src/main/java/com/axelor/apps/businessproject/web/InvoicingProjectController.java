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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.report.IReport;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InvoicingProjectController {

  @Inject protected InvoicingProjectService invoicingProjectService;

  @Inject protected InvoicingProjectRepository invoicingProjectRepo;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void generateInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException {
    InvoicingProject invoicingProject = request.getContext().asType(InvoicingProject.class);
    invoicingProject = invoicingProjectRepo.find(invoicingProject.getId());
    invoicingProject.setStatusSelect(InvoicingProjectRepository.STATUS_INVOICED);
    if (invoicingProject.getSaleOrderLineSet().isEmpty()
        && invoicingProject.getPurchaseOrderLineSet().isEmpty()
        && invoicingProject.getLogTimesSet().isEmpty()
        && invoicingProject.getExpenseLineSet().isEmpty()
        && invoicingProject.getElementsToInvoiceSet().isEmpty()
        && invoicingProject.getProjectSet().isEmpty()) {
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

    if (invoicingProject.getProject().getAssignedTo() == null) {
      throw new AxelorException(
          invoicingProject,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVOICING_PROJECT_USER));
    }
    Invoice invoice = invoicingProjectService.generateInvoice(invoicingProject);
    response.setReload(true);
    response.setView(
        ActionView.define("Invoice")
            .model(Invoice.class.getName())
            .add("form", "invoice-form")
            .param("forceEdit", "true")
            .context("_showRecord", String.valueOf(invoice.getId()))
            .map());
  }

  public void fillIn(ActionRequest request, ActionResponse response) throws AxelorException {
    InvoicingProject invoicingProject = request.getContext().asType(InvoicingProject.class);
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

  /**
   * Generates invoicing project minutes report
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void print(ActionRequest request, ActionResponse response) {
    InvoicingProject invoicingProject =
        invoicingProjectRepo.find(request.getContext().asType(InvoicingProject.class).getId());

    String name = I18n.get("Invoicing Project");

    try {
      String fileLink =
          ReportFactory.createReport(IReport.INVOICING_PROJECT, name + " - ${date}")
              .addParam("InvoicingProjectId", invoicingProject.getId())
              .addParam(
                  "Locale",
                  ReportSettings.getPrintingLocale(
                      invoicingProject.getProject().getClientPartner()))
              .addFormat(ReportSettings.FORMAT_PDF)
              .generate()
              .getFileLink();

      log.debug("Printing " + name);

      response.setView(ActionView.define(name).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
