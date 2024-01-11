package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import java.math.BigDecimal;

public interface FixedAssetRecordService {

  void resetAssetDisposalReason(FixedAsset fixedAsset);

  void setDisposalQtySelect(FixedAsset fixedAsset, int disposalTypeSelect);

  BigDecimal setDisposalAmount(FixedAsset fixedAsset, int disposalTypeSelect);
}
