package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.rest.dto.QualityImprovementCreateUpdateResult;
import com.axelor.apps.quality.rest.dto.QualityImprovementPutRequest;

public interface QualityImprovementUpdateAPIService {
  QualityImprovementCreateUpdateResult updateQualityImprovement(
      QualityImprovement qualityImprovement, QualityImprovementPutRequest qualityImprovementRequest)
      throws AxelorException;
}
