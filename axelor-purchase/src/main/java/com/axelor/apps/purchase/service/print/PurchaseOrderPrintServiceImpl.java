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
package com.axelor.apps.purchase.service.print;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.report.IReport;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBase;
import com.axelor.utils.ModelTool;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.file.PdfTool;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderPrintServiceImpl implements PurchaseOrderPrintService {

  protected AppPurchaseService appPurchaseService;
  protected AppBaseService appBaseService;

  @Inject
  public PurchaseOrderPrintServiceImpl(
      AppPurchaseService appPurchaseService, AppBaseService appBaseService) {
    this.appPurchaseService = appPurchaseService;
    this.appBaseService = appBaseService;
  }

  @Override
  public String printPurchaseOrder(PurchaseOrder purchaseOrder, String formatPdf)
      throws AxelorException {
    String fileName = getFileName(purchaseOrder) + "." + formatPdf;
    return PdfTool.getFileLinkFromPdfFile(print(purchaseOrder, formatPdf), fileName);
  }

  @Override
  public String printPurchaseOrders(List<Long> ids) throws IOException {
    List<File> printedPurchaseOrders = new ArrayList<>();
    ModelTool.apply(
        PurchaseOrder.class,
        ids,
        new ThrowConsumer<PurchaseOrder, Exception>() {

          @Override
          public void accept(PurchaseOrder purchaseOrder) throws Exception {
            printedPurchaseOrders.add(print(purchaseOrder, ReportSettings.FORMAT_PDF));
          }
        });

    Integer status = Beans.get(PurchaseOrderRepository.class).find(ids.get(0)).getStatusSelect();
    String fileName = getPurchaseOrderFilesName(status);
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
              I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MISSING_PRINTING_SETTINGS),
              purchaseOrder.getPurchaseOrderSeq()),
          purchaseOrder);
    }
    String locale = ReportSettings.getPrintingLocale(purchaseOrder.getSupplierPartner());
    String title = getFileName(purchaseOrder);
    AppBase appBase = appBaseService.getAppBase();
    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.PURCHASE_ORDER, title + " - ${date}");

    return reportSetting
        .addParam("PurchaseOrderId", purchaseOrder.getId())
        .addParam(
            "Timezone",
            purchaseOrder.getCompany() != null ? purchaseOrder.getCompany().getTimezone() : null)
        .addParam("Locale", locale)
        .addParam(
            "GroupProducts",
            appBase.getIsRegroupProductsOnPrintings()
                && purchaseOrder.getGroupProductsOnPrintings())
        .addParam("GroupProductTypes", appBase.getRegroupProductsTypeSelect())
        .addParam("GroupProductLevel", appBase.getRegroupProductsLevelSelect())
        .addParam("GroupProductProductTitle", appBase.getRegroupProductsLabelProducts())
        .addParam("GroupProductServiceTitle", appBase.getRegroupProductsLabelServices())
        .addParam("HeaderHeight", purchaseOrder.getPrintingSettings().getPdfHeaderHeight())
        .addParam("FooterHeight", purchaseOrder.getPrintingSettings().getPdfFooterHeight())
        .addParam(
            "AddressPositionSelect", purchaseOrder.getPrintingSettings().getAddressPositionSelect())
        .addFormat(formatPdf);
  }

  protected String getPurchaseOrderFilesName(Integer status) {
    String prefixFileName = I18n.get("Purchase orders");
    if (status == PurchaseOrderRepository.STATUS_DRAFT
        || status == PurchaseOrderRepository.STATUS_REQUESTED) {
      prefixFileName = I18n.get("Purchase quotations");
    }
    return prefixFileName
        + " - "
        + appBaseService
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + ReportSettings.FORMAT_PDF;
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
        + ((appPurchaseService.getAppPurchase().getManagePurchaseOrderVersion()
                && purchaseOrder.getVersionNumber() > 1)
            ? "-V" + purchaseOrder.getVersionNumber()
            : "");
  }
}
