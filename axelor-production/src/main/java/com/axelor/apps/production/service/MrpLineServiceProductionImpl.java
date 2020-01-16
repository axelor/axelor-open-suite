/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class MrpLineServiceProductionImpl extends MrpLineServiceImpl {

  protected ManufOrderService manufOrderService;

  @Inject
  public MrpLineServiceProductionImpl(
      AppProductionService appProductionService,
      PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      ManufOrderService manufOrderService,
      ProductionOrderRepository productionOrderRepo,
      StockRulesService stockRulesService) {

    super(
        appProductionService,
        purchaseOrderServiceSupplychainImpl,
        purchaseOrderLineService,
        purchaseOrderRepo,
        stockRulesService);
    this.manufOrderService = manufOrderService;
  }

  @Override
  public void generateProposal(
      MrpLine mrpLine,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      boolean isProposalsPerSupplier)
      throws AxelorException {

    super.generateProposal(
        mrpLine, purchaseOrders, purchaseOrdersPerSupplier, isProposalsPerSupplier);

    if (mrpLine.getMrpLineType().getElementSelect()
        == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL) {

      this.generateManufacturingProposal(mrpLine);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generateManufacturingProposal(MrpLine mrpLine) throws AxelorException {

    Product product = mrpLine.getProduct();

    ManufOrder manufOrder =
        manufOrderService.generateManufOrder(
            product,
            mrpLine.getQty(),
            ManufOrderService.DEFAULT_PRIORITY,
            ManufOrderService.IS_TO_INVOICE,
            null,
            mrpLine.getMaturityDate().atStartOfDay(),
            null,
            ManufOrderServiceImpl
                .ORIGIN_TYPE_MRP); // TODO compute the time to produce to put the manuf order at the
    // correct day

    linkToOrder(mrpLine, manufOrder);
  }

  @Override
  protected String computeRelatedName(Model model) {

    if (model instanceof ManufOrder) {

      return ((ManufOrder) model).getManufOrderSeq();

    } else if (model instanceof OperationOrder) {

      return ((OperationOrder) model).getName();

    } else {

      return super.computeRelatedName(model);
    }
  }

  @Override
  protected Partner getPartner(Model model) {

    if (model instanceof ManufOrder) {

      return ((ManufOrder) model).getClientPartner();

    } else if (model instanceof OperationOrder) {

      return ((OperationOrder) model).getManufOrder().getClientPartner();

    } else {

      return super.getPartner(model);
    }
  }
}
