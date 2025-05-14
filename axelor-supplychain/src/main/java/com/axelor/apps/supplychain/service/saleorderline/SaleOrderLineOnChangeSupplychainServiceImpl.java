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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnChangeSupplychainServiceImpl extends SaleOrderLineOnChangeServiceImpl {

  protected AnalyticLineModelService analyticLineModelService;
  protected AppAccountService appAccountService;
  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;
  protected AppSupplychainService appSupplychainService;

  @Inject
  public SaleOrderLineOnChangeSupplychainServiceImpl(
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      AnalyticLineModelService analyticLineModelService,
      AppAccountService appAccountService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService) {
    super(
        saleOrderLineDiscountService,
        saleOrderLineComputeService,
        saleOrderLineTaxService,
        saleOrderLinePriceService,
        saleOrderLineComplementaryProductService);
    this.analyticLineModelService = analyticLineModelService;
    this.appAccountService = appAccountService;
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public Map<String, Object> qtyOnChange(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, SaleOrderLine parentSol)
      throws AxelorException {
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    Map<String, Object> saleOrderLineMap = super.qtyOnChange(saleOrderLine, saleOrder, parentSol);
    if (appSupplychain.getManageStockReservation()) {
      saleOrderLineMap.putAll(
          saleOrderLineServiceSupplyChain.updateRequestedReservedQty(saleOrderLine));
    }
    saleOrderLineMap.putAll(checkInvoicedOrDeliveredOrderQty(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> compute(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = super.compute(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(computeAnalyticDistribution(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  protected Map<String, Object> computeAnalyticDistribution(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (!appAccountService.getAppAccount().getManageAnalyticAccounting()) {
      return saleOrderLineMap;
    }

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    if (analyticLineModelService.productAccountManageAnalytic(analyticLineModel)) {

      analyticLineModelService.computeAnalyticDistribution(analyticLineModel);

      saleOrderLineMap.put(
          "analyticDistributionTemplate", analyticLineModel.getAnalyticDistributionTemplate());
      saleOrderLineMap.put("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
    }
    return saleOrderLineMap;
  }

  protected Map<String, Object> checkInvoicedOrDeliveredOrderQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLine.setQty(
        saleOrderLineServiceSupplyChain.checkInvoicedOrDeliveredOrderQty(saleOrderLine, saleOrder));
    saleOrderLineMap.put("qty", saleOrderLine.getQty());
    saleOrderLineServiceSupplyChain.updateDeliveryState(saleOrderLine);
    saleOrderLineMap.put("deliveryState", saleOrderLine.getDeliveryState());

    return saleOrderLineMap;
  }
}
