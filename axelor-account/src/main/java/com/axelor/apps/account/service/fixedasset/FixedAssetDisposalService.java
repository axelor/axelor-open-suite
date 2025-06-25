package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.AssetDisposalReason;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface FixedAssetDisposalService {

  List<FixedAsset> processDisposal(
      FixedAsset fixedAsset,
      List<FixedAsset> fixedAssetList,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException;

  FixedAsset fullDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException;
}
