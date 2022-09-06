package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FixedAssetLineIfrsComputationServiceImpl
    extends AbstractFixedAssetLineComputationServiceImpl {

  @Inject
  public FixedAssetLineIfrsComputationServiceImpl(
      FixedAssetFailOverControlService fixedAssetFailOverControlService,
      AppBaseService appBaseService) {
    super(fixedAssetFailOverControlService, appBaseService);
  }

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      return fixedAsset.getFailoverDate();
    }
    return fixedAsset.getIfrsFirstDepreciationDate();
  }

  @Override
  protected BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsIfrsEqualToFiscalDepreciation()
        && fixedAsset
            .getIfrsComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)) {
      return fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue());
    }

    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)
        && getComputationMethodSelect(fixedAsset)
            .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      return fixedAsset.getGrossValue().subtract(getAlreadyDepreciatedAmount(fixedAsset));
    }
    return fixedAsset.getGrossValue();
  }

  @Override
  protected LocalDate computeProrataTemporisFirstDepreciationDate(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsFirstDepreciationDate();
  }

  @Override
  protected List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsFixedAssetLineList();
  }

  @Override
  protected BigDecimal getNumberOfDepreciation(FixedAsset fixedAsset) {
    return BigDecimal.valueOf(fixedAsset.getIfrsNumberOfDepreciation());
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

  @Override
  protected Boolean isProrataTemporis(FixedAsset fixedAsset) {
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      // This case means that prorata temporis was already computed in another software.
      return false;
    }
    return fixedAsset.getFixedAssetCategory().getIsProrataTemporis();
  }

  @Override
  protected BigDecimal computeInitialDegressiveDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue) {
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      FixedAssetLine dummyPreviousLine = new FixedAssetLine();
      dummyPreviousLine.setAccountingValue(baseValue);
      return super.computeOnGoingDegressiveDepreciation(fixedAsset, dummyPreviousLine);
    }
    return super.computeInitialDegressiveDepreciation(fixedAsset, baseValue);
  }

  @Override
  protected BigDecimal getNumberOfPastDepreciation(FixedAsset fixedAsset) {
    return BigDecimal.valueOf(fixedAsset.getIfrsNbrOfPastDepreciations());
  }

  @Override
  protected BigDecimal getAlreadyDepreciatedAmount(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsAlreadyDepreciatedAmount();
  }

  @Override
  protected Integer getDurationInMonth(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsDurationInMonth();
  }

  @Override
  protected BigDecimal getDepreciatedAmountCurrentYear(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsDepreciatedAmountCurrentYear();
  }

  @Override
  protected LocalDate getFailOverDepreciationEndDate(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsFailOverDepreciationEndDate();
  }

  @Override
  protected int getFirstDateDepreciationInitSelect(FixedAsset fixedAsset) {
    return fixedAsset.getIfrsFirstDepreciationDateInitSelect();
  }
}
