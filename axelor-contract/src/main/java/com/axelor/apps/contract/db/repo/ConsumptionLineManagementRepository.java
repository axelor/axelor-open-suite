/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBase;
import java.util.Map;

public class ConsumptionLineManagementRepository extends ConsumptionLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    AppBase appBase = Beans.get(AppBaseService.class).getAppBase();
    json.put("$nbDecimalDigitForQty", appBase.getNbDecimalDigitForQty());
    return super.populate(json, context);
  }
}
