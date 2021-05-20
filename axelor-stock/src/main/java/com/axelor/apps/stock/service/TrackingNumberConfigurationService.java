package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.exception.AxelorException;

public interface TrackingNumberConfigurationService {

  /**
   * Check consistency between sequence and barcode type config
   *
   * @param config the tracking number configuration config
   * @return true if it is consistent
   */
  public boolean checkSequenceAndBarcodeTypeConfigConsistency(TrackingNumberConfiguration config)
      throws AxelorException;
}
