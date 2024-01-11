package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.service.fixedasset.attributes.FixedAssetAttrsService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class FixedAssetGroupServiceImpl implements FixedAssetGroupService {

  protected FixedAssetAttrsService fixedAssetAttrsService;
  protected FixedAssetRecordService fixedAssetRecordService;

  @Inject
  public FixedAssetGroupServiceImpl(
      FixedAssetAttrsService fixedAssetAttrsService,
      FixedAssetRecordService fixedAssetRecordService) {
    this.fixedAssetAttrsService = fixedAssetAttrsService;
    this.fixedAssetRecordService = fixedAssetRecordService;
  }

  @Override
  public Map<String, Object> getDisposalWizardValuesMap(
      FixedAsset fixedAsset, int disposalTypeSelect) {
    fixedAssetRecordService.resetAssetDisposalReason(fixedAsset);
    fixedAssetRecordService.setDisposalQtySelect(fixedAsset, disposalTypeSelect);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put(
        "disposalAmount",
        fixedAssetRecordService.setDisposalAmount(fixedAsset, disposalTypeSelect));
    valuesMap.put("assetDisposalReason", fixedAsset.getAssetDisposalReason());
    valuesMap.put("disposalQtySelect", fixedAsset.getDisposalQtySelect());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getDisposalWizardAttrsMap(
      int disposalTypeSelect, FixedAsset fixedAsset) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    fixedAssetAttrsService.addDisposalAmountTitle(disposalTypeSelect, attrsMap);
    fixedAssetAttrsService.addDisposalAmountReadonly(disposalTypeSelect, attrsMap);
    fixedAssetAttrsService.addDisposalAmountScale(fixedAsset, attrsMap);

    return attrsMap;
  }
}
