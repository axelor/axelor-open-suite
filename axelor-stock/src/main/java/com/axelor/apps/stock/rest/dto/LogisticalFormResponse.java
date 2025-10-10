package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.utils.api.ResponseStructure;

public class LogisticalFormResponse extends ResponseStructure {

  protected Long logisticalFormId;

  public LogisticalFormResponse(LogisticalForm logisticalForm) {
    super(logisticalForm.getVersion());
    this.logisticalFormId = logisticalForm.getId();
  }

  public Long getLogisticalFormId() {
    return logisticalFormId;
  }
}
