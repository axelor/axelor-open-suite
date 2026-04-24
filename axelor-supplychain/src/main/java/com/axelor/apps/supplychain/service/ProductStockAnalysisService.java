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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import java.math.BigDecimal;

public interface ProductStockAnalysisService {

  /** Compute the stock coverage ratio in days: stock / (sales / 360). */
  BigDecimal computeSlowOrFastMover(Product product);

  /** Compute total sales quantity for a product over the last 12 months. */
  BigDecimal computeSales(Product product);

  /** Compute current total stock for a product across all stock locations. */
  BigDecimal computeStocks(Product product);

  /** Compute stock level 6 months ago for a product. */
  BigDecimal computeStocks6MonthsAgo(Product product);
}
