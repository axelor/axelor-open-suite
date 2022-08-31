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
