package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.rest.dto.QualityImprovementRequest;

public interface QualityImprovementCreateAPIService {
  QualityImprovement createQualityImprovement(QualityImprovementRequest qualityImprovementRequest)
      throws AxelorException;
}
