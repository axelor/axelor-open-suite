package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;

public interface QualityImprovementUpdateService {

  QualityImprovement updateQualityImprovement(
      QualityImprovement baseQualityImprovement,
      QualityImprovement newQualityImprovement,
      QIIdentification qiIdentification,
      QIResolution qiResolution)
      throws AxelorException;
}
