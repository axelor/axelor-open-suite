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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.stock.db.repo.product.ProductStockRepositoryPopulate;
import com.axelor.apps.stock.db.repo.product.ProductStockRepositorySave;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class ProductStockRepository extends ProductBaseRepository {

  @Inject protected ProductStockRepositorySave productStockRepositorySave;
  @Inject protected ProductStockRepositoryPopulate productStockRepositoryPopulate;

  @Override
  public Product save(Product product) {
    productStockRepositorySave.addProductCompanies(product);
    return super.save(product);
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (Boolean.TRUE.equals(context.get("_xFillProductAvailableQty"))
        || (context.get("_parent") != null
            && Boolean.TRUE.equals(
                ((Map) context.get("_parent")).get("_xFillProductAvailableQty")))) {
      productStockRepositoryPopulate.setAvailableQty(json, context);
    }

    if (context.containsKey("fromStockWizard")) {
      productStockRepositoryPopulate.fillFromStockWizard(json, context);
    }

    return json;
  }

  @Override
  public Product copy(Product product, boolean deep) {
    Product copy = super.copy(product, deep);
    copy.setAvgPrice(BigDecimal.ZERO);
    return copy;
  }
}
