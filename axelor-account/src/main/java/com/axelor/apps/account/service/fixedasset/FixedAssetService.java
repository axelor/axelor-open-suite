/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FixedAssetService {

  void updateAnalytic(FixedAsset fixedAsset) throws AxelorException;

  void updateDepreciation(FixedAsset fixedAsset) throws AxelorException;

  List<FixedAsset> splitFixedAsset(
      FixedAsset fixedAsset,
      int splitType,
      BigDecimal amount,
      LocalDate splittingDate,
      String comments)
      throws AxelorException;

  /**
   * Call splitFixedAsset and save both fixed asset. (Original and all created)
   *
   * @param fixedAsset
   * @param splitType
   * @param amount
   * @param splittingDate
   * @param comments
   * @return
   * @throws AxelorException
   */
  List<FixedAsset> splitAndSaveFixedAsset(
      FixedAsset fixedAsset,
      int splitType,
      BigDecimal amount,
      LocalDate splittingDate,
      String comments)
      throws AxelorException;

  void checkFixedAssetBeforeSplit(FixedAsset fixedAsset, int splitType, BigDecimal amount)
      throws AxelorException;

  void onChangeDepreciationPlan(FixedAsset fixedAsset) throws AxelorException;

  public boolean checkDepreciationPlans(FixedAsset fixedAsset);
}
