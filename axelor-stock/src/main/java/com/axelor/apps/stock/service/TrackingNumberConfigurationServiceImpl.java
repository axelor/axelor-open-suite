package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.AppStock;
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.module.StockModule;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.exception.AxelorException;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

@Alternative
@Priority(StockModule.PRIORITY)
public class TrackingNumberConfigurationServiceImpl implements TrackingNumberConfigurationService {

  protected SequenceService sequenceService;
  protected BarcodeGeneratorService barcodeGeneratorService;
  protected AppBaseService appBaseService;
  protected AppStockService appStockService;

  @Inject
  public TrackingNumberConfigurationServiceImpl(
      SequenceService sequenceService,
      BarcodeGeneratorService barcodeGeneratorService,
      AppBaseService appBaseService,
      AppStockService appStockService) {
    this.sequenceService = sequenceService;
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.appBaseService = appBaseService;
    this.appStockService = appStockService;
  }

  @Override
  public boolean checkSequenceAndBarcodeTypeConfigConsistency(TrackingNumberConfiguration config)
      throws AxelorException {
    AppStock appStock = appStockService.getAppStock();
    if (appStock != null
        && appStock.getActivateTrackingNumberBarCodeGeneration()
        && config != null
        && config.getSequence() != null
        && config.getUseTrackingNumberSeqAsSerialNbr()) {
      Sequence sequence = config.getSequence();
      BarcodeTypeConfig barcodeTypeConfig = config.getBarcodeTypeConfig();
      if (!appStock.getEditTrackingNumberBarcodeType()) {
        barcodeTypeConfig = appStock.getTrackingNumberBarcodeTypeConfig();
      }
      String testSeq =
          sequenceService.computeTestSeq(
              sequence, appBaseService.getTodayDate(sequence.getCompany()));
      return barcodeGeneratorService.checkSerialNumberConsistency(
          testSeq, barcodeTypeConfig, false);
    }
    return false;
  }
}
