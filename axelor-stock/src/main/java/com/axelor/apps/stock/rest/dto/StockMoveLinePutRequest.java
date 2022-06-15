package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.tool.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockMoveLinePutRequest extends RequestStructure {

  @Min(0)
  @NotNull
  private BigDecimal realQty;

  @Min(StockMoveLineRepository.CONFORMITY_NONE)
  @Max(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT)
  private Integer conformity;

  public StockMoveLinePutRequest() {}

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
}
