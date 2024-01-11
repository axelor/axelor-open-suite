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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProdProductRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufOrderServiceBusinessImpl extends ManufOrderServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected OperationOrderServiceBusinessImpl operationOrderServiceBusinessImpl;

  @Inject
  public ManufOrderServiceBusinessImpl(
      SequenceService sequenceService,
      OperationOrderService operationOrderService,
      ManufOrderWorkflowService manufOrderWorkflowService,
      ProductVariantService productVariantService,
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ManufOrderRepository manufOrderRepo,
      ProdProductRepository prodProductRepo,
      OperationOrderServiceBusinessImpl operationOrderServiceBusinessImpl,
      ProductCompanyService productCompanyService,
      BarcodeGeneratorService barcodeGeneratorService,
      ProductStockLocationService productStockLocationService,
      UnitConversionService unitConversionService,
      MetaFiles metaFiles,
      PartnerRepository partnerRepository) {
    super(
        sequenceService,
        operationOrderService,
        manufOrderWorkflowService,
        productVariantService,
        appBaseService,
        appProductionService,
        manufOrderRepo,
        prodProductRepo,
        productCompanyService,
        barcodeGeneratorService,
        productStockLocationService,
        unitConversionService,
        metaFiles,
        partnerRepository);
    this.operationOrderServiceBusinessImpl = operationOrderServiceBusinessImpl;
  }

  @Transactional
  public void propagateIsToInvoice(ManufOrder manufOrder) {

    logger.debug(
        "{} is to invoice ? {}", manufOrder.getManufOrderSeq(), manufOrder.getIsToInvoice());

    boolean isToInvoice = manufOrder.getIsToInvoice();

    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {

        operationOrder.setIsToInvoice(isToInvoice);
      }
    }

    manufOrderRepo.save(manufOrder);
  }

  @Override
  public ManufOrder createManufOrder(
      Product product,
      BigDecimal qty,
      Unit unit,
      int priority,
      boolean isToInvoice,
      Company company,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException {

    ManufOrder manufOrder =
        super.createManufOrder(
            product,
            qty,
            unit,
            priority,
            isToInvoice,
            company,
            billOfMaterial,
            plannedStartDateT,
            plannedEndDateT);

    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      return manufOrder;
    }
    manufOrder.setIsToInvoice(isToInvoice);

    return manufOrder;
  }
}
