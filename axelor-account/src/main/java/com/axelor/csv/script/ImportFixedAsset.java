package com.axelor.csv.script;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class ImportFixedAsset {

  @Inject FixedAssetGenerationService fixedAssetGenerationService;

  public Object importFixedAsset(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof FixedAsset;
    FixedAsset fixedAsset = (FixedAsset) bean;
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }
}
