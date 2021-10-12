package com.axelor.apps.account.web;

import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.apps.account.db.repo.FixedAssetTypeRepository;
import com.axelor.common.StringUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class FixedAssetCategoryController {
  public void setDepreciationPlanSelect(ActionRequest request, ActionResponse response) {
    try {
      FixedAssetCategory fixedAssetCategory = request.getContext().asType(FixedAssetCategory.class);
      if (fixedAssetCategory.getFixedAssetType() != null
          && fixedAssetCategory.getFixedAssetType().getTechnicalTypeSelect()
              == FixedAssetTypeRepository
                  .FIXED_ASSET_CATEGORY_TECHNICAL_TYPE_SELECT_ONGOING_ASSET) {
        fixedAssetCategory.setDepreciationPlanSelect(
            FixedAssetCategoryRepository.DEPRECIATION_PLAN_SELECT_NONE);
      }
      if (StringUtils.notEmpty(fixedAssetCategory.getDepreciationPlanSelect())) {
        response.setValue("depreciationPlanSelect", fixedAssetCategory.getDepreciationPlanSelect());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
