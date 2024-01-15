/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.studio.db.AppStock;
import com.google.inject.Inject;

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
