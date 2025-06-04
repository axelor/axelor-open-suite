package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QIDetectionRepository;
import com.axelor.apps.quality.rest.dto.QualityImprovementRequest;
import com.axelor.apps.quality.service.QualityImprovementCreateService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
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
  public QualityImprovement createQualityImprovement(
      QualityImprovementRequest qualityImprovementRequest) throws AxelorException {

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

    qualityImprovementCreateService.createQualityImprovement(
        qualityImprovement, qiIdentification, qiResolution);
    return qualityImprovement;
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

    if (qiDetectionOrigin == QIDetectionRepository.ORIGIN_SUPPLIER
        && customerSaleOrderLine != null) {
      qiIdentification.setQuantity(customerSaleOrderLine.getQty());
    } else if (qiDetectionOrigin == QIDetectionRepository.ORIGIN_CUSTOMER
        && supplierPurchaseOrderLine != null) {
      qiIdentification.setQuantity(supplierPurchaseOrderLine.getQty());
    } else if (qiDetectionOrigin == QIDetectionRepository.ORIGIN_INTERNAL) {
      qiIdentification.setDetectedByInternal(AuthUtils.getUser().getPartner());
    }
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
