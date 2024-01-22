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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.utils.StringTool;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MrpFilterSaleOrderLineServiceImpl implements MrpFilterSaleOrderLineService {

  protected StockLocationService stockLocationService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected MrpLineTypeService mrpLineTypeService;
  protected MrpSaleOrderCheckLateSaleService mrpSaleOrderCheckLateSaleService;

  @Inject
  public MrpFilterSaleOrderLineServiceImpl(
      StockLocationService stockLocationService,
      SaleOrderLineRepository saleOrderLineRepository,
      MrpLineTypeService mrpLineTypeService,
      MrpSaleOrderCheckLateSaleService mrpSaleOrderCheckLateSaleService) {
    this.stockLocationService = stockLocationService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.mrpLineTypeService = mrpLineTypeService;
    this.mrpSaleOrderCheckLateSaleService = mrpSaleOrderCheckLateSaleService;
  }

  @Override
  public List<Long> getSaleOrderLinesComplyingToMrpLineTypes(Mrp mrp) {

    List<Long> idList = new ArrayList<>();
    idList.add((long) -1);

    List<MrpLineType> saleOrderMrpLineTypeList =
        mrpLineTypeService.getMrpLineTypeList(
            MrpLineTypeRepository.ELEMENT_SALE_ORDER, mrp.getMrpTypeSelect());

    if ((saleOrderMrpLineTypeList != null && !saleOrderMrpLineTypeList.isEmpty())
        && mrp.getStockLocation() != null) {

      List<StockLocation> stockLocationList =
          stockLocationService.getAllLocationAndSubLocation(mrp.getStockLocation(), false).stream()
              .filter(x -> !x.getIsNotInMrp())
              .collect(Collectors.toList());

      for (MrpLineType saleOrderMrpLineType : saleOrderMrpLineTypeList) {
        idList.addAll(
            getSaleOrderLinesComplyingToMrpLineType(mrp, stockLocationList, saleOrderMrpLineType));
        idList = idList.stream().distinct().collect(Collectors.toList());
      }
    }

    return idList;
  }

  protected List<Long> getSaleOrderLinesComplyingToMrpLineType(
      Mrp mrp, List<StockLocation> stockLocationList, MrpLineType saleOrderMrpLineType) {

    List<Integer> statusList = StringTool.getIntegerList(saleOrderMrpLineType.getStatusSelect());

    String filter =
        "self.product.productTypeSelect = 'storable'"
            + " AND self.saleOrder IS NOT NULL"
            + " AND self.product.excludeFromMrp = false"
            + " AND self.product.stockManaged = true"
            + " AND self.deliveryState != :deliveryState"
            + " AND self.saleOrder.company.id = :companyId"
            + " AND self.saleOrder.stockLocation IN (:stockLocations)"
            + " AND (:mrpTypeSelect = :mrpTypeMrp OR self.product.productSubTypeSelect = :productSubTypeFinished)"
            + " AND self.saleOrder.statusSelect IN (:saleOrderStatusList)"
            + " AND self.deliveredQty < self.qty"
            + " AND (self.saleOrder.archived = false OR self.saleOrder.archived is null)";

    // Checking the one off sales parameter
    if (saleOrderMrpLineType.getIncludeOneOffSalesSelect()
        == MrpLineTypeRepository.ONE_OFF_SALES_EXCLUDED) {
      filter += "AND (self.saleOrder.oneoffSale IS NULL OR self.saleOrder.oneoffSale IS FALSE)";
    } else if (saleOrderMrpLineType.getIncludeOneOffSalesSelect()
        == MrpLineTypeRepository.ONE_OFF_SALES_ONLY) {
      filter += "AND self.saleOrder.oneoffSale IS TRUE";
    }

    List<SaleOrderLine> saleOrderLineList =
        saleOrderLineRepository
            .all()
            .filter(filter)
            .bind("deliveryState", SaleOrderLineRepository.DELIVERY_STATE_DELIVERED)
            .bind(
                "companyId",
                mrp.getStockLocation() != null ? mrp.getStockLocation().getCompany().getId() : -1)
            .bind("stockLocations", stockLocationList)
            .bind("mrpTypeSelect", mrp.getMrpTypeSelect())
            .bind("mrpTypeMrp", MrpRepository.MRP_TYPE_MRP)
            .bind("productSubTypeFinished", ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT)
            .bind("saleOrderStatusList", statusList)
            .fetch();
    return saleOrderLineList.stream()
        .filter(
            saleOrderLine ->
                mrpSaleOrderCheckLateSaleService.checkLateSalesParameter(
                    saleOrderLine, saleOrderMrpLineType))
        .map(SaleOrderLine::getId)
        .collect(Collectors.toList());
  }
}
