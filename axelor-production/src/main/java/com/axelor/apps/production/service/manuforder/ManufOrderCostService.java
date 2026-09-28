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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import java.util.Map;

public interface ManufOrderCostService {

  String MATERIAL_COST = "$materialCost";
  String LABOR_COST = "$laborCost";
  String MACHINE_COST = "$machineCost";
  String SUBCONTRACTING_COST = "$subcontractingCost";
  String REAL_COST = "$realCost";
  String THEORETICAL_COST = "$theoreticalCost";
  String VARIANCE_AMOUNT = "$varianceAmount";
  String VARIANCE_PERCENT = "$variancePercent";

  Map<String, Object> getRealCostValues(ManufOrder manufOrder) throws AxelorException;
}
