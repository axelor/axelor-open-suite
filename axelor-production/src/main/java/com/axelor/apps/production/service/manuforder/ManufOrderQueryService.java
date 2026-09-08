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

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.script.ScriptAllowed;
import java.util.List;

@ScriptAllowed
public interface ManufOrderQueryService {

  /**
   * Recursively collects the IDs of all ManufOrders descending from the given ManufOrder (children,
   * grandchildren, etc.) via the parentMO relationship.
   *
   * @param manufOrder the root manufacturing order
   * @return list of all descendant ManufOrder IDs
   */
  List<Long> getAllDescendantManufOrderIds(ManufOrder manufOrder);
}
