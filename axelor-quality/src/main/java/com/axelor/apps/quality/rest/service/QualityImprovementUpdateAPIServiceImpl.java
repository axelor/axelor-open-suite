package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.rest.dto.QualityImprovementRequest;
import com.axelor.apps.quality.service.QualityImprovementUpdateService;
import com.google.inject.Inject;

public class QualityImprovementUpdateAPIServiceImpl implements QualityImprovementUpdateAPIService {

  protected QualityImprovementUpdateService qualityImprovementUpdateService;
  protected QualityImprovementParseService qualityImprovementParseService;

  @Inject
  public QualityImprovementUpdateAPIServiceImpl(
      QualityImprovementUpdateService qualityImprovementUpdateService,
      QualityImprovementParseService qualityImprovementParseService) {
    this.qualityImprovementUpdateService = qualityImprovementUpdateService;
    this.qualityImprovementParseService = qualityImprovementParseService;
  }

  @Override
  public QualityImprovement updateQualityImprovement(
      QualityImprovement qualityImprovement, QualityImprovementRequest qualityImprovementRequest)
      throws AxelorException {

    QualityImprovement newQualityImprovement =
        qualityImprovementParseService.getQualityImprovementFromRequestBody(
            qualityImprovementRequest);

    QIIdentification newQiIdentification =
        qualityImprovementParseService.getQiIdentificationFromRequestBody(
            qualityImprovementRequest.getQiIdentification(), qualityImprovement.getQiDetection());

    QIResolution newQiResolution =
        qualityImprovementParseService.getQiResolutionFromRequestBody(
            qualityImprovementRequest.getQiResolution());

    return qualityImprovementUpdateService.updateQualityImprovement(
        qualityImprovement, newQualityImprovement, newQiIdentification, newQiResolution);
  }
}
