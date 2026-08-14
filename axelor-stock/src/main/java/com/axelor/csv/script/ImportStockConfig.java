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
package com.axelor.csv.script;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import jakarta.inject.Inject;
import java.util.Map;

public class ImportStockConfig {

  @Inject private StockConfigRepository stockConfigRepo;
  @Inject private ProductRepository productRepo;

  public Object importStockConfig(Object bean, Map<String, Object> values) {

    assert bean instanceof StockConfig;

    StockConfig stockConfig = (StockConfig) bean;
    stockConfigRepo.save(stockConfig);

    // Products imported earlier in the demo data (base_product.csv in axelor-base) were saved
    // before this StockConfig existed, so ProductStockRepositorySave.addProductCompanies could
    // not create their per-company price lines. Re-saving them now that StockConfig is available
    // fixes it for every product, including services, regardless of which optional apps are
    // installed afterwards.
    for (Product product : productRepo.all().fetch()) {
      productRepo.save(product);
    }

    return stockConfig;
  }
}
