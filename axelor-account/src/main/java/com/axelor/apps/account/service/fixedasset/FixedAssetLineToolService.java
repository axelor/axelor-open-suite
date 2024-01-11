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
   * @throws NullPointerException if fixedAsset is null
   */
  LinkedHashMap<LocalDate, List<FixedAssetLine>> groupAndSortByDateFixedAssetLine(
      FixedAsset fixedAsset);
}
