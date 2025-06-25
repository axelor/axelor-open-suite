package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class QualityImprovementCreateServiceImpl implements QualityImprovementCreateService {

  protected QualityImprovementRepository qualityImprovementRepository;
  protected QualityImprovementService qualityImprovementService;
  protected QualityImprovementCheckValuesService qualityImprovementCheckValuesService;

  @Inject
  public QualityImprovementCreateServiceImpl(
      QualityImprovementRepository qualityImprovementRepository,
      QualityImprovementService qualityImprovementService,
      QualityImprovementCheckValuesService qualityImprovementCheckValuesService) {
    this.qualityImprovementRepository = qualityImprovementRepository;
    this.qualityImprovementService = qualityImprovementService;
    this.qualityImprovementCheckValuesService = qualityImprovementCheckValuesService;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public QualityImprovement createQualityImprovement(
      QualityImprovement qualityImprovement,
      QIIdentification qiIdentification,
      QIResolution qiResolution)
      throws AxelorException {

    qualityImprovement.setCompany(AuthUtils.getUser().getActiveCompany());

    qiIdentification.setQi(qualityImprovement);
    qiResolution.setQi(qualityImprovement);
    qualityImprovement.setQiIdentification(qiIdentification);
    qualityImprovement.setQiResolution(qiResolution);

    qualityImprovement.setQiStatus(qualityImprovementService.getDefaultQIStatus());
    qualityImprovementCheckValuesService.checkQualityImprovementValues(qualityImprovement);

    return qualityImprovementRepository.save(qualityImprovement);
  }
}
