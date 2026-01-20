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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@RequestScoped
public class WeightedAveragePriceServiceImpl implements WeightedAveragePriceService {

  protected ProductRepository productRepo;
  protected AppBaseService appBaseService;
  protected ProductCompanyService productCompanyService;

  @Inject
  public WeightedAveragePriceServiceImpl(
      ProductRepository productRepo,
      AppBaseService appBaseService,
      ProductCompanyService productCompanyService) {
    this.productRepo = productRepo;
    this.appBaseService = appBaseService;
    this.productCompanyService = productCompanyService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeAvgPriceForProduct(Product product) throws AxelorException {

    Boolean avgPriceHandledByCompany = false;
    Set<MetaField> companySpecificFields =
        appBaseService.getAppBase().getCompanySpecificProductFieldsSet();
    for (MetaField field : companySpecificFields) {
      if (field.getName().equals("avgPrice")) {
        avgPriceHandledByCompany = true;
        break;
      }
    }
    ProductService productService = Beans.get(ProductService.class);
    if (avgPriceHandledByCompany
        && product.getProductCompanyList() != null
        && !product.getProductCompanyList().isEmpty()) {
      for (ProductCompany productCompany : product.getProductCompanyList()) {
        Company company = productCompany.getCompany();
        BigDecimal productAvgPrice = this.computeAvgPriceForCompany(product, company);
        if (productAvgPrice.compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }

        productCompanyService.set(product, "avgPrice", productAvgPrice, company);
        if ((Integer) productCompanyService.get(product, "costTypeSelect", company)
            == ProductRepository.COST_TYPE_AVERAGE_PRICE) {
          productCompanyService.set(product, "costPrice", productAvgPrice, company);
          if ((Boolean) productCompanyService.get(product, "autoUpdateSalePrice", company)) {
            productService.updateSalePrice(product, company);
          }
        }
      }
    } else {
      BigDecimal productAvgPrice = this.computeAvgPriceForCompany(product, null);

      if (productAvgPrice.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }

      product.setAvgPrice(productAvgPrice);
      if (product.getCostTypeSelect() == ProductRepository.COST_TYPE_AVERAGE_PRICE) {
        product.setCostPrice(productAvgPrice);
        if (product.getAutoUpdateSalePrice()) {
          productService.updateSalePrice(product, null);
        }
      }
    }
    JPA.save(product);
  }

  @Override
  public BigDecimal computeAvgPriceForCompany(Product product, Company company) {
    Long productId = product.getId();

    String jpql =
        "SELECT COALESCE(SUM(self.avgPrice * self.currentQty), 0), COALESCE(SUM(self.currentQty), 0) "
            + "FROM StockLocationLine self "
            + "WHERE self.product.id = :productId "
            + "AND self.stockLocation.typeSelect != :virtualType ";

    if (company != null) {
      jpql += " AND self.stockLocation.company.id = :companyId";
    }

    Query query = JPA.em().createQuery(jpql);
    query.setParameter("productId", productId);
    query.setParameter("virtualType", StockLocationRepository.TYPE_VIRTUAL);

    if (company != null) {
      query.setParameter("companyId", company.getId());
    }

    Object[] result = (Object[]) query.getSingleResult();

    if (result == null || result.length == 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal totalWeightedValue = (BigDecimal) result[0];
    BigDecimal totalQty = (BigDecimal) result[1];

    if (totalQty.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    return totalWeightedValue.divide(totalQty, scale, RoundingMode.HALF_UP);
  }
}
