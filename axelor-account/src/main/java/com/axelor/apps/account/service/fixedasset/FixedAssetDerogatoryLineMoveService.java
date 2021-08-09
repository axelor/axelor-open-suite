package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.exception.AxelorException;

public interface FixedAssetDerogatoryLineMoveService {

  /** @param fixedAssetLine */
  void realize(FixedAssetDerogatoryLine fixedAssetLine) throws AxelorException;
}
