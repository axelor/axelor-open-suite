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
package com.axelor.apps.purchase.service.print;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.exception.IExceptionMessage;
import com.axelor.apps.purchase.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.tool.ModelTool;
import com.axelor.apps.tool.ThrowConsumer;
import com.axelor.apps.tool.file.PdfTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderPrintServiceImpl implements PurchaseOrderPrintService {

  @Override
  public String printPurchaseOrder(PurchaseOrder purchaseOrder, String formatPdf)
      throws AxelorException {
    String fileName = getPurchaseOrderFilesName(false, formatPdf);
    return PdfTool.getFileLinkFromPdfFile(print(purchaseOrder, formatPdf), fileName);
  }

  @Override
  public String printPurchaseOrders(List<Long> ids) throws IOException {
    List<File> printedPurchaseOrders = new ArrayList<>();
    ModelTool.apply(
        PurchaseOrder.class,
        ids,
        new ThrowConsumer<PurchaseOrder>() {

          @Override
          public void accept(PurchaseOrder purchaseOrder) throws Exception {
            printedPurchaseOrders.add(print(purchaseOrder, ReportSettings.FORMAT_PDF));
          }
        });
    String fileName = getPurchaseOrderFilesName(true, ReportSettings.FORMAT_PDF);
    return PdfTool.mergePdfToFileLink(printedPurchaseOrders, fileName);
  }

  public File print(PurchaseOrder purchaseOrder, String formatPdf) throws AxelorException {
    ReportSettings reportSettings = prepareReportSettings(purchaseOrder, formatPdf);
    return reportSettings.generate().getFile();
  }

  public ReportSettings prepareReportSettings(PurchaseOrder purchaseOrder, String formatPdf)
      throws AxelorException {
    if (purchaseOrder.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(IExceptionMessage.PURCHASE_ORDER_MISSING_PRINTING_SETTINGS),
              purchaseOrder.getPurchaseOrderSeq()),
          purchaseOrder);
    }
    String locale = ReportSettings.getPrintingLocale(purchaseOrder.getSupplierPartner());
    String title = getFileName(purchaseOrder);
    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.PURCHASE_ORDER, title + " - ${date}");

    return reportSetting
        .addParam("PurchaseOrderId", purchaseOrder.getId())
        .addParam(
            "PurchaseOrderLineQuery", this.getPurchaseOrderLineDataSetQuery(purchaseOrder.getId()))
        .addParam("Locale", locale)
        .addParam("HeaderHeight", purchaseOrder.getPrintingSettings().getPdfHeaderHeight())
        .addParam("FooterHeight", purchaseOrder.getPrintingSettings().getPdfFooterHeight())
        .addFormat(formatPdf);
  }

  @Override // returns sql query for PurchaseOrderLineDataSet in PurchaseOrder.rptdesign
  public String getPurchaseOrderLineDataSetQuery(Long purchaseOrderId) {

    String selectClause = this.getPurchaseOrderLineQuerySelectClause();
    String fromClause = this.getPurchaseOrderLineQueryFromClause();
    String whereClause =
        "PurchaseOrder.id = " + purchaseOrderId.toString() + "order by PurchaseOrderLine.sequence ";

    if (!selectClause.contains("product_standard")) {
      selectClause = selectClause.concat(", CAST(null as varchar) as product_standard");
    }

    return String.format("select %s from %s where %s", selectClause, fromClause, whereClause);
  }

  protected String getPurchaseOrderLineQuerySelectClause() {
    return "	Product.code as product_code, "
        + "	Product.name as product_name, "
        + " PurchaseOrder.id as purchase_id, "
        + " PurchaseOrderLine.id as purchase_line_id, "
        + "	PurchaseOrderLine.product_code as supplier_product_code, "
        + "	PurchaseOrderLine.product_name as supplier_product_name, "
        + "	PurchaseOrderLine.description, "
        + "	PurchaseOrderLine.qty, "
        + "	PurchaseOrderLine.desired_deliv_date,"
        + "	PurchaseOrderLine.sequence, "
        + "	Unit.label_to_printing as \"UnitCode\", "
        + "	(CASE WHEN PurchaseOrder.in_ati "
        + "		THEN PurchaseOrderLine.in_tax_price"
        + "		ELSE PurchaseOrderLine.price END)"
        + "		as \"UnitPrice\", "
        + "	(PurchaseOrderLine.price_discounted "
        + "		- (CASE WHEN PurchaseOrder.in_ati "
        + "			THEN PurchaseOrderLine.in_tax_price"
        + "			ELSE PurchaseOrderLine.price END))"
        + "		* PurchaseOrderLine.qty "
        + "		as \"totalDiscountAmount\","
        + "	PurchaseOrderLine.ex_tax_total,"
        + "	PurchaseOrderLine.in_tax_total,"
        + "	PurchaseOrder.in_ati, "
        + "	PurchaseOrderLine.is_title_line as \"isTitleLine\","
        + "	TaxLine.value as \"TaxValue\" ";
  }

  protected String getPurchaseOrderLineQueryFromClause() {
    return "purchase_purchase_order_line as PurchaseOrderLine "
        + "inner join purchase_purchase_order as PurchaseOrder on (PurchaseOrderLine.purchase_order = PurchaseOrder.id) "
        + "left outer join base_product as Product on (PurchaseOrderLine.product = Product.id) "
        + "left outer join base_unit as Unit on (PurchaseOrderLine.unit = Unit.id) "
        + "left outer join account_tax_line as TaxLine on (PurchaseOrderLine.tax_line = TaxLine.id) ";
  }

  protected String getPurchaseOrderFilesName(boolean plural, String formatPdf) {
    return I18n.get(plural ? "Purchase orders" : "Purchase order")
        + " - "
        + Beans.get(AppBaseService.class).getTodayDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + formatPdf;
  }

  @Override
  public String getFileName(PurchaseOrder purchaseOrder) {
    return I18n.get("Purchase order")
        + " "
        + purchaseOrder.getPurchaseOrderSeq()
        + ((purchaseOrder.getVersionNumber() > 1) ? "-V" + purchaseOrder.getVersionNumber() : "");
  }
}
