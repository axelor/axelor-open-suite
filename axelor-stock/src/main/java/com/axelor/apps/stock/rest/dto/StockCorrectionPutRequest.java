package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockCorrectionReason;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class StockCorrectionPutRequest extends RequestStructure {

  @Min(StockCorrectionRepository.STATUS_DRAFT)
  @Max(StockCorrectionRepository.STATUS_VALIDATED)
  private Integer status;

  @Min(0)
  private BigDecimal realQty;

  @Min(0)
  private Long reasonId;

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

  public Long getReasonId() {
    return reasonId;
  }

  public void setReasonId(Long reasonId) {
    this.reasonId = reasonId;
  }

  // Transform id to object
  public StockCorrectionReason fetchReason() {
    if (this.reasonId != null) {
      return ObjectFinder.find(StockCorrectionReason.class, reasonId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
