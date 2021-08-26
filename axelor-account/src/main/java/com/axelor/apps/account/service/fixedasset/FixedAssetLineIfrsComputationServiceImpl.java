package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FixedAssetLineIfrsComputationServiceImpl
    extends AbstractFixedAssetLineComputationServiceImpl {

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    return fixedAsset.getFirstDepreciationDate();
  }

  @Override
  protected BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsIfrsEqualToFiscalDepreciation()
        && fixedAsset
            .getIfrsComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)) {
      return fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue());
    }
    return fixedAsset.getGrossValue();
  }

  @Override
  protected LocalDate computeProrataTemporisFirstDepreciationDate(FixedAsset fixedAsset) {
    return fixedAsset.getFirstDepreciationDate();
  }

  @Override
  protected LocalDate computeProrataTemporisAcquisitionDate(FixedAsset fixedAsset) {
    return fixedAsset.getAcquisitionDate();
  }

  @Override
  protected List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsFixedAssetLineList();
  }

  @Override
  protected Integer getNumberOfDepreciation(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsNumberOfDepreciation();
  }

  @Override
  protected String getComputationMethodSelect(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsComputationMethodSelect();
  }

  @Override
  protected BigDecimal getDegressiveCoef(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsDegressiveCoef();
  }

  @Override
  protected Integer getPeriodicityInMonth(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsPeriodicityInMonth();
  }

  @Override
  protected Integer getTypeSelect() {

    return FixedAssetLineRepository.TYPE_SELECT_IFRS;
  }
}
