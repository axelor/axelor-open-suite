/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

/** This service is used to compute new lines from an existing fixed asset header. */
public interface FixedAssetLineComputationService {

  /**
   * Compute the first fixed asset line from an empty fixed asset.
   *
   * @param fixedAsset a fixed asset with no lines
   * @return the created fixed asset line
   */
  FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset);

  /**
   * Compute the next fixed asset line from a fixed asset and the previous line.
   *
   * @param fixedAsset a fixed asset with existing lines
   * @param previousFixedAssetLine the previous line
   * @return the created fixed asset line
   */
  FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine);
}
