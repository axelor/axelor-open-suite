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
package com.axelor.apps.account.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AssistantReportInvoice;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AssistantReportInvoiceController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final DateTimeFormatter dtFormater = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  public void printSales(ActionRequest request, ActionResponse response) throws AxelorException {

    AssistantReportInvoice assistant = request.getContext().asType(AssistantReportInvoice.class);

    String name = I18n.get("SaleInvoicesDetails-") + getDateString(assistant);

    String fileLink =
        ReportFactory.createReport(IReport.SALE_INVOICES_DETAILS, name + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam(
                "Timezone",
                assistant.getCompany() != null ? assistant.getCompany().getTimezone() : null)
            .addParam("assistantId", assistant.getId())
            .addParam("companyId", assistant.getCompany().getId())
            .addParam("graphType", assistant.getGraphTypeSelect().toString())
            .addParam("turnoverTypeSelect", assistant.getTurnoverTypeSelect())
            .addFormat(assistant.getFormatSelect())
            .generate()
            .getFileLink();

    logger.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  private String getDateString(AssistantReportInvoice assistant) {
    return assistant.getFromDate().format(dtFormater) + assistant.getToDate().format(dtFormater);
  }

  public void printPurchases(ActionRequest request, ActionResponse response)
      throws AxelorException {

    AssistantReportInvoice assistant = request.getContext().asType(AssistantReportInvoice.class);

    String name = I18n.get("PurchaseInvoicesDetails-") + getDateString(assistant);

    String fileLink =
        ReportFactory.createReport(IReport.PURCHASE_INVOICES_DETAILS, name + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam(
                "Timezone",
                assistant.getCompany() != null ? assistant.getCompany().getTimezone() : null)
            .addParam("assistantId", assistant.getId())
            .addParam("companyId", assistant.getCompany().getId())
            .addParam("partnersIds", Joiner.on(",").join(assistant.getPartnerSet()))
            .addParam("productsIds", Joiner.on(",").join(assistant.getProductSet()))
            .addParam(
                "productCategoriesIds", Joiner.on(",").join(assistant.getProductCategorySet()))
            .addParam("graphType", assistant.getGraphTypeSelect().toString())
            .addFormat(assistant.getFormatSelect())
            .generate()
            .getFileLink();

    logger.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  public void clearProductSet(ActionRequest request, ActionResponse response)
      throws AxelorException {

    AssistantReportInvoice assistant = request.getContext().asType(AssistantReportInvoice.class);

    Company assistantCompany = assistant.getCompany();
    Set<Product> productSet = assistant.getProductSet();
    Set<Product> finalProductSet = new HashSet<>();

    if (productSet != null && assistantCompany != null) {
      for (Product product : productSet) {
        for (ProductCompany productCompany : product.getProductCompanyList()) {
          if (productCompany.getCompany() == assistantCompany) {
            finalProductSet.add(product);
            break;
          }
        }
      }

      response.setValue("productSet", finalProductSet);
    }
  }
}
