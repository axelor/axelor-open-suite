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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface FixedAssetLineMoveService {

  void realize(
      FixedAssetLine fixedAssetLine, boolean isBatch, boolean generateMove, boolean isDisposal)
      throws AxelorException;

  /**
   * This method will generate a disposal move on fixedAsset at disposalDate. The move will have 2
   * or 3 move lines, depending on value of fixedAssetLine. (it can be null)
   *
   * @param fixedAsset
   * @param fixedAssetLine
   * @param transferredReason
   * @param disposalDate
   * @throws AxelorException
   */
  void generateDisposalMove(
      FixedAsset fixedAsset,
      FixedAssetLine fixedAssetLine,
      int transferredReason,
      LocalDate disposalDate)
      throws AxelorException;

  /**
   * Method that may computes action "realize" on lines of fiscalFixedAssetLineList,
   * fixedAssetLineList and fixedAssetDerogatoryLineList that matches the same depreciation date. It
   * will compute depending on the fixedAsset.depreciationPlanSelect
   *
   * @param fixedAsset
   * @param depreciationDate
   * @throws AxelorException
   */
  void realizeOthersLines(
      FixedAsset fixedAsset, LocalDate depreciationDate, boolean isBatch, boolean generateMove)
      throws AxelorException;

  void generateSaleMove(
      FixedAsset fixedAsset, TaxLine taxLine, BigDecimal disposalAmount, LocalDate disposalDate)
      throws AxelorException;

  /**
   * Method that only create a move on fixed asset line.
   *
   * @param fixedAssetLine
   * @throws AxelorException
   */
  void simulate(FixedAssetLine fixedAssetLine) throws AxelorException;

  /**
   * Method that only create a move all move lines of fixed asset that matches depreciationDate.
   *
   * @param fixedAssetLine
   * @throws AxelorException
   */
  void simulateOthersLine(FixedAsset fixedAsset, LocalDate depreciationDate) throws AxelorException;

  /**
   * Method that checks if fixedAssetLine can be simulated or not.
   *
   * @param fixedAssetLine
   * @return true if it can be simulated. false otherwise
   * @throws AxelorException
   */
  boolean canSimulate(FixedAssetLine fixedAssetLine) throws AxelorException;

  /**
   * When calling service from a batch, it might be necessary to set batch to attach generated moves
   * to batch
   *
   * @param batch
   */
  void setBatch(Batch batch);

  Move generateMove(FixedAssetLine fixedAssetLine, boolean isSimulated, boolean isDisposal)
      throws AxelorException;
}
