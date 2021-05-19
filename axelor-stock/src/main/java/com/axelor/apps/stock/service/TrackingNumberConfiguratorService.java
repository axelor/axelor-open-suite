package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class TrackingNumberConfiguratorService {

  @Inject private SequenceService sequenceService;
  @Inject private BarcodeGeneratorService barcodeGeneratorService;

  public boolean checkSequenceAndBarcodeTypeConfigConsistency(TrackingNumberConfiguration config)
      throws AxelorException {
    if (config.getBarcodeTypeConfig() != null
        && config.getSequence() != null
        && config.getUseTrackingNumberSeqAsSerialNbr()) {
      Sequence sequence = config.getSequence();
      String testSeq =
          sequenceService.computeTestSeq(
              sequence, Beans.get(AppBaseService.class).getTodayDate(sequence.getCompany()));
      return barcodeGeneratorService.checkSerialNumberConsistency(
          testSeq, config.getBarcodeTypeConfig(), false);
    }
    return false;
  }
}
