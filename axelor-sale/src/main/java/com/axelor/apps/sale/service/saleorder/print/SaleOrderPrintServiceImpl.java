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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateHelper;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SaleOrderPrintServiceImpl implements SaleOrderPrintService {

  protected SaleOrderService saleOrderService;
  protected AppSaleService appSaleService;
  protected PrintingTemplatePrintService printingTemplatePrintService;
  protected SaleOrderRepository saleOrderRepository;
  protected SaleConfigService saleConfigService;

  @Inject
  public SaleOrderPrintServiceImpl(
      SaleOrderService saleOrderService,
      AppSaleService appSaleService,
      PrintingTemplatePrintService printingTemplatePrintService,
      SaleOrderRepository saleOrderRepository,
      SaleConfigService saleConfigService) {
    this.saleOrderService = saleOrderService;
    this.appSaleService = appSaleService;
    this.printingTemplatePrintService = printingTemplatePrintService;
    this.saleOrderRepository = saleOrderRepository;
    this.saleConfigService = saleConfigService;
  }

  @Override
  public String printSaleOrder(
      SaleOrder saleOrder, boolean proforma, PrintingTemplate saleOrderPrintTemplate)
      throws AxelorException, IOException {
    return PrintingTemplateHelper.getFileLink(print(saleOrder, proforma, saleOrderPrintTemplate));
  }

  @Override
  public String printSaleOrders(List<Long> ids) throws IOException, AxelorException {
    List<File> printedSaleOrders = new ArrayList<>();
    int errorCount =
        ModelHelper.apply(
            SaleOrder.class,
            ids,
            new ThrowConsumer<SaleOrder, Exception>() {
              @Override
              public void accept(SaleOrder saleOrder) throws Exception {
                try {
                  printedSaleOrders.add(
                      print(
                          saleOrder,
                          false,
                          saleConfigService.getSaleOrderPrintTemplate(saleOrder.getCompany())));
                } catch (Exception e) {
                  TraceBackService.trace(e);
                  throw e;
                }
              }
            });
    if (errorCount > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }
    Integer status = saleOrderRepository.find(ids.get(0)).getStatusSelect();
    String fileName = getSaleOrderFilesName(status);
    return PrintingTemplateHelper.mergeToFileLink(printedSaleOrders, fileName);
  }

  @Override
  public File print(SaleOrder saleOrder, boolean proforma, PrintingTemplate saleOrderPrintTemplate)
      throws AxelorException {
    return print(saleOrder, proforma, saleOrderPrintTemplate, saleOrderPrintTemplate.getToAttach());
  }

  @Override
  public File print(
      SaleOrder saleOrder,
      boolean proforma,
      PrintingTemplate saleOrderPrintTemplate,
      boolean toAttach)
      throws AxelorException {
    saleOrderService.checkPrintingSettings(saleOrder);
    String title = saleOrderService.getFileName(saleOrder);

    PrintingGenFactoryContext factoryContext =
        new PrintingGenFactoryContext(EntityHelper.getEntity(saleOrder));
    factoryContext.setContext(Map.of("ProformaInvoice", proforma));

    return printingTemplatePrintService.getPrintFile(
        saleOrderPrintTemplate, factoryContext, title + " - ${date}", toAttach);
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
            .format(DateTimeFormatter.BASIC_ISO_DATE);
  }
}
