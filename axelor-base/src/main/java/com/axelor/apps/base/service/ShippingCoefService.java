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
   * Get the shipping coefficient of a product using the supplier catalog
   *
   * @param product
   * @param partner
   * @param company
   * @param qty
   * @return the shipping coefficient for the given product, partner and company.
   */
  public BigDecimal getShippingCoefDefByPartner(
      Product product, Partner partner, Company company, BigDecimal qty) {
    BigDecimal shippingCoef = BigDecimal.ONE;
    if (partner == null || company == null) {
      return shippingCoef;
    }
    List<ShippingCoef> shippingCoefList =
        shippingCoefRepo
            .all()
            .filter(
                "self.supplierCatalog.product.id = :productId"
                    + " AND self.supplierCatalog.supplierPartner.id = :partnerId"
                    + " AND self.supplierCatalog.minQty <= :qty")
            .bind("productId", product.getId())
            .bind("partnerId", partner.getId())
            .bind("qty", qty)
            .order("supplierCatalog.minQty")
            .fetch();

    if (shippingCoefList == null || shippingCoefList.isEmpty()) {
      return shippingCoef;
    }

    for (ShippingCoef shippingCoefObject : shippingCoefList) {
      if (company.equals(shippingCoefObject.getCompany())) {
        shippingCoef = shippingCoefObject.getShippingCoef();
      }
    }

    return shippingCoef;
  }

  /**
   * Get the shipping coefficient of a product according to the product configuration
   *
   * @param product
   * @param supplierPartner
   * @param company
   * @param qty
   * @return the shipping coefficient for a product
   */
  public BigDecimal getShippingCoef(
      Product product, Partner supplierPartner, Company company, BigDecimal qty) {
    BigDecimal shippingCoef;

    if (product.getDefShipCoefByPartner()) {
      shippingCoef = getShippingCoefDefByPartner(product, supplierPartner, company, qty);
    } else {
      shippingCoef = product.getShippingCoef();
    }

    if (shippingCoef.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ONE;
    }
    return shippingCoef;
  }
}
