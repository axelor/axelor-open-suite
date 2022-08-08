package com.axelor.csv.script;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class ImportFixedAsset {

  @Inject FixedAssetGenerationService fixedAssetGenerationService;

  public Object importFixedAsset(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof FixedAsset;
    FixedAsset fixedAsset = (FixedAsset) bean;
    if (fixedAsset != null
        && (fixedAsset.getOriginSelect() == null || fixedAsset.getOriginSelect() == 0)) {
      fixedAsset.setOriginSelect(FixedAssetRepository.ORIGINAL_SELECT_IMPORT);
    }
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }
}
