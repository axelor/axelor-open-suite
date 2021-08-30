package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface FixedAssetDerogatoryLineMoveService {

  /** @param fixedAssetLine */
  void realize(FixedAssetDerogatoryLine fixedAssetLine, boolean isBatch, boolean generateMove)
      throws AxelorException;

  Move generateMove(
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine,
      Account creditLineAccount,
      Account debitLineAccount,
      BigDecimal amount)
      throws AxelorException;
}
