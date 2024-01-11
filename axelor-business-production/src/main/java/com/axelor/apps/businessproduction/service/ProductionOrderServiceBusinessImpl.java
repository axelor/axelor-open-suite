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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.production.service.productionorder.ProductionOrderServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductionOrderServiceBusinessImpl extends ProductionOrderServiceImpl {

  @Inject
  public ProductionOrderServiceBusinessImpl(
      ManufOrderService manufOrderService,
      SequenceService sequenceService,
      ProductionOrderRepository productionOrderRepo) {
    super(manufOrderService, sequenceService, productionOrderRepo);
  }

  @Transactional(rollbackOn = {Exception.class})
  public ProductionOrder generateProductionOrder(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      Project project,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine)
      throws AxelorException {

    ProductionOrder productionOrder = this.createProductionOrder(saleOrder);
    productionOrder.setProject(project);

    this.addManufOrder(
        productionOrder,
        product,
        billOfMaterial,
        qtyRequested,
        startDate,
        endDate,
        saleOrder,
        saleOrderLine,
        ManufOrderOriginTypeProduction.ORIGIN_TYPE_OTHER);

    return productionOrderRepo.save(productionOrder);
  }
}
