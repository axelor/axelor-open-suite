/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
