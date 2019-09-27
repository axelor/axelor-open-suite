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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.SupplierCatalog;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface PurchaseProductService {

  public Map<String, Object> getDiscountsFromCatalog(
      SupplierCatalog supplierCatalog, BigDecimal price);

  /**
   * @param product a product
   * @return An optional with the shippingCoef for the company and the partner of the product. An
   *     empty optional if nothing was found
   */
  public Optional<BigDecimal> getShippingCoefFromPartners(Product product);
}
