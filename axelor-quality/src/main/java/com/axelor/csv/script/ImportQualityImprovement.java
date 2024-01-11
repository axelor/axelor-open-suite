package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.google.inject.Inject;
import java.util.Map;

public class ImportQualityImprovement {

  @Inject protected SequenceService sequenceService;

  public Object importQualityImprovement(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof QualityImprovement;

    QualityImprovement qualityImprovement = (QualityImprovement) bean;
    qualityImprovement.setSequence(
        sequenceService.getSequenceNumber(
            SequenceRepository.QUALITY_IMPROVEMENT,
            QualityImprovement.class,
            "sequence",
            qualityImprovement));
    return qualityImprovement;
  }
}
