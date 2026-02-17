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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.i18n.I18n;
import com.google.common.collect.Sets;
import jakarta.inject.Inject;
import java.util.Set;

public class DepRateCalculationProductServiceImpl implements DepRateCalculationProductService {

  protected final ProductRepository productRepository;
  protected final StockLocationLineRepository stockLocationLineRepository;

  @Inject
  public DepRateCalculationProductServiceImpl(
      ProductRepository productRepository,
      StockLocationLineRepository stockLocationLineRepository) {
    this.productRepository = productRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
  }

  @Override
  public Set<Product> getProducts(UnitCostCalculation unitCostCalculation) throws AxelorException {

    Set<Product> productSet = Sets.newHashSet();

    if (!unitCostCalculation.getProductSet().isEmpty()) {
      productSet.addAll(unitCostCalculation.getProductSet());
    }

    if (!unitCostCalculation.getProductCategorySet().isEmpty()) {
      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productCategory in (:productCategorySet) AND self.productTypeSelect = :productTypeSelect"
                      + " AND self.dtype = 'Product'"
                      + " AND self.stockManaged IS TRUE"
                      + " AND self.stockCategorySelect != 0")
              .bind("productCategorySet", unitCostCalculation.getProductCategorySet())
              .bind("productTypeSelect", ProductRepository.PRODUCT_TYPE_STORABLE)
              .fetch());
    }

    if (!unitCostCalculation.getProductFamilySet().isEmpty()) {
      productSet.addAll(
          productRepository
              .all()
              .filter(
                  "self.productFamily in (:productFamily) AND self.productTypeSelect = :productTypeSelect"
                      + " AND self.dtype = 'Product'"
                      + " AND self.stockManaged = true"
                      + " AND self.stockCategorySelect != 0")
              .bind("productFamily", unitCostCalculation.getProductFamilySet())
              .bind("productTypeSelect", ProductRepository.PRODUCT_TYPE_STORABLE)
              .fetch());
    }

    if (productSet.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.UNIT_COST_CALCULATION_CHOOSE_FILTERS));
    }

    // Filter by customer stock locations if needed
    Set<Product> finalProductSet = Sets.newHashSet();
    for (Product product : productSet) {
      long count =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.product = :product AND coalesce(self.stockLocation.customerOk,false) = :customerOk")
              .bind("product", product)
              .bind("customerOk", unitCostCalculation.getCustomerOk())
              .count();

      if (count > 0) {
        finalProductSet.add(product);
      }
    }

    return finalProductSet;
  }
}
