package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfigurationProfile;

public interface TrackingNumberConfigurationProfileService {
  void calculateDimension(
      TrackingNumber trackingNumber,
      TrackingNumberConfigurationProfile trackingNumberConfigurationProfile)
      throws AxelorException;

  void setDefaultsFieldFormula(TrackingNumberConfigurationProfile profile);
}
