package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.rest.dto.QualityImprovementCreateUpdateResult;
import com.axelor.apps.quality.rest.dto.QualityImprovementPutRequest;
import com.axelor.apps.quality.service.QualityImprovementUpdateService;
import com.axelor.i18n.I18n;
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
  public QualityImprovementCreateUpdateResult updateQualityImprovement(
      QualityImprovement qualityImprovement, QualityImprovementPutRequest qualityImprovementRequest)
      throws AxelorException {
    QualityImprovementCreateUpdateResult qualityImprovementCreateUpdateResult =
        new QualityImprovementCreateUpdateResult();

    QualityImprovement newQualityImprovement =
        qualityImprovementParseService.getQualityImprovementFromRequestBody(
            qualityImprovementRequest);

    QIIdentification newQiIdentification =
        qualityImprovementParseService.getQiIdentificationFromRequestBody(
            qualityImprovementRequest.getQiIdentification(), qualityImprovement.getQiDetection());

    QIResolution newQiResolution =
        qualityImprovementParseService.getQiResolutionFromRequestBody(
            qualityImprovementRequest.getQiResolution());

    int errors =
        qualityImprovementParseService.filterQIResolutionDefaultValues(
            newQiResolution, qualityImprovement.getType());

    if (errors > 0) {
      String errorMessage =
          String.format(I18n.get(QualityExceptionMessage.API_QI_RESOLUTION_DEFAULT_ERROR), errors);
      qualityImprovementCreateUpdateResult.setErrorMessage(errorMessage);
    }

    qualityImprovementUpdateService.updateQualityImprovement(
        qualityImprovement, newQualityImprovement, newQiIdentification, newQiResolution);

    qualityImprovementCreateUpdateResult.setQualityImprovement(qualityImprovement);

    return qualityImprovementCreateUpdateResult;
  }
}
