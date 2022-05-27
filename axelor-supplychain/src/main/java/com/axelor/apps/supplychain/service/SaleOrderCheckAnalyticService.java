/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public interface SaleOrderCheckAnalyticService {

  /**
   * Checks every sale order line for analytic distribution. An exception will be thrown with the
   * list of lines missing analytic distribution information.
   *
   * @param saleOrder a non null sale order
   * @throws AxelorException if one or more lines are missing an analytic distribution
   */
  void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException;
}
