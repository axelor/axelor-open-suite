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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.StringUtils;

public class SaleOrderLineDomainProductionServiceImpl
    implements SaleOrderLineDomainProductionService {
  @Override
  public String getBomDomain(SaleOrderLine saleOrderLine) {
    String domain = getProdProcessDomain(saleOrderLine);
    if (StringUtils.isEmpty(domain)) {
      return domain;
    }
    return domain + " AND self.defineSubBillOfMaterial = true";
  }

  @Override
  public String getProdProcessDomain(SaleOrderLine saleOrderLine) {
    StringBuilder domain = new StringBuilder();
    Product product = saleOrderLine.getProduct();
    if (product == null) {
      return "";
    }
    domain.append("self.product.id = ");
    domain.append(product.getId());

    Product parentProduct = product.getParentProduct();
    if (parentProduct != null) {
      domain.append(" OR self.product.id = ");
      domain.append(parentProduct.getId());
    }

    return domain.toString();
  }
}
