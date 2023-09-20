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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.report.engine.ReportSettings;
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
  String printManufOrders(List<Long> ids) throws IOException, AxelorException;

  /**
   * Print a single manuf order
   *
   * @param manufOrder
   * @return a path to printed manuf order.
   * @throws AxelorException
   */
  String printManufOrder(ManufOrder manufOrder) throws AxelorException;

  ReportSettings prepareReportSettings(ManufOrder manufOrder) throws AxelorException;

  /** Returns the filename of a printing with multiple manuf orders. */
  String getManufOrdersFilename();

  /** Returns the filename of a printing with one manuf order. */
  String getFileName(ManufOrder manufOrder);
}
