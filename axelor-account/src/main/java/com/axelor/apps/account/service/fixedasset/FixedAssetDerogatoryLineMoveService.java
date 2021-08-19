package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface FixedAssetDerogatoryLineMoveService {

  /** @param fixedAssetLine */
  void realize(FixedAssetDerogatoryLine fixedAssetLine) throws AxelorException;

  void generateMove(
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine,
      Account creditLineAccount,
      Account debitLineAccount,
      BigDecimal amount)
      throws AxelorException;
}
