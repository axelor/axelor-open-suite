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
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.TempBomTreeRepository;
import com.axelor.apps.production.service.BillOfMaterialLineService;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class BillOfMaterialServiceMaintenanceImpl extends BillOfMaterialServiceImpl
    implements BillOfMaterialMaintenanceService {

  protected ProductionConfigService productionConfigService;
  protected BirtTemplateService birtTemplateService;

  @Inject
  public BillOfMaterialServiceMaintenanceImpl(
      BillOfMaterialRepository billOfMaterialRepo,
      TempBomTreeRepository tempBomTreeRepo,
      ProductRepository productRepo,
      ProductCompanyService productCompanyService,
      BillOfMaterialLineService billOfMaterialLineService,
      BillOfMaterialService billOfMaterialService,
      ProductionConfigService productionConfigService,
      BirtTemplateService birtTemplateService) {
    super(
        billOfMaterialRepo,
        tempBomTreeRepo,
        productRepo,
        productCompanyService,
        billOfMaterialLineService,
        billOfMaterialService);
    this.productionConfigService = productionConfigService;
    this.birtTemplateService = birtTemplateService;
  }

  @Override
  public String computeName(BillOfMaterial bom) {

    return bom.getProduct() != null
        ? super.computeName(bom)
        : bom.getMachineType().getName() + "-" + bom.getId();
  }

  @Override
  public String getReportLink(BillOfMaterial billOfMaterial, String name) throws AxelorException {
    billOfMaterial = billOfMaterialRepo.find(billOfMaterial.getId());
    Company company = billOfMaterial.getCompany();
    BirtTemplate maintenanceBOMBirtTemplate = null;
    if (ObjectUtils.notEmpty(company)) {
      maintenanceBOMBirtTemplate =
          productionConfigService
              .getProductionConfig(company)
              .getMaintenanceBillOfMaterialBirtTemplate();
    }
    if (maintenanceBOMBirtTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }
    return birtTemplateService.generateBirtTemplateLink(
        maintenanceBOMBirtTemplate, billOfMaterial, getFileName(billOfMaterial) + "-${date}");
  }

  @Override
  public String getFileName(BillOfMaterial billOfMaterial) {

    return I18n.get("Bill of Materials")
        + "-"
        + billOfMaterial.getName()
        + ((billOfMaterial.getVersionNumber() > 1) ? "-V" + billOfMaterial.getVersionNumber() : "");
  }
}
