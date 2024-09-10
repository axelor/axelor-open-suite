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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdResidualProduct;

public class ManufOrderResidualProductServiceImpl implements ManufOrderResidualProductService {

  @Override
  public boolean hasResidualProduct(ManufOrder manufOrder) {

    return manufOrder.getToProduceProdProductList().stream()
        .anyMatch(prodProduct -> isResidualProduct(prodProduct, manufOrder));
  }

  @Override
  public boolean isResidualProduct(ProdProduct prodProduct, ManufOrder manufOrder) {
    if (manufOrder.getBillOfMaterial() != null
        && manufOrder.getBillOfMaterial().getProdResidualProductList() != null) {
      return manufOrder.getBillOfMaterial().getProdResidualProductList().stream()
          .map(ProdResidualProduct::getProduct)
          .anyMatch(product -> product.equals(prodProduct.getProduct()));
    }
    return false;
  }
}
