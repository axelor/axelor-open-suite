/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.List;

public interface ManufOrderPrintService {

  /**
   * Print a list of manuf orders
   *
   * @return ReportSettings
   * @throws IOException
   * @throws AxelorException
   */
  String printManufOrders(List<Long> ids) throws IOException;

  /**
   * Print a single manuf order
   *
   * @param manufOrder
   * @return a path to printed manuf order.
   * @throws AxelorException
   */
  String printManufOrder(ManufOrder manufOrder) throws AxelorException;

  /** Returns the filename of a printing with multiple manuf orders. */
  String getManufOrdersFilename();

  /** Returns the filename of a printing with one manuf order. */
  String getFileName(ManufOrder manufOrder);
}
