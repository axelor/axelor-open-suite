/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
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

public class ManufOrderPrintServiceImpl implements ManufOrderPrintService {

  protected AppBaseService appBaseService;
  protected ProductionConfigService productionConfigService;
  protected BirtTemplateService birtTemplateService;
  protected ManufOrderRepository manufOrderRepository;

  @Inject
  public ManufOrderPrintServiceImpl(
      AppBaseService appBaseService,
      ProductionConfigService productionConfigService,
      BirtTemplateService birtTemplateService,
      ManufOrderRepository manufOrderRepository) {
    this.appBaseService = appBaseService;
    this.productionConfigService = productionConfigService;
    this.birtTemplateService = birtTemplateService;
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public String printManufOrders(List<Long> ids) throws IOException, AxelorException {
    List<File> printedManufOrders = new ArrayList<>();
    int errorCount =
        ModelTool.apply(
            ManufOrder.class,
            ids,
            new ThrowConsumer<ManufOrder, Exception>() {

              @Override
              public void accept(ManufOrder manufOrder) throws Exception {
                try {
                  printedManufOrders.add(print(manufOrder));
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
    String fileName = getManufOrdersFilename();
    return PdfTool.mergePdfToFileLink(printedManufOrders, fileName);
  }

  @Override
  public String printManufOrder(ManufOrder manufOrder) throws AxelorException {
    String fileName = getFileName(manufOrder);
    return PdfTool.getFileLinkFromPdfFile(print(manufOrder), fileName);
  }

  protected File print(ManufOrder manufOrder) throws AxelorException {
    ReportSettings reportSettings = prepareReportSettings(manufOrder);
    return reportSettings.generate().getFile();
  }

  @Override
  public ReportSettings prepareReportSettings(ManufOrder manufOrder) throws AxelorException {
    manufOrder = manufOrderRepository.find(manufOrder.getId());
    String title = getFileName(manufOrder);
    Company company = manufOrder.getCompany();
    BirtTemplate maintenanceManufOrderBirtTemplate = null;
    if (ObjectUtils.notEmpty(company)) {
      maintenanceManufOrderBirtTemplate =
          productionConfigService
              .getProductionConfig(company)
              .getMaintenanceManufOrderBirtTemplate();
    }
    if (maintenanceManufOrderBirtTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }
    return birtTemplateService.generate(
        maintenanceManufOrderBirtTemplate,
        manufOrder,
        null,
        title,
        maintenanceManufOrderBirtTemplate.getAttach(),
        ReportSettings.FORMAT_PDF);
  }

  @Override
  public String getManufOrdersFilename() {
    return I18n.get("Manufacturing orders")
        + " - "
        + appBaseService
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + ReportSettings.FORMAT_PDF;
  }

  @Override
  public String getFileName(ManufOrder manufOrder) {
    return I18n.get("Manufacturing order")
        + " "
        + manufOrder.getManufOrderSeq()
        + "."
        + ReportSettings.FORMAT_PDF;
  }
}
