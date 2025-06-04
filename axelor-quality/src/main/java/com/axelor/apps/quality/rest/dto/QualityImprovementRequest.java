package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.quality.db.QIAnalysisMethod;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.NotNull;

public class QualityImprovementRequest extends RequestStructure {

  @NotNull protected Integer type;
  protected Integer gravityType;
  @NotNull protected Long qiDetectionId;
  protected Long analysisMethodId;

  protected QIIdentificationRequest qiIdentification;
  protected QIResolutionRequest qiResolution;

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public Integer getGravityType() {
    return gravityType;
  }

  public void setGravityType(Integer gravityType) {
    this.gravityType = gravityType;
  }

  public Long getQiDetectionId() {
    return qiDetectionId;
  }

  public void setQiDetectionId(Long qiDetectionId) {
    this.qiDetectionId = qiDetectionId;
  }

  public Long getAnalysisMethodId() {
    return analysisMethodId;
  }

  public void setAnalysisMethodId(Long analysisMethodId) {
    this.analysisMethodId = analysisMethodId;
  }

  public QIIdentificationRequest getQiIdentification() {
    return qiIdentification;
  }

  public void setQiIdentification(QIIdentificationRequest qiIdentification) {
    this.qiIdentification = qiIdentification;
  }

  public QIResolutionRequest getQiResolution() {
    return qiResolution;
  }

  public void setQiResolution(QIResolutionRequest qiResolution) {
    this.qiResolution = qiResolution;
  }

  public QIDetection fetchQIDetection() {
    if (qiDetectionId == null || qiDetectionId == 0L) {
      return null;
    }
    return ObjectFinder.find(QIDetection.class, qiDetectionId, ObjectFinder.NO_VERSION);
  }

  public QIAnalysisMethod fetchAnalysisMethod() {
    if (analysisMethodId == null || analysisMethodId == 0L) {
      return null;
    }
    return ObjectFinder.find(QIAnalysisMethod.class, analysisMethodId, ObjectFinder.NO_VERSION);
  }
}
