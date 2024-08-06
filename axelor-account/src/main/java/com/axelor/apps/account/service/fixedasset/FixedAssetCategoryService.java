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
import com.axelor.apps.account.db.FixedAssetCategory;

public interface FixedAssetCategoryService {

  boolean compareFixedAssetCategoryTypeSelect(
      FixedAsset fixedAsset, int fixedAssetCategoryTechnicalTypeSelectOngoingAsset);

  public boolean compareFixedAssetCategoryTypeSelect(
      FixedAssetCategory fixedAssetCategory, int typeSelect);

  public void setDepreciationPlanSelectToNone(FixedAsset fixedAsset, int typeSelect);

  void setDepreciationPlanSelectToNone(
      FixedAssetCategory fixedAssetCategory, int fixedAssetCategoryTechnicalTypeSelectOngoingAsset);
}
