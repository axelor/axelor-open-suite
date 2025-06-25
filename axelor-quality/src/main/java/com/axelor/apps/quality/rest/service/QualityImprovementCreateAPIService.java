package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.rest.dto.QualityImprovementCreateUpdateResult;
import com.axelor.apps.quality.rest.dto.QualityImprovementPostRequest;

public interface QualityImprovementCreateAPIService {
  QualityImprovementCreateUpdateResult createQualityImprovement(
      QualityImprovementPostRequest qualityImprovementRequest) throws AxelorException;
}
