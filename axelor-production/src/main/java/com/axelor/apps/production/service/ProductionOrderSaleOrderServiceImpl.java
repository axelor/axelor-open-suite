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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionOrderSaleOrderServiceImpl implements ProductionOrderSaleOrderService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected UnitConversionService unitConversionService;
  protected ProductionOrderService productionOrderService;
  protected ProductionOrderRepository productionOrderRepo;

  @Inject
  public ProductionOrderSaleOrderServiceImpl(
      UserService userInfoService,
      UnitConversionService unitConversionService,
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo) {

    this.unitConversionService = unitConversionService;
    this.productionOrderService = productionOrderService;
    this.productionOrderRepo = productionOrderRepo;
  }

  @Override
  public List<Long> generateProductionOrder(SaleOrder saleOrder) throws AxelorException {

    List<Long> productionOrderIdList = new ArrayList<>();
    if (saleOrder.getSaleOrderLineList() != null) {

      ProductionOrder productionOrder = null;
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

        productionOrder = this.generateProductionOrder(saleOrderLine);
        if (productionOrder != null) {
          productionOrderIdList.add(productionOrder.getId());
        }
      }
    }

    return productionOrderIdList;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public ProductionOrder generateProductionOrder(SaleOrderLine saleOrderLine)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();

    if (saleOrderLine.getSaleSupplySelect() == ProductRepository.SALE_SUPPLY_PRODUCE
        && saleOrderLine.getBillOfMaterial() != null
        && saleOrderLine.getBillOfMaterial().getProdProcess() != null
        && product != null
        && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {
      Unit unit = saleOrderLine.getProduct().getUnit();
      BigDecimal qty = saleOrderLine.getQty();
      if (!unit.equals(saleOrderLine.getUnit())) {
        qty =
            unitConversionService.convert(
                saleOrderLine.getUnit(), unit, qty, qty.scale(), saleOrderLine.getProduct());
      }
      return productionOrderRepo.save(
          productionOrderService.generateProductionOrder(
              product, saleOrderLine.getBillOfMaterial(), qty, LocalDateTime.now()));
    }

    return null;
  }
}
