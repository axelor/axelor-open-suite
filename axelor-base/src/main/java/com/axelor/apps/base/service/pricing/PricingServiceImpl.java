/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PricingServiceImpl implements PricingService {

  protected PricingRepository pricingRepo;
  protected AppBaseService appBaseService;
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public PricingServiceImpl(PricingRepository pricingRepo, AppBaseService appBaseService) {
    this.pricingRepo = pricingRepo;
    this.appBaseService = appBaseService;
  }

  @Override
  public Optional<Pricing> getRandomPricing(
      Company company,
      Product product,
      ProductCategory productCategory,
      String modelName,
      Pricing previousPricing) {

    return getPricings(company, product, productCategory, modelName, previousPricing).stream()
        .findAny();
  }

  @Override
  public List<Pricing> getPricings(
      Company company,
      Product product,
      ProductCategory productCategory,
      String modelName,
      Pricing previousPricing) {

    LOG.debug("Fetching pricings");
    StringBuilder filter = new StringBuilder();
    Map<String, Object> bindings = new HashMap<>();

    filter.append("self.startDate <= :todayDate ");
    filter.append("AND (self.endDate > :todayDate OR self.endDate = NULL) ");
    bindings.put("todayDate", appBaseService.getTodayDate(company));

    if (company != null) {
      filter.append("AND self.company = :company ");
      bindings.put("company", company);
    }

    if (modelName != null) {
      filter.append("AND self.concernedModel.name = :modelName ");
      bindings.put("modelName", modelName);
    }

    if (previousPricing != null) {
      filter.append("AND self.previousPricing = :previousPricing ");
      bindings.put("previousPricing", previousPricing);
    } else {
      filter.append("AND self.previousPricing is NULL ");
    }

    appendProductFilter(product, productCategory, filter, bindings);
    LOG.debug("Filtering pricing with {}", filter.toString());
    return pricingRepo.all().filter(filter.toString()).bind(bindings).fetch();
  }

  protected void appendProductFilter(
      Product product,
      ProductCategory productCategory,
      StringBuilder filter,
      Map<String, Object> bindings) {
    StringBuilder productFilter = new StringBuilder();
    productFilter.append("(");
    if (product != null) {
      productFilter.append("self.product = :product ");
      bindings.put("product", product);

      if (product.getParentProduct() != null) {
        productFilter.append("OR self.product = :parentProduct ");
        bindings.put("parentProduct", product.getParentProduct());
      }
      if (productCategory != null) {
        productFilter.append("OR self.productCategory = :productCategory ");
        bindings.put("productCategory", productCategory);
      }
    } else {
      if (productCategory != null) {
        productFilter.append("self.productCategory = :productCategory ");
        bindings.put("productCategory", productCategory);
      }
    }
    productFilter.append(")");

    // if productFilter is more than just "()"
    if (productFilter.length() > 2) {
      filter.append("AND ");
      filter.append(productFilter);
    }
  }
}
