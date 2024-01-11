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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineOriginRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class MrpLineServiceProductionImpl extends MrpLineServiceImpl {

  protected ManufOrderService manufOrderService;
  protected ManufOrderRepository manufOrderRepository;
  protected OperationOrderRepository operationOrderRepository;

  @Inject
  public MrpLineServiceProductionImpl(
      AppBaseService appBaseService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      StockRulesService stockRulesService,
      SaleOrderLineRepository saleOrderLineRepo,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      MrpForecastRepository mrpForecastRepo,
      ManufOrderService manufOrderService,
      ManufOrderRepository manufOrderRepository,
      OperationOrderRepository operationOrderRepository,
      MrpLineRepository mrpLineRepo) {
    super(
        appBaseService,
        purchaseOrderSupplychainService,
        purchaseOrderService,
        purchaseOrderLineService,
        purchaseOrderRepo,
        stockRulesService,
        saleOrderLineRepo,
        purchaseOrderLineRepo,
        mrpForecastRepo,
        mrpLineRepo);
    this.manufOrderService = manufOrderService;
    this.manufOrderRepository = manufOrderRepository;
    this.operationOrderRepository = operationOrderRepository;
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
            == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        && Beans.get(AppProductionService.class).isApp("production")) {

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
            ManufOrderOriginTypeProduction
                .ORIGIN_TYPE_MRP); // TODO compute the time to produce to put the manuf order at the
    // correct day

    linkToOrder(mrpLine, manufOrder);
  }

  @Override
  protected String getMrpLineOriginStr(MrpLineOrigin mrpLineOrigin) {
    if (mrpLineOrigin
        .getRelatedToSelect()
        .equals(MrpLineOriginRepository.RELATED_TO_MANUFACTURING_ORDER)) {
      ManufOrder manufOrder = manufOrderRepository.find(mrpLineOrigin.getRelatedToSelectId());
      return manufOrder.getManufOrderSeq();
    }
    if (mrpLineOrigin
        .getRelatedToSelect()
        .equals(MrpLineOriginRepository.RELATED_TO_OPERATION_ORDER)) {
      OperationOrder operationOrder =
          operationOrderRepository.find(mrpLineOrigin.getRelatedToSelectId());
      return operationOrder.getName();
    }
    return super.getMrpLineOriginStr(mrpLineOrigin);
  }

  @Override
  protected String computeRelatedName(Model model) {

    if (!Beans.get(AppProductionService.class).isApp("production")) {

      return super.computeRelatedName(model);
    }

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

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      return super.getPartner(model);
    }

    if (model instanceof ManufOrder) {

      return ((ManufOrder) model).getClientPartner();

    } else if (model instanceof OperationOrder) {

      return ((OperationOrder) model).getManufOrder().getClientPartner();

    } else {

      return super.getPartner(model);
    }
  }
}
