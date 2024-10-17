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
package com.axelor.apps.stock.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

public class StockLocationUtilsServiceImpl implements StockLocationUtilsService {

  protected UnitRepository unitRepository;
  protected UnitConversionService unitConversionService;
  protected AppBaseService appBaseService;

  @Inject
  public StockLocationUtilsServiceImpl(
      UnitRepository unitRepository,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService) {
    this.unitRepository = unitRepository;
    this.unitConversionService = unitConversionService;
    this.appBaseService = appBaseService;
  }

  @Override
  public BigDecimal getRealQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId) throws AxelorException {

    return getQtyOfProductInStockLocations(productId, stockLocationIds, companyId, "currentQty");
  }

  @Override
  public BigDecimal getFutureQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId) throws AxelorException {

    return getQtyOfProductInStockLocations(productId, stockLocationIds, companyId, "futureQty");
  }

  protected BigDecimal getQtyOfProductInStockLocations(
      Long productId, List<Long> stockLocationIds, Long companyId, String qtyFieldName)
      throws AxelorException {
    Product product = JPA.find(Product.class, productId);
    Unit productUnit = product.getUnit();

    StringBuilder query = new StringBuilder();
    Map<String, Object> parameterMap = new HashMap<>();
    query.append("SELECT self.unit.id, sum(self.%s)");
    query.append(" FROM StockLocationLine self");
    query.append(" WHERE self.stockLocation.typeSelect != :stockLocationTypeSelectVirtual");
    query.append(" AND self.product.id = :productId AND self.product.stockManaged is TRUE");

    parameterMap.put("stockLocationTypeSelectVirtual", StockLocationRepository.TYPE_VIRTUAL);
    parameterMap.put("productId", product.getId());

    if (companyId != null && companyId > 0L) {
      query.append(" AND self.stockLocation.company.id = :companyId");
      parameterMap.put("companyId", companyId);
    }

    if (stockLocationIds != null && !stockLocationIds.isEmpty()) {
      query.append(" AND self.stockLocation.id IN (:stockLocationIds)");
      parameterMap.put("stockLocationIds", stockLocationIds);
    }

    query.append(" GROUP BY self.unit.id");

    TypedQuery<Tuple> sumOfQtyPerUnitQuery =
        JPA.em().createQuery(String.format(query.toString(), qtyFieldName), Tuple.class);

    parameterMap.forEach(sumOfQtyPerUnitQuery::setParameter);

    BigDecimal sumOfQty = BigDecimal.ZERO;
    for (Tuple qtyPerUnit : sumOfQtyPerUnitQuery.getResultList()) {
      Long stockLocationLineUnitId = (Long) qtyPerUnit.get(0);
      BigDecimal sumOfQtyOfStockLocationLineUnit = (BigDecimal) qtyPerUnit.get(1);
      if (stockLocationLineUnitId == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(StockExceptionMessage.STOCK_LOCATION_UNIT_NULL));
      }
      if (productUnit != null && !productUnit.getId().equals(stockLocationLineUnitId)) {
        Unit stockLocationLineUnit = unitRepository.find(stockLocationLineUnitId);

        sumOfQty =
            sumOfQty.add(
                unitConversionService.convert(
                    stockLocationLineUnit,
                    productUnit,
                    sumOfQtyOfStockLocationLineUnit,
                    sumOfQtyOfStockLocationLineUnit.scale(),
                    product));
      } else {
        sumOfQty = sumOfQty.add(sumOfQtyOfStockLocationLineUnit);
      }
    }
    return sumOfQty.setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getStockLocationValue(StockLocation stockLocation) {

    Query query =
        JPA.em()
            .createQuery(
                "SELECT SUM( self.currentQty * "
                    + "CASE WHEN (location.company.stockConfig.stockValuationTypeSelect = 1) THEN (self.product.avgPrice) "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 2) THEN "
                    + "CASE WHEN (self.product.costTypeSelect = 3) THEN (self.avgPrice) ELSE (self.product.costPrice) END "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 3) THEN (self.product.salePrice) "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 4) THEN (self.product.purchasePrice) "
                    + "WHEN (location.company.stockConfig.stockValuationTypeSelect = 5) THEN (self.avgPrice) "
                    + "ELSE (self.avgPrice) END ) AS value "
                    + "FROM StockLocationLine AS self "
                    + "LEFT JOIN StockLocation AS location "
                    + "ON location.id= self.stockLocation "
                    + "WHERE self.stockLocation.id =:id");
    query.setParameter("id", stockLocation.getId());

    List<?> result = query.getResultList();
    return (result.get(0) == null || ((BigDecimal) result.get(0)).signum() == 0)
        ? BigDecimal.ZERO
        : ((BigDecimal) result.get(0)).setScale(2, BigDecimal.ROUND_HALF_UP);
  }
}
