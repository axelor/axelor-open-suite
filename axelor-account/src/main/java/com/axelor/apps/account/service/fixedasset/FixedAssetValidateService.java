package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface FixedAssetValidateService {

  /**
   * Validate fixedAsset
   *
   * @param fixedAsset
   * @throws AxelorException
   */
  void validate(FixedAsset fixedAsset) throws AxelorException;

  int massValidation(List<Long> fixedAssetIds) throws AxelorException;
}
