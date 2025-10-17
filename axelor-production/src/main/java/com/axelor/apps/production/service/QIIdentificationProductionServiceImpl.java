package com.axelor.apps.production.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.service.QIIdentificationServiceImpl;
import com.google.inject.Inject;

public class QIIdentificationProductionServiceImpl extends QIIdentificationServiceImpl {

  @Inject
  public QIIdentificationProductionServiceImpl(AppBaseService appBaseService) {
    super(appBaseService);
  }

  @Override
  protected boolean requiresIdentificationUpdate(QIIdentification qiIdentification) {
    return super.requiresIdentificationUpdate(qiIdentification)
        || qiIdentification.getManufOrder() != null
        || qiIdentification.getOperationOrder() != null
        || qiIdentification.getToConsumeProdProduct() != null
        || qiIdentification.getConsumedProdProduct() != null;
  }
}
