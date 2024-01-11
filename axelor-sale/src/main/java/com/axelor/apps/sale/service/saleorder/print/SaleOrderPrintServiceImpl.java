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
package com.axelor.apps.sale.service.saleorder.print;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.report.IReport;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
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

public class SaleOrderPrintServiceImpl implements SaleOrderPrintService {

  protected SaleOrderService saleOrderService;
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderPrintServiceImpl(
      SaleOrderService saleOrderService, AppSaleService appSaleService) {
    this.saleOrderService = saleOrderService;
    this.appSaleService = appSaleService;
  }

  @Override
  public String printSaleOrder(SaleOrder saleOrder, boolean proforma, String format)
      throws AxelorException, IOException {
    String fileName = saleOrderService.getFileName(saleOrder) + "." + format;

    return PdfTool.getFileLinkFromPdfFile(print(saleOrder, proforma, format), fileName);
  }

  @Override
  public String printSaleOrders(List<Long> ids) throws IOException {
    List<File> printedSaleOrders = new ArrayList<>();
    ModelTool.apply(
        SaleOrder.class,
        ids,
        new ThrowConsumer<SaleOrder, Exception>() {
          @Override
          public void accept(SaleOrder saleOrder) throws Exception {
            printedSaleOrders.add(print(saleOrder, false, ReportSettings.FORMAT_PDF));
          }
        });
    Integer status = Beans.get(SaleOrderRepository.class).find(ids.get(0)).getStatusSelect();
    String fileName = getSaleOrderFilesName(status);
    return PdfTool.mergePdfToFileLink(printedSaleOrders, fileName);
  }

  public File print(SaleOrder saleOrder, boolean proforma, String format) throws AxelorException {
    ReportSettings reportSettings = prepareReportSettings(saleOrder, proforma, format);
    return reportSettings.generate().getFile();
  }

  @Override
  public ReportSettings prepareReportSettings(SaleOrder saleOrder, boolean proforma, String format)
      throws AxelorException {

    if (saleOrder.getPrintingSettings() == null) {
      if (saleOrder.getCompany().getPrintingSettings() != null) {
        saleOrder.setPrintingSettings(saleOrder.getCompany().getPrintingSettings());
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            String.format(
                I18n.get(SaleExceptionMessage.SALE_ORDER_MISSING_PRINTING_SETTINGS),
                saleOrder.getSaleOrderSeq()),
            saleOrder);
      }
    }
    String locale = ReportSettings.getPrintingLocale(saleOrder.getClientPartner());

    String title = saleOrderService.getFileName(saleOrder);

    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.SALES_ORDER, title + " - ${date}");

    return reportSetting
        .addParam("SaleOrderId", saleOrder.getId())
        .addParam(
            "Timezone",
            saleOrder.getCompany() != null ? saleOrder.getCompany().getTimezone() : null)
        .addParam("Locale", locale)
        .addParam("ProformaInvoice", proforma)
        .addParam("HeaderHeight", saleOrder.getPrintingSettings().getPdfHeaderHeight())
        .addParam("FooterHeight", saleOrder.getPrintingSettings().getPdfFooterHeight())
        .addParam(
            "AddressPositionSelect", saleOrder.getPrintingSettings().getAddressPositionSelect())
        .addFormat(format);
  }

  /** Return the name for the printed sale orders. */
  protected String getSaleOrderFilesName(Integer status) {
    String prefixFileName = I18n.get("Sale orders");
    if (status == SaleOrderRepository.STATUS_DRAFT_QUOTATION
        || status == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      prefixFileName = I18n.get("Sale quotations");
    }

    return prefixFileName
        + " - "
        + appSaleService
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + ReportSettings.FORMAT_PDF;
  }
}
