package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class FixedAssetFailOverControlServiceImpl implements FixedAssetFailOverControlService {

  /**
   * {@inheritDoc}
   *
   * @throws AxelorException
   */
  @Override
  public void controlFailOver(FixedAsset fixedAsset) throws AxelorException {
    Objects.requireNonNull(fixedAsset);
    FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
    if (isFailOver(fixedAsset) && fixedAssetCategory != null) {
      if (fixedAsset.getAlreadyDepreciatedAmount() != null
          && fixedAsset.getAlreadyDepreciatedAmount().compareTo(fixedAsset.getGrossValue()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                AccountExceptionMessage
                    .IMMO_FIXED_ASSET_FAILOVER_CONTROL_PAST_DEPRECIATION_GREATER_THAN_GROSS_VALUE));
      }
      if (fixedAsset.getFiscalAlreadyDepreciatedAmount() != null
          && fixedAsset.getFiscalAlreadyDepreciatedAmount().compareTo(fixedAsset.getGrossValue())
              > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                AccountExceptionMessage
                    .IMMO_FIXED_ASSET_FAILOVER_CONTROL_PAST_DEPRECIATION_GREATER_THAN_GROSS_VALUE));
      }
      if (fixedAsset.getIfrsAlreadyDepreciatedAmount() != null
          && fixedAsset.getIfrsAlreadyDepreciatedAmount().compareTo(fixedAsset.getGrossValue())
              > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                AccountExceptionMessage
                    .IMMO_FIXED_ASSET_FAILOVER_CONTROL_PAST_DEPRECIATION_GREATER_THAN_GROSS_VALUE));
      }
      String depreciationPlanSelect = fixedAsset.getDepreciationPlanSelect();
      if (fixedAsset.getFailoverDate() != null && depreciationPlanSelect != null) {

        if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)) {
          LocalDate dateToCheck =
              fixedAsset.getFirstDepreciationDateInitSelect()
                          == FixedAssetCategoryRepository
                              .REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                      && fixedAsset.getFirstServiceDate() != null
                  ? fixedAsset.getFirstServiceDate()
                  : fixedAsset.getAcquisitionDate();
          checkFailOverDate(
              fixedAsset.getPeriodicityTypeSelect(),
              dateToCheck,
              fixedAsset.getFailoverDate(),
              fixedAsset.getNumberOfDepreciation());
        }
        if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)) {
          LocalDate dateToCheck =
              fixedAsset.getFiscalFirstDepreciationDateInitSelect()
                          == FixedAssetCategoryRepository
                              .REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                      && fixedAsset.getFirstServiceDate() != null
                  ? fixedAsset.getFirstServiceDate()
                  : fixedAsset.getAcquisitionDate();
          checkFailOverDate(
              fixedAsset.getFiscalPeriodicityTypeSelect(),
              dateToCheck,
              fixedAsset.getFailoverDate(),
              fixedAsset.getFiscalNumberOfDepreciation());
        }
        if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)) {
          LocalDate dateToCheck =
              fixedAsset.getIfrsFirstDepreciationDateInitSelect()
                          == FixedAssetCategoryRepository
                              .REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                      && fixedAsset.getFirstServiceDate() != null
                  ? fixedAsset.getFirstServiceDate()
                  : fixedAsset.getAcquisitionDate();
          checkFailOverDate(
              fixedAsset.getIfrsPeriodicityTypeSelect(),
              dateToCheck,
              fixedAsset.getFailoverDate(),
              fixedAsset.getIfrsNumberOfDepreciation());
        }
      }
    }
  }

  protected void checkFailOverDate(
      Integer periodicityTypeSelect,
      LocalDate dateToCheck,
      LocalDate failOverDate,
      Integer numberOfDepreciation)
      throws AxelorException {
    ChronoUnit chronoUnit =
        periodicityTypeSelect == FixedAssetRepository.PERIODICITY_TYPE_MONTH
            ? ChronoUnit.MONTHS
            : ChronoUnit.YEARS;

    if (chronoUnit.between(dateToCheck, failOverDate) >= numberOfDepreciation
        || failOverDate.isBefore(dateToCheck)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_FAILOVER_CONTROL_DATE_NOT_CONFORM));
    }
  }

  @Override
  public boolean isFailOver(FixedAsset fixedAsset) {
    return fixedAsset.getOriginSelect() == FixedAssetRepository.ORIGINAL_SELECT_IMPORT
        && fixedAsset.getFailoverDate() != null;
  }
}
