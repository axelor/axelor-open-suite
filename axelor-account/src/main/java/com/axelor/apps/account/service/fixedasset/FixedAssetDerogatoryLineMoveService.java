package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Batch;
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
      BigDecimal amount,
      Boolean isSimulated)
      throws AxelorException;

  void simulate(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) throws AxelorException;

  boolean canSimulate(FixedAssetDerogatoryLine fixedAssetLine) throws AxelorException;

  /**
   * When calling service from a batch, it might be necessary to set batch to attach generated moves
   * to batch
   *
   * @param batch
   */
  void setBatch(Batch batch);
}
