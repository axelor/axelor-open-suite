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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.PricingService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;
import javax.persistence.PrePersist;

public class PricingListener {

  @PrePersist
  private void onPrePersist(Pricing pricing) throws AxelorException {
    Product product = pricing.getProduct();
    ProductCategory productCategory = pricing.getProductCategory();
    Company company = pricing.getCompany();
    String model = pricing.getConcernedModel().getName();

    long totalPricing =
        Beans.get(PricingService.class)
            .getPricing(
                product,
                productCategory,
                pricing.getCompany(),
                pricing.getConcernedModel().getName(),
                null)
            .count();

    if (totalPricing > 0) {
      throw new PersistenceException(
          String.format(
              I18n.get(IExceptionMessage.PRICING_1),
              product != null ? product.getName() : pricing.getProductCategory().getName(),
              company.getName(),
              model));
    }
  }
}
