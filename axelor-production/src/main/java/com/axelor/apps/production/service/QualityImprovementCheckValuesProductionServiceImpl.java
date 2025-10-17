package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.service.QualityImprovementCheckValuesServiceImpl;
import com.axelor.i18n.I18n;

public class QualityImprovementCheckValuesProductionServiceImpl
    extends QualityImprovementCheckValuesServiceImpl {

  @Override
  protected void checkFieldsByType(int type, QIIdentification qiIdentification)
      throws AxelorException {
    if (type == 2
        && (qiIdentification.getProduct() != null || qiIdentification.getManufOrder() != null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.API_TYPE_SYSTEM_PRODUCT_MANUF_ORDER_FILLED));
    }
  }
}
