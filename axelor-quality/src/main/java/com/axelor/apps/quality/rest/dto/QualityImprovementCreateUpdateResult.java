package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.quality.db.QualityImprovement;

public class QualityImprovementCreateUpdateResult {

  protected QualityImprovement qualityImprovement;
  protected String errorMessage;

  public QualityImprovement getQualityImprovement() {
    return qualityImprovement;
  }

  public void setQualityImprovement(QualityImprovement qualityImprovement) {
    this.qualityImprovement = qualityImprovement;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
