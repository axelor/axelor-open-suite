package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
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
