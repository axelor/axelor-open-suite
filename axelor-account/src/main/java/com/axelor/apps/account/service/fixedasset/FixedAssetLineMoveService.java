/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface FixedAssetLineMoveService {

  void realize(FixedAssetLine fixedAssetLine, boolean isBatch) throws AxelorException;

  void generateDisposalMove(FixedAssetLine fixedAssetLine) throws AxelorException;

  /**
   * Method that may computes action "realize" on lines of fiscalFixedAssetLineList,
   * fixedAssetLineList and fixedAssetDerogatoryLineList that matches the same depreciation date. It
   * will compute depending on the fixedAsset.depreciationPlanSelect
   *
   * @param fixedAsset
   * @param depreciationDate
   * @throws AxelorException
   */
  void realizeOthersLines(FixedAsset fixedAsset, LocalDate depreciationDate, boolean isBatch)
      throws AxelorException;
}
