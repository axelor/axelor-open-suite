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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.report.engine.ReportSettings;
import java.time.LocalDateTime;

public interface StockLocationPrintService {

  ReportSettings print(
      Integer printType,
      String exportType,
      String financialDataDateTimeString,
      Boolean withoutDetailsByStockLocation,
      Long... stockLocationIds)
      throws AxelorException;

  ReportSettings print(
      Integer printType,
      String exportType,
      LocalDateTime financialDataDateTime,
      Boolean withoutDetailsByStockLocation,
      Long... stockLocationIds)
      throws AxelorException;
}
