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
package com.axelor.apps.production.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineOnChangeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineOnChangeProductionServiceImpl
    extends SaleOrderLineOnChangeSupplychainServiceImpl {

  protected final SaleOrderLineProductionService saleOrderLineProductionService;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;

  @Inject
  public SaleOrderLineOnChangeProductionServiceImpl(
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      AnalyticLineModelService analyticLineModelService,
      AppAccountService appAccountService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineProductionService saleOrderLineProductionService,
      SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService) {
    super(
        saleOrderLineDiscountService,
        saleOrderLineComputeService,
        saleOrderLineTaxService,
        saleOrderLinePriceService,
        saleOrderLineComplementaryProductService,
        analyticLineModelService,
        appAccountService,
        saleOrderLineServiceSupplyChain,
        appSupplychainService);
    this.saleOrderLineProductionService = saleOrderLineProductionService;
    this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
  }

  @Override
  public Map<String, Object> qtyOnChange(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, SaleOrderLine parentSol)
      throws AxelorException {

    Map<String, Object> saleOrderLineMap = super.qtyOnChange(saleOrderLine, saleOrder, parentSol);

    saleOrderLineMap.putAll(updateProduceQty(saleOrderLine, saleOrder, parentSol));

    return saleOrderLineMap;
  }

  protected Map<String, Object> updateProduceQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, SaleOrderLine parentSol)
      throws AxelorException {
    saleOrderLine.setQtyToProduce(
        saleOrderLineProductionService.computeQtyToProduce(saleOrderLine, parentSol));

    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetail : saleOrderLineDetailsList) {
        saleOrderLineDetailsPriceService.computePrices(
            saleOrderLineDetail, saleOrder, saleOrderLine);
      }
    }

    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
      for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
        updateProduceQty(subSaleOrderLine, saleOrder, saleOrderLine);
      }
    }

    Map<String, Object> values = new HashMap<>();
    values.put("qtyToProduce", saleOrderLine.getQtyToProduce());
    values.put("subSaleOrderLineList", getSubSaleOrderLinesMap(saleOrderLine));
    values.put("saleOrderLineDetailsList", saleOrderLine.getSaleOrderLineDetailsList());

    return values;
  }

  // This is a fix to update N+2 level when the sale order is persisted on the qty on change.
  protected List<Map<String, Object>> getSubSaleOrderLinesMap(SaleOrderLine saleOrderLine) {
    List<Map<String, Object>> subSaleOrderLineMapList = new ArrayList<>();
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isEmpty(subSaleOrderLineList)) {
      return subSaleOrderLineMapList;
    }
    for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
      Map<String, Object> subSaleOrderLineMap = new HashMap<>(Mapper.toMap(subSaleOrderLine));
      subSaleOrderLineMap.put("subSaleOrderLineList", getSubSaleOrderLinesMap(subSaleOrderLine));
      subSaleOrderLineMap.put(
          "saleOrderLineDetailsList", getSaleOrderLineDetailsMapList(subSaleOrderLine));
      subSaleOrderLineMapList.add(subSaleOrderLineMap);
    }
    return subSaleOrderLineMapList;
  }

  protected List<Map<String, Object>> getSaleOrderLineDetailsMapList(
      SaleOrderLine subSaleOrderLine) {
    List<Map<String, Object>> saleOrderLineDetailsMapList = new ArrayList<>();
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        subSaleOrderLine.getSaleOrderLineDetailsList();
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetail : saleOrderLineDetailsList) {
        saleOrderLineDetailsMapList.add(Mapper.toMap(saleOrderLineDetail));
      }
    }
    return saleOrderLineDetailsMapList;
  }
}
