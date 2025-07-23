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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QIDetectionRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.rest.dto.QualityImprovementCreateUpdateResult;
import com.axelor.apps.quality.rest.dto.QualityImprovementPostRequest;
import com.axelor.apps.quality.service.QualityImprovementCreateService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class QualityImprovementCreateAPIServiceImpl implements QualityImprovementCreateAPIService {

  protected QualityImprovementParseService qualityImprovementParseService;
  protected QualityImprovementCreateService qualityImprovementCreateService;
  protected AppBaseService appBaseService;

  @Inject
  public QualityImprovementCreateAPIServiceImpl(
      QualityImprovementParseService qualityImprovementParseService,
      QualityImprovementCreateService qualityImprovementCreateService,
      AppBaseService appBaseService) {
    this.qualityImprovementParseService = qualityImprovementParseService;
    this.qualityImprovementCreateService = qualityImprovementCreateService;
    this.appBaseService = appBaseService;
  }

  /**
   * Create QI from request
   *
   * @param qualityImprovementRequest
   * @return
   * @throws AxelorException
   */
  @Override
  public QualityImprovementCreateUpdateResult createQualityImprovement(
      QualityImprovementPostRequest qualityImprovementRequest) throws AxelorException {
    QualityImprovementCreateUpdateResult qualityImprovementCreateUpdateResult =
        new QualityImprovementCreateUpdateResult();

    QualityImprovement qualityImprovement =
        qualityImprovementParseService.getQualityImprovementFromRequestBody(
            qualityImprovementRequest);

    QIIdentification qiIdentification =
        qualityImprovementParseService.getQiIdentificationFromRequestBody(
            qualityImprovementRequest.getQiIdentification(), qualityImprovement.getQiDetection());
    fillQIIdentificationDefaultValues(qiIdentification, qualityImprovement.getQiDetection());

    QIResolution qiResolution =
        qualityImprovementParseService.getQiResolutionFromRequestBody(
            qualityImprovementRequest.getQiResolution());
    fillQIResolutionDefaultValues(qiResolution);

    int errors =
        qualityImprovementParseService.filterQIResolutionDefaultValues(
            qiResolution, qualityImprovement.getType());

    if (errors > 0) {
      String errorMessage =
          String.format(I18n.get(QualityExceptionMessage.API_QI_RESOLUTION_DEFAULT_ERROR), errors);
      qualityImprovementCreateUpdateResult.setErrorMessage(errorMessage);
    }

    qualityImprovementCreateService.createQualityImprovement(
        qualityImprovement, qiIdentification, qiResolution);

    qualityImprovementCreateUpdateResult.setQualityImprovement(qualityImprovement);

    return qualityImprovementCreateUpdateResult;
  }

  /**
   * fill QIIdentification default values at creation
   *
   * @param qiIdentification
   * @param qiDetection
   */
  protected void fillQIIdentificationDefaultValues(
      QIIdentification qiIdentification, QIDetection qiDetection) {
    User user = AuthUtils.getUser();
    qiIdentification.setWrittenBy(user);
    qiIdentification.setWrittenOn(
        appBaseService.getTodayDateTime(user.getActiveCompany()).toLocalDateTime());

    Integer qiDetectionOrigin = qiDetection.getOrigin();
    SaleOrderLine customerSaleOrderLine = qiIdentification.getCustomerSaleOrderLine();
    PurchaseOrderLine supplierPurchaseOrderLine = qiIdentification.getSupplierPurchaseOrderLine();

    if (qiDetectionOrigin == QIDetectionRepository.ORIGIN_CUSTOMER
        && customerSaleOrderLine != null) {
      qiIdentification.setQuantity(customerSaleOrderLine.getQty());
    } else if (qiDetectionOrigin == QIDetectionRepository.ORIGIN_SUPPLIER
        && supplierPurchaseOrderLine != null) {
      qiIdentification.setQuantity(supplierPurchaseOrderLine.getQty());
    }

    qiIdentification.setDetectedByInternal(AuthUtils.getUser().getPartner());
  }

  /**
   * fill QIResolution default values at creation
   *
   * @param qiResolution
   */
  protected void fillQIResolutionDefaultValues(QIResolution qiResolution) {
    User user = AuthUtils.getUser();
    qiResolution.setDefaultWrittenBy(user);
    qiResolution.setDefaultWrittenOn(
        appBaseService.getTodayDateTime(user.getActiveCompany()).toLocalDateTime());
  }
}
