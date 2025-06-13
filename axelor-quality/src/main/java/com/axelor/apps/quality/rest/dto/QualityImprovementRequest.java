package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.quality.db.QIAnalysisMethod;
import com.axelor.apps.quality.db.QIDetection;

public interface QualityImprovementRequest {

  public Integer getType();

  public void setType(Integer type);

  public Integer getGravityType();

  public void setGravityType(Integer gravityType);

  public Long getQiDetectionId();

  public void setQiDetectionId(Long qiDetectionId);

  public Long getAnalysisMethodId();

  public void setAnalysisMethodId(Long analysisMethodId);

  public QIIdentificationRequest getQiIdentification();

  public void setQiIdentification(QIIdentificationRequest qiIdentification);

  public QIResolutionRequest getQiResolution();

  public void setQiResolution(QIResolutionRequest qiResolution);

  public QIDetection fetchQIDetection();

  public QIAnalysisMethod fetchAnalysisMethod();
}
