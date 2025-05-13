/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.TempBomTreeRepository;
import com.axelor.apps.production.service.BillOfMaterialLineService;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class BillOfMaterialServiceMaintenanceImpl extends BillOfMaterialServiceImpl
    implements BillOfMaterialMaintenanceService {

  protected ProductionConfigService productionConfigService;
  protected PrintingTemplatePrintService printingTemplatePrintService;

  @Inject
  public BillOfMaterialServiceMaintenanceImpl(
      BillOfMaterialRepository billOfMaterialRepo,
      TempBomTreeRepository tempBomTreeRepo,
      ProductRepository productRepo,
      ProductCompanyService productCompanyService,
      BillOfMaterialLineService billOfMaterialLineService,
      BillOfMaterialService billOfMaterialService,
      CostSheetService costSheetService,
      ProductionConfigService productionConfigService,
      PrintingTemplatePrintService printingTemplatePrintService) {
    super(
        billOfMaterialRepo,
        tempBomTreeRepo,
        productRepo,
        productCompanyService,
        billOfMaterialLineService,
        billOfMaterialService,
        costSheetService);
    this.productionConfigService = productionConfigService;
    this.printingTemplatePrintService = printingTemplatePrintService;
  }

  @Override
  public String getReportLink(BillOfMaterial billOfMaterial, String name) throws AxelorException {
    billOfMaterial = billOfMaterialRepo.find(billOfMaterial.getId());
    Company company = billOfMaterial.getCompany();
    PrintingTemplate maintenanceBOMPrintTemplate = null;
    if (ObjectUtils.notEmpty(company)) {
      maintenanceBOMPrintTemplate =
          productionConfigService
              .getProductionConfig(company)
              .getMaintenanceBillOfMaterialPrintTemplate();
    }
    if (maintenanceBOMPrintTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }
    return printingTemplatePrintService.getPrintLink(
        maintenanceBOMPrintTemplate, new PrintingGenFactoryContext(billOfMaterial));
  }

  @Override
  public String getFileName(BillOfMaterial billOfMaterial) {

    return I18n.get("Bill of Materials")
        + "-"
        + billOfMaterial.getName()
        + ((billOfMaterial.getVersionNumber() > 1) ? "-V" + billOfMaterial.getVersionNumber() : "");
  }
}
