/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
