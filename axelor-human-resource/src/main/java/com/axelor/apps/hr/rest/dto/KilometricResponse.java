package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;

public class KilometricResponse extends ResponseStructure {

  protected BigDecimal distance;

  public KilometricResponse(BigDecimal distance) {
    super(0);
    this.distance = distance;
  }

  public BigDecimal getDistance() {
    return distance;
  }
}
