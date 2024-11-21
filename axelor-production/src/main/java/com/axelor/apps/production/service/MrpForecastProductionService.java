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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.db.MrpForecast;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MrpForecastProductionService {

  public void generateMrpForecast(
      Period period,
      List<MrpForecast> mrpForecastList,
      StockLocation stockLocation,
      int technicalOrigin);

  public BigDecimal computeTotalForecast(List<MrpForecast> mrpForecastList, Company company)
      throws AxelorException;

  public List<Map<String, Object>> resetMrpForecasts(
      List<MrpForecast> mrpForecastList, Company company) throws AxelorException;
}
