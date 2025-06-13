package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;

public interface QualityImprovementCreateService {

  QualityImprovement createQualityImprovement(
      QualityImprovement qualityImprovement,
      QIIdentification qiIdentification,
      QIResolution qiResolution)
      throws AxelorException;
}
