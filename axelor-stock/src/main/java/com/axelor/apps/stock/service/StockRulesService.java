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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public interface StockRulesService {
  void generateOrder(Product product, BigDecimal qty, StockLocationLine stockLocationLine, int type)
      throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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
