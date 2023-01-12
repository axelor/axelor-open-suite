/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.rest.dto;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.tool.api.ResponseStructure;

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
