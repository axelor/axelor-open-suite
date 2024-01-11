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
package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockMoveLinePutRequest extends RequestStructure {
  @Min(0)
  private Long unitId;

  @Min(0)
  @NotNull
  private BigDecimal realQty;

  @Min(StockMoveLineRepository.CONFORMITY_NONE)
  @Max(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT)
  private Integer conformity;

  public StockMoveLinePutRequest() {}

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public void setRealQty(BigDecimal realQty) {
    this.realQty = realQty;
  }

  public Integer getConformity() {
    return conformity;
  }

  public void setConformity(Integer conformity) {
    this.conformity = conformity;
  }

  public Unit fetchUnit() {
    if (this.unitId != null) {
      return ObjectFinder.find(Unit.class, unitId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
