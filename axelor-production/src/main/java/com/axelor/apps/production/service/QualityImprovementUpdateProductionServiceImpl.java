package com.axelor.apps.production.service;

import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.quality.service.QualityImprovementCheckValuesService;
import com.axelor.apps.quality.service.QualityImprovementUpdateServiceImpl;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;

public class QualityImprovementUpdateProductionServiceImpl
    extends QualityImprovementUpdateServiceImpl {

  @Inject
  public QualityImprovementUpdateProductionServiceImpl(
      QualityImprovementRepository qualityImprovementRepository,
      QualityImprovementCheckValuesService qualityImprovementCheckValuesService,
      MetaFiles metaFiles) {
    super(qualityImprovementRepository, qualityImprovementCheckValuesService, metaFiles);
  }

  @Override
  protected void updateQIIdentification(
      QIIdentification baseQiIdentification, QIIdentification newQiIdentification) {
    super.updateQIIdentification(baseQiIdentification, newQiIdentification);
    baseQiIdentification.setManufOrder(newQiIdentification.getManufOrder());
    baseQiIdentification.setOperationOrder(newQiIdentification.getOperationOrder());
  }
}
