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
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

public interface FixedAssetLineToolService {

  /**
   * This method group and sort {@link FixedAsset#getFixedAssetLineList()} and {@link
   * FixedAsset#getFiscalFixedAssetLineList()} by period of [month multiplied by periodicityInMonth]
   * in {@link FixedAssetLine#getDepreciationDate()}. Because it sorted, the method will explicitly
   * return a {@link LinkedHashMap}.
   *
   * @param fixedAsset
   * @return generated {@link LinkedHashMap}
   */
  LinkedHashMap<LocalDate, List<FixedAssetLine>> groupAndSortByDateFixedAssetLine(
      FixedAsset fixedAsset);

  BigDecimal getCompanyScaledValue(
      BigDecimal amount1,
      BigDecimal amount2,
      FixedAsset fixedAsset,
      ArithmeticOperation arithmeticOperation);

  BigDecimal getCompanyScaledValue(BigDecimal amount1, FixedAsset fixedAsset);

  BigDecimal getCompanyScaledValue(
      BigDecimal amount1,
      BigDecimal amount2,
      FixedAssetLine fixedAssetLine,
      ArithmeticOperation arithmeticOperation)
      throws AxelorException;

  BigDecimal getCompanyScaledValue(BigDecimal amount1, FixedAssetLine fixedAssetLine)
      throws AxelorException;

  BigDecimal getCompanyDivideScaledValue(
      BigDecimal amount1, BigDecimal amount2, FixedAsset fixedAsset);

  boolean isGreaterThan(BigDecimal amount1, BigDecimal amount2, FixedAsset fixedAsset);

  boolean equals(BigDecimal amount1, BigDecimal amount2, FixedAsset fixedAsset);

  int getCompanyScale(FixedAssetLine fixedAssetLine) throws AxelorException;

  @FunctionalInterface
  interface ArithmeticOperation {

    BigDecimal operate(BigDecimal a, BigDecimal b);
  }
}
