/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.stock.service.StockLocationFetchService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.db.JPA;
import com.axelor.utils.helpers.StringHelper;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MrpFilterSaleOrderLineServiceImpl implements MrpFilterSaleOrderLineService {

  protected StockLocationFetchService stockLocationFetchService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected MrpLineTypeService mrpLineTypeService;
  protected MrpSaleOrderCheckLateSaleService mrpSaleOrderCheckLateSaleService;

  @Inject
  public MrpFilterSaleOrderLineServiceImpl(
      StockLocationFetchService stockLocationFetchService,
      SaleOrderLineRepository saleOrderLineRepository,
      MrpLineTypeService mrpLineTypeService,
      MrpSaleOrderCheckLateSaleService mrpSaleOrderCheckLateSaleService) {
    this.stockLocationFetchService = stockLocationFetchService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.mrpLineTypeService = mrpLineTypeService;
    this.mrpSaleOrderCheckLateSaleService = mrpSaleOrderCheckLateSaleService;
  }

  @Override
  public List<Long> getSaleOrderLinesComplyingToMrpLineTypes(Mrp mrp) {

    Set<Long> idSet = new LinkedHashSet<>();
    idSet.add(0L);

    if (mrp.getStockLocation() == null) {
      return new ArrayList<>(idSet);
    }

    List<MrpLineType> saleOrderMrpLineTypeList =
        mrpLineTypeService.getMrpLineTypeList(
            MrpLineTypeRepository.ELEMENT_SALE_ORDER, mrp.getMrpTypeSelect());

    if (CollectionUtils.isEmpty(saleOrderMrpLineTypeList)) {
      return new ArrayList<>(idSet);
    }

    EntityManager em = JPA.em();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    Root<StockLocation> root = cb.createQuery().from(StockLocation.class);

    List<Predicate> extraFilters = new ArrayList<>();
    extraFilters.add(cb.isFalse(root.get("isNotInMrp")));

    List<Long> stockLocationList =
        stockLocationFetchService.getAllLocationAndSubLocation(
            mrp.getStockLocation().getId(), false, extraFilters);

    for (MrpLineType saleOrderMrpLineType : saleOrderMrpLineTypeList) {
      idSet.addAll(
          getSaleOrderLinesComplyingToMrpLineType(mrp, stockLocationList, saleOrderMrpLineType));
    }

    return new ArrayList<>(idSet);
  }

  protected List<Long> getSaleOrderLinesComplyingToMrpLineType(
      Mrp mrp, List<Long> stockLocationList, MrpLineType saleOrderMrpLineType) {

    List<Integer> statusList = StringHelper.getIntegerList(saleOrderMrpLineType.getStatusSelect());

    String filter =
        "self.product.productTypeSelect = 'storable'"
            + " AND self.saleOrder IS NOT NULL"
            + " AND self.product.excludeFromMrp = false"
            + " AND self.product.stockManaged = true"
            + " AND self.deliveryState != :deliveryState"
            + " AND self.saleOrder.company.id = :companyId"
            + " AND self.saleOrder.stockLocation.id IN (:stockLocations)"
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
