package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.tool.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class StockCorrectionPutRequest implements RequestStructure {

  @Min(StockCorrectionRepository.STATUS_DRAFT)
  @Max(StockCorrectionRepository.STATUS_VALIDATED)
  private Integer status;

  @Min(0)
  private BigDecimal realQty;

  public StockCorrectionPutRequest() {}

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public void setRealQty(BigDecimal realQty) {
    this.realQty = realQty;
  }
}
