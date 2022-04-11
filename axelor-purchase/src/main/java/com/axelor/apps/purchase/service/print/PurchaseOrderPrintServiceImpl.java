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
package com.axelor.apps.purchase.service.print;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
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
    String fileName = getPurchaseOrderFilesName(purchaseOrder.getStatusSelect(), false, formatPdf);
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

    Integer status = Beans.get(PurchaseOrderRepository.class).find(ids.get(0)).getStatusSelect();
    String fileName = getPurchaseOrderFilesName(status, true, ReportSettings.FORMAT_PDF);
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
        .addParam("Locale", locale)
        .addParam("HeaderHeight", purchaseOrder.getPrintingSettings().getPdfHeaderHeight())
        .addParam("FooterHeight", purchaseOrder.getPrintingSettings().getPdfFooterHeight())
        .addFormat(formatPdf);
  }

  protected String getPurchaseOrderFilesName(Integer status, boolean plural, String formatPdf) {
    String prefixFileName = I18n.get(plural ? "Purchase orders" : "Purchase order");
    if (status == PurchaseOrderRepository.STATUS_DRAFT
        || status == PurchaseOrderRepository.STATUS_REQUESTED) {
      prefixFileName = I18n.get(plural ? "Purchase quotations" : "Purchase quotation");
    }
    return prefixFileName
        + " - "
        + Beans.get(AppBaseService.class).getTodayDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + formatPdf;
  }

  @Override
  public String getFileName(PurchaseOrder purchaseOrder) {
    String prefixFileName = I18n.get("Purchase order");
    if (purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_DRAFT
        || purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_REQUESTED) {
      prefixFileName = I18n.get("Purchase quotation");
    }
    return prefixFileName
        + " "
        + purchaseOrder.getPurchaseOrderSeq()
        + ((purchaseOrder.getVersionNumber() > 1) ? "-V" + purchaseOrder.getVersionNumber() : "");
  }
}
