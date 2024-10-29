/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.LocalizationRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateHelper;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.businessproject.db.ProjectHoldBack;
import com.axelor.apps.businessproject.service.InvoiceServiceProject;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.invoice.InvoicePrintBusinessProjectService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

@Singleton
public class InvoiceController {

  public void updateLines(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      invoice = Beans.get(InvoiceServiceProject.class).updateLines(invoice);
      response.setValue("invoiceLineList", invoice.getInvoiceLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void exportAnnex(ActionRequest request, ActionResponse response) throws AxelorException {

    Invoice invoice =
        Beans.get(InvoiceRepository.class).find(request.getContext().asType(Invoice.class).getId());
    try {
      PrintingTemplate invoicingAnnexPrintTemplate =
          Beans.get(AppBusinessProjectService.class).getInvoicingAnnexPrintTemplate();

      String fileLink =
          Beans.get(PrintingTemplatePrintService.class)
              .getPrintLink(invoicingAnnexPrintTemplate, new PrintingGenFactoryContext(invoice));
      if (StringUtils.isEmpty(fileLink)) {
        return;
      }
      String title = I18n.get("Invoice");
      if (invoice.getInvoiceId() != null) {
        title += invoice.getInvoiceId();
      }
      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printExpenses(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    try {
      Invoice invoice =
          Beans.get(InvoiceRepository.class)
              .find(Long.parseLong(context.get("_invoiceId").toString()));

      Map<String, Object> localizationMap =
          context.get("localization") != null
              ? (Map<String, Object>) context.get("localization")
              : null;
      String locale =
          localizationMap != null && localizationMap.get("id") != null
              ? Beans.get(LocalizationRepository.class)
                  .find(Long.parseLong(localizationMap.get("id").toString()))
                  .getCode()
              : null;

      File expenseFile =
          Beans.get(InvoicePrintBusinessProjectService.class).printExpenses(invoice, locale);
      if (expenseFile != null) {
        response.setView(
            ActionView.define(I18n.get("Expense"))
                .add("html", PrintingTemplateHelper.getFileLink(expenseFile))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void loadRelatedHoldBacks(ActionRequest request, ActionResponse response) {

    Invoice invoice =
        Beans.get(InvoiceRepository.class).find(request.getContext().asType(Invoice.class).getId());
    List<ProjectHoldBack> projectHoldBacks =
        Beans.get(InvoicePrintBusinessProjectService.class).loadProjectHoldBacks(invoice);
    response.setValue("$projectHoldBackList", projectHoldBacks);
  }
}
