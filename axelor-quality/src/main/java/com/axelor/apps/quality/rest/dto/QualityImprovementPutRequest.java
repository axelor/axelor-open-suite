/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.quality.db.QIAnalysisMethod;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import jakarta.validation.constraints.NotNull;

public class QualityImprovementPutRequest extends RequestStructure
    implements QualityImprovementRequest {

  @NotNull protected Integer type;
  protected Integer gravityType;
  @NotNull protected Long qiDetectionId;
  protected Long analysisMethodId;

  protected QIIdentificationRequest qiIdentification;
  protected QIResolutionRequest qiResolution;

  @Override
  public Integer getType() {
    return type;
  }

  @Override
  public void setType(Integer type) {
    this.type = type;
  }

  @Override
  public Integer getGravityType() {
    return gravityType;
  }

  @Override
  public void setGravityType(Integer gravityType) {
    this.gravityType = gravityType;
  }

  @Override
  public Long getQiDetectionId() {
    return qiDetectionId;
  }

  @Override
  public void setQiDetectionId(Long qiDetectionId) {
    this.qiDetectionId = qiDetectionId;
  }

  @Override
  public Long getAnalysisMethodId() {
    return analysisMethodId;
  }

  @Override
  public void setAnalysisMethodId(Long analysisMethodId) {
    this.analysisMethodId = analysisMethodId;
  }

  @Override
  public QIIdentificationRequest getQiIdentification() {
    return qiIdentification;
  }

  @Override
  public void setQiIdentification(QIIdentificationRequest qiIdentification) {
    this.qiIdentification = qiIdentification;
  }

  @Override
  public QIResolutionRequest getQiResolution() {
    return qiResolution;
  }

  @Override
  public void setQiResolution(QIResolutionRequest qiResolution) {
    this.qiResolution = qiResolution;
  }

  @Override
  public QIDetection fetchQIDetection() {
    if (qiDetectionId == null || qiDetectionId == 0L) {
      return null;
    }
    return ObjectFinder.find(QIDetection.class, qiDetectionId, ObjectFinder.NO_VERSION);
  }

  @Override
  public QIAnalysisMethod fetchAnalysisMethod() {
    if (analysisMethodId == null || analysisMethodId == 0L) {
      return null;
    }
    return ObjectFinder.find(QIAnalysisMethod.class, analysisMethodId, ObjectFinder.NO_VERSION);
  }
}
