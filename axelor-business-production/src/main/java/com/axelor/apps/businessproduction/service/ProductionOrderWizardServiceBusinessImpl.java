/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.production.service.productionorder.ProductionOrderWizardServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionOrderWizardServiceBusinessImpl extends ProductionOrderWizardServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ProductionOrderServiceBusinessImpl productionOrderServiceBusinessImpl;

  @Inject
  public ProductionOrderWizardServiceBusinessImpl(
      ProductionOrderService productionOrderService,
      BillOfMaterialRepository billOfMaterialRepo,
      ProductRepository productRepo,
      AppProductionService appProductionService,
      ProductionOrderServiceBusinessImpl productionOrderServiceBusinessImpl) {
    super(productionOrderService, billOfMaterialRepo, productRepo, appProductionService);
    this.productionOrderServiceBusinessImpl = productionOrderServiceBusinessImpl;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Long validate(Context context) throws AxelorException {

    Map<String, Object> bomContext = (Map<String, Object>) context.get("billOfMaterial");
    BillOfMaterial billOfMaterial =
        billOfMaterialRepo.find(((Integer) bomContext.get("id")).longValue());

    BigDecimal qty = new BigDecimal((String) context.get("qty"));

    Product product = null;

    if (context.get("product") != null) {
      Map<String, Object> productContext = (Map<String, Object>) context.get("product");
      product = productRepo.find(((Integer) productContext.get("id")).longValue());
    } else {
      product = billOfMaterial.getProduct();
    }

    ZonedDateTime startDateT, endDateT = null;
    if (context.containsKey("_startDate") && context.get("_startDate") != null) {
      startDateT =
          ZonedDateTime.parse(
              context.get("_startDate").toString(),
              DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
    } else {
      startDateT = appProductionService.getTodayDateTime();
    }

    if (context.containsKey("_endDate") && context.get("_endDate") != null) {
      endDateT =
          ZonedDateTime.parse(
              context.get("_endDate").toString(),
              DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
    }

    Project project = null;
    if (context.get("business_id") != null) {
      project =
          Beans.get(ProjectRepository.class)
              .find(((Integer) context.get("business_id")).longValue());
    }

    ProductionOrder productionOrder =
        productionOrderServiceBusinessImpl.generateProductionOrder(
            product,
            billOfMaterial,
            qty,
            project,
            startDateT.toLocalDateTime(),
            endDateT != null ? endDateT.toLocalDateTime() : null,
            null);

    if (productionOrder != null) {
      return productionOrder.getId();
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCTION_ORDER_2));
    }
  }
}
