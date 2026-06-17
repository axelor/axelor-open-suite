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
import com.axelor.apps.supplychain.db.StockRotationCategory;
import javax.script.ScriptException;

public interface StockRotationCategoryService {

  String VAR_COVERAGE_IN_DAYS = "coverageInDays";
  String VAR_SALES_LAST_12_MONTHS = "salesLast12Months";
  String VAR_CURRENT_STOCK = "currentStock";
  String VAR_STOCK_6_MONTHS_AGO = "stock6MonthsAgo";

  /**
   * Validate that the formula of a stock rotation category is syntactically correct by executing it
   * against dummy values for all supported variables.
   */
  void checkFormula(StockRotationCategory stockRotationCategory) throws ScriptException;

  /**
   * Evaluate the formula of a stock rotation category against the stock metrics of a product.
   *
   * @return true if the product matches this category, false otherwise.
   */
  boolean evaluateFormula(StockRotationCategory stockRotationCategory, Product product);

  /**
   * Find the first stock rotation category whose formula evaluates to true for the given product.
   * Categories are evaluated in their natural order (by id).
   *
   * @return the matching category or null if none match.
   */
  StockRotationCategory findMatchingCategory(Product product);

  /**
   * Compute the stock rotation category for the given product and assign it to {@link
   * Product#getStockRotationCategory()}. Does nothing if no category matches.
   */
  void assignStockRotationCategory(Product product);
}
