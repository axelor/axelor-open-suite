package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QualityImprovement;

public interface QualityImprovementCheckValuesService {
  void checkQualityImprovementValues(QualityImprovement qualityImprovement) throws AxelorException;
}
