/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
      Boolean isSimulated,
      Boolean isDisposal)
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
