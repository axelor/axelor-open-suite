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
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class StockInternalMovePutRequest extends RequestStructure {

  @Min(0)
  private Long unitId;

  @Min(0)
  private BigDecimal movedQty;

  private String notes;

  @Min(StockMoveRepository.STATUS_DRAFT)
  @Max(StockMoveRepository.STATUS_CANCELED)
  private Integer status;

  public StockInternalMovePutRequest() {}

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public BigDecimal getMovedQty() {
    return movedQty;
  }

  public void setMovedQty(BigDecimal movedQty) {
    this.movedQty = movedQty;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  // Transform id to object
  public Unit fetchUnit() {
    if (this.unitId != null) {
      return ObjectFinder.find(Unit.class, unitId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
