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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ShippingCoef;
import com.axelor.apps.base.db.repo.ShippingCoefRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class ShippingCoefService {

  protected ShippingCoefRepository shippingCoefRepo;

  @Inject
  public ShippingCoefService(ShippingCoefRepository shippingCoefRepo) {
    this.shippingCoefRepo = shippingCoefRepo;
  }

  /**
   * @param product
   * @param partner
   * @param company
   * @return the shipping coefficient for the given product, partner and company.
   */
  public BigDecimal getShippingCoef(Product product, Partner partner, Company company) {
    BigDecimal shippingCoef = BigDecimal.ZERO;
    List<ShippingCoef> shippingCoefList =
        shippingCoefRepo
            .all()
            .filter(
                "self.supplierCatalog.product = ?1"
                    + " AND self.supplierCatalog.supplierPartner = ?2",
                product,
                partner)
            .fetch();

    if (shippingCoefList == null || shippingCoefList.isEmpty()) {
      return shippingCoef;
    }

    for (ShippingCoef shippingCoefObject : shippingCoefList) {
      if (shippingCoefObject.getCompany().equals(company)) {
        shippingCoef = shippingCoefObject.getShippingCoef();
      }
    }

    return shippingCoef;
  }
}
