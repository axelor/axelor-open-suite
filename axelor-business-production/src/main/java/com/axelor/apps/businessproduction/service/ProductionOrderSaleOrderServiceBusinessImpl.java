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

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionOrderSaleOrderServiceBusinessImpl
    extends ProductionOrderSaleOrderServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ProductionOrderServiceBusinessImpl productionOrderServiceBusinessImpl;

  @Inject
  public ProductionOrderSaleOrderServiceBusinessImpl(
      UnitConversionService unitConversionService,
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo,
      ProductionOrderServiceBusinessImpl productionOrderServiceBusinessImpl,
      AppProductionService appProductionService) {
    super(unitConversionService, productionOrderService, productionOrderRepo, appProductionService);

    this.productionOrderServiceBusinessImpl = productionOrderServiceBusinessImpl;
  }

  @Override
  protected ProductionOrder createProductionOrder(SaleOrder saleOrder) throws AxelorException {

    ProductionOrder productionOrder = super.createProductionOrder(saleOrder);
    productionOrder.setProject(saleOrder.getProject());
    return productionOrder;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void createSaleOrder(ProductionOrder productionOrder) throws AxelorException {

    logger.debug(
        "Création d'un devis client pour l'ordre de production : {}",
        new Object[] {productionOrder.getProductionOrderSeq()});

    Project project = productionOrder.getProject();

    project.getClientPartner();

    //		if(businessFolder.getCompany() != null)  {
    //
    //			SaleOrder saleOrder = saleOrderServiceStockImpl.createSaleOrder(
    //					businessFolder,
    //					user,
    //					businessFolder.getCompany(),
    //					null,
    //					partner.getCurrency(),
    //					null,
    //					null,
    //					null,
    //					saleOrderServiceStockImpl.getLocation(businessProject.getCompany()),
    //					today,
    //					Beans.get(PriceListRepository.class).all().filter("self.partner = ?1 AND self.typeSelect
    // = 1", partner).fetchOne(),
    //					partner);
    //
    //			Beans.get(SaleOrderRepository.class).save(saleOrder);
    //
    //		}
    //
    //		//TODO
    //
    //		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
    //
    //
    //	purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLineService.createPurchaseOrderLine(purchaseOrder, saleOrderLine));
    //
    //		}
    //
    //		purchaseOrderService.computePurchaseOrder(purchaseOrder);
    //
    //		purchaseOrder.save();
  }
}
