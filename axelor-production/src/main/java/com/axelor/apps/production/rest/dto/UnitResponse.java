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
package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Unit;
import com.axelor.utils.api.ResponseStructure;

public class UnitResponse extends ResponseStructure {
  private final Long unitId;
  private final String unitName;

  public UnitResponse(Unit unit) {
    super(unit.getVersion());
    this.unitId = unit.getId();
    this.unitName = unit.getName();
  }

  public Long getUnitId() {
    return unitId;
  }

  public String getUnitName() {
    return unitName;
  }
}
