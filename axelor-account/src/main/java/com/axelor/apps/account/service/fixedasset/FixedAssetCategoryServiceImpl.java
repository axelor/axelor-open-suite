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
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.common.ObjectUtils;

public class FixedAssetCategoryServiceImpl implements FixedAssetCategoryService {

  @Override
  public boolean compareFixedAssetCategoryTypeSelect(FixedAsset fixedAsset, int typeSelect) {
    boolean result = false;
    if (ObjectUtils.notEmpty(fixedAsset.getFixedAssetCategory())) {
      result = compareFixedAssetCategoryTypeSelect(fixedAsset.getFixedAssetCategory(), typeSelect);
    }
    return result;
  }

  @Override
  public boolean compareFixedAssetCategoryTypeSelect(
      FixedAssetCategory fixedAssetCategory, int typeSelect) {

    boolean result = false;
    if (ObjectUtils.notEmpty(fixedAssetCategory.getFixedAssetType())) {
      result = fixedAssetCategory.getFixedAssetType().getTechnicalTypeSelect() == typeSelect;
    }
    return result;
  }

  @Override
  public void setDepreciationPlanSelectToNone(FixedAsset fixedAsset, int typeSelect) {
    if (fixedAsset.getFixedAssetCategory() != null) {
      setDepreciationPlanSelectToNone(fixedAsset.getFixedAssetCategory(), typeSelect);
    }
  }

  @Override
  public void setDepreciationPlanSelectToNone(
      FixedAssetCategory fixedAssetCategory, int typeSelect) {
    if (fixedAssetCategory.getFixedAssetType() != null
        && fixedAssetCategory.getFixedAssetType().getTechnicalTypeSelect() == typeSelect) {
      fixedAssetCategory.setDepreciationPlanSelect(
          FixedAssetCategoryRepository.DEPRECIATION_PLAN_SELECT_NONE);
    }
  }
}
