package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class TrackingNumberConfigurationServiceImpl implements TrackingNumberConfigurationService {

  protected SequenceService sequenceService;
  protected BarcodeGeneratorService barcodeGeneratorService;
  protected AppBaseService appBaseService;

  @Inject
  public TrackingNumberConfigurationServiceImpl(
      SequenceService sequenceService,
      BarcodeGeneratorService barcodeGeneratorService,
      AppBaseService appBaseService) {
    this.sequenceService = sequenceService;
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.appBaseService = appBaseService;
  }

  @Override
  public boolean checkSequenceAndBarcodeTypeConfigConsistency(TrackingNumberConfiguration config)
      throws AxelorException {
    if (config.getBarcodeTypeConfig() != null
        && config.getSequence() != null
        && config.getUseTrackingNumberSeqAsSerialNbr()) {
      Sequence sequence = config.getSequence();
      String testSeq =
          sequenceService.computeTestSeq(
              sequence, appBaseService.getTodayDate(sequence.getCompany()));
      return barcodeGeneratorService.checkSerialNumberConsistency(
          testSeq, config.getBarcodeTypeConfig(), false);
    }
    return false;
  }
}
