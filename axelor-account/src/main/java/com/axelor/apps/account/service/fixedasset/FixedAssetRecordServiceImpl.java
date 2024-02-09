package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class FixedAssetRecordServiceImpl implements FixedAssetRecordService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public FixedAssetRecordServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void resetAssetDisposalReason(FixedAsset fixedAsset) {
    fixedAsset.setAssetDisposalReason(null);
  }

  @Override
  public void setDisposalQtySelect(FixedAsset fixedAsset, int disposalTypeSelect) {
    if (disposalTypeSelect != FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION) {
      fixedAsset.setDisposalQtySelect(FixedAssetRepository.DISPOSABLE_QTY_SELECT_TOTAL);
    }
  }

  @Override
  public BigDecimal setDisposalAmount(FixedAsset fixedAsset, int disposalTypeSelect) {
    BigDecimal disposalAmount;

    switch (disposalTypeSelect) {
      case FixedAssetRepository.DISPOSABLE_TYPE_SELECT_SCRAPPING:
      case FixedAssetRepository.DISPOSABLE_TYPE_SELECT_ONGOING_CESSION:
        disposalAmount = fixedAsset.getAccountingValue();
        break;
      case FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION:
        disposalAmount = fixedAsset.getResidualValue();
        break;
      default:
        disposalAmount = BigDecimal.ZERO;
    }

    return currencyScaleService.getCompanyScaledValue(fixedAsset, disposalAmount);
  }
}
