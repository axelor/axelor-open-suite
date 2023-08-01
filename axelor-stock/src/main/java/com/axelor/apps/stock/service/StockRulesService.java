/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import java.math.BigDecimal;

public interface StockRulesService {
  void generateOrder(Product product, BigDecimal qty, StockLocationLine stockLocationLine, int type)
      throws AxelorException;

  void generatePurchaseOrder(
      Product product, BigDecimal qty, StockLocationLine stockLocationLine, int type)
      throws AxelorException;

  boolean useMinStockRules(
      StockLocationLine stockLocationLine, StockRules stockRules, BigDecimal qty, int type);

  StockRules getStockRules(Product product, StockLocation stockLocation, int type, int useCase);

  BigDecimal getQtyToOrder(
      BigDecimal qty,
      StockLocationLine stockLocationLine,
      int type,
      StockRules stockRules,
      BigDecimal minReorderQty);

  BigDecimal getQtyToOrder(
      BigDecimal qty, StockLocationLine stockLocationLine, int type, StockRules stockRules);
}
