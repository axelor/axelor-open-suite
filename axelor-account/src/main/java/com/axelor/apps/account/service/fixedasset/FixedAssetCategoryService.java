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
