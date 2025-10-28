package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.rest.dto.QIIdentificationRequest;
import com.axelor.apps.quality.rest.service.QualityImprovementParseServiceImpl;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.utils.api.ObjectFinder;
import com.google.inject.Inject;

public class QualityImprovementParseProductionServiceImpl
    extends QualityImprovementParseServiceImpl {

  @Inject
  public QualityImprovementParseProductionServiceImpl(MetaFileRepository metaFileRepository) {
    super(metaFileRepository);
  }

  @Override
  public QIIdentification getQiIdentificationFromRequestBody(
      QIIdentificationRequest qiIdentificationRequest, QIDetection qiDetection) {
    QIIdentification qiIdentification =
        super.getQiIdentificationFromRequestBody(qiIdentificationRequest, qiDetection);
    qiIdentification.setManufOrder(fetchManufOrder(qiIdentificationRequest));
    qiIdentification.setOperationOrder(fetchOperationOrder(qiIdentificationRequest));
    return qiIdentification;
  }

  protected ManufOrder fetchManufOrder(QIIdentificationRequest qiIdentificationRequest) {
    Long manufOrderId = qiIdentificationRequest.getManufOrderId();
    if (manufOrderId == null || manufOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(ManufOrder.class, manufOrderId, ObjectFinder.NO_VERSION);
  }

  protected OperationOrder fetchOperationOrder(QIIdentificationRequest qiIdentificationRequest) {
    Long operationOrderId = qiIdentificationRequest.getOperationOrderId();
    if (operationOrderId == null || operationOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(OperationOrder.class, operationOrderId, ObjectFinder.NO_VERSION);
  }
}
