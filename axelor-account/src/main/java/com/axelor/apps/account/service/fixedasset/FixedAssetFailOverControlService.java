package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.exception.AxelorException;

public interface FixedAssetFailOverControlService {

  /**
   * Method that controls values's consistency when on failOver.
   *
   * @param fixedAsset
   */
  void controlFailOver(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Method that return true if fixedAsset is a failOver
   *
   * @param fixedAsset
   * @return
   */
  boolean isFailOver(FixedAsset fixedAsset);
}
