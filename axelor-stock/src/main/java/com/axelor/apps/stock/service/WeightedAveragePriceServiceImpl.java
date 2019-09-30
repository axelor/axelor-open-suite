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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.List;

@RequestScoped
public class WeightedAveragePriceServiceImpl implements WeightedAveragePriceService {

  protected ProductRepository productRepo;
  protected AppBaseService appBaseService;

  @Inject
  public WeightedAveragePriceServiceImpl(
      ProductRepository productRepo, AppBaseService appBaseService) {
    this.productRepo = productRepo;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void computeAvgPriceForProduct(Product product) {

    BigDecimal productAvgPrice = this.computeAvgPriceForCompany(product, null);

    if (productAvgPrice.compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    product.setAvgPrice(productAvgPrice);
    if (product.getCostTypeSelect() == ProductRepository.COST_TYPE_AVERAGE_PRICE) {
      product.setCostPrice(productAvgPrice);
      if (product.getAutoUpdateSalePrice()) {
        Beans.get(ProductService.class).updateSalePrice(product);
      }
    }
    productRepo.save(product);
  }

  @Override
  public BigDecimal computeAvgPriceForCompany(Product product, Company company) {
    Long productId = product.getId();
    String query =
        "SELECT new list(self.id, self.avgPrice, self.currentQty) FROM StockLocationLine as self "
            + "WHERE self.product.id = "
            + productId
            + " AND self.stockLocation.typeSelect != "
            + StockLocationRepository.TYPE_VIRTUAL;

    if (company != null) {
      query += " AND self.stockLocation.company = " + company.getId();
    }

    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    BigDecimal productAvgPrice = BigDecimal.ZERO;
    BigDecimal qtyTot = BigDecimal.ZERO;
    List<List<Object>> results = JPA.em().createQuery(query).getResultList();
    if (results.isEmpty()) {
      return BigDecimal.ZERO;
    }
    for (List<Object> result : results) {
      BigDecimal avgPrice = (BigDecimal) result.get(1);
      BigDecimal qty = (BigDecimal) result.get(2);
      productAvgPrice = productAvgPrice.add(avgPrice.multiply(qty));
      qtyTot = qtyTot.add(qty);
    }
    if (qtyTot.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    productAvgPrice = productAvgPrice.divide(qtyTot, scale, BigDecimal.ROUND_HALF_UP);
    return productAvgPrice;
  }
}
