package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequestScoped
public class FixedAssetLineFiscalComputationServiceImpl
    extends AbstractFixedAssetLineComputationServiceImpl {

  @Inject
  public FixedAssetLineFiscalComputationServiceImpl(
      FixedAssetFailOverControlService fixedAssetFailOverControlService) {
    super(fixedAssetFailOverControlService);
  }

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      return fixedAsset.getFailoverDate();
    }
    return fixedAsset.getFirstDepreciationDate();
  }

  @Override
  protected BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset) {
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      return fixedAsset.getGrossValue().subtract(fixedAsset.getAlreadyDepreciatedAmount());
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
    return fixedAsset.getFiscalFixedAssetLineList();
  }

  @Override
  protected Integer getNumberOfDepreciation(FixedAsset fixedAsset) {
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      return fixedAsset.getFiscalNumberOfDepreciation() - fixedAsset.getNbrOfPastDepreciations();
    }
    return fixedAsset.getFiscalNumberOfDepreciation();
  }

  @Override
  protected String getComputationMethodSelect(FixedAsset fixedAsset) {
    return fixedAsset.getFiscalComputationMethodSelect();
  }

  @Override
  protected BigDecimal getDegressiveCoef(FixedAsset fixedAsset) {
    return fixedAsset.getFiscalDegressiveCoef();
  }

  @Override
  protected Integer getPeriodicityInMonth(FixedAsset fixedAsset) {
    return fixedAsset.getFiscalPeriodicityInMonth();
  }

  @Override
  protected Integer getTypeSelect() {

    return FixedAssetLineRepository.TYPE_SELECT_FISCAL;
  }
}
