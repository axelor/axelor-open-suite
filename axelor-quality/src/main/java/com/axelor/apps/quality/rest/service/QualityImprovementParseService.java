package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.rest.dto.QIIdentificationRequest;
import com.axelor.apps.quality.rest.dto.QIResolutionRequest;
import com.axelor.apps.quality.rest.dto.QualityImprovementRequest;

public interface QualityImprovementParseService {
  QualityImprovement getQualityImprovementFromRequestBody(QualityImprovementRequest requestBody);

  QIIdentification getQiIdentificationFromRequestBody(
      QIIdentificationRequest qiIdentificationRequest, QIDetection qiDetection)
      throws AxelorException;

  QIResolution getQiResolutionFromRequestBody(QIResolutionRequest qiResolutionPostRequest)
      throws AxelorException;

  int filterQIResolutionDefaultValues(QIResolution qiResolution, int qiType);
}
