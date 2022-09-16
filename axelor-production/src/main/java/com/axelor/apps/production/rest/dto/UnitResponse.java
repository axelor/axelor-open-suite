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
