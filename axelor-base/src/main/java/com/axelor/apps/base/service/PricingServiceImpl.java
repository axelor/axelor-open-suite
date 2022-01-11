/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.Query;
import com.google.inject.Inject;

public class PricingServiceImpl implements PricingService {

  @Inject private PricingRepository pricingRepo;

  @Inject private AppBaseService appBaseService;

  @Override
  public Query<Pricing> getPricing(
      Product product,
      ProductCategory productCategory,
      Company company,
      String modelName,
      Pricing parentPricing) {

    StringBuilder filter = new StringBuilder();

    filter.append("(self.product = :product ");

    if (product != null && product.getParentProduct() != null) {
      filter.append("OR self.product = :parentProduct ");
    }

    filter.append("OR self.productCategory = :productCategory) ");

    if (parentPricing != null) {
      filter.append("AND self.previousPricing = :parentPricing ");
    } else {
      filter.append("AND self.previousPricing IS NULL ");
    }

    filter.append(
        "AND self.company = :company "
            + "AND self.startDate <= :todayDate "
            + "AND self.concernedModel.name = :modelName");

    return pricingRepo
        .all()
        .filter(filter.toString())
        .bind("product", product)
        .bind("parentProduct", product != null ? product.getParentProduct() : null)
        .bind("productCategory", productCategory)
        .bind("parentPricing", parentPricing)
        .bind("company", company)
        .bind("todayDate", appBaseService.getTodayDate(company))
        .bind("modelName", modelName);
  }
}
