package com.axelor.apps.account.service.fixedasset;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

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
      if (fixedAssetCategory
          .getComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_FAILOVER_CONTROL_ONLY_LINEAR));
      }
      if (fixedAsset.getAlreadyDepreciatedAmount() != null
          && fixedAsset.getAlreadyDepreciatedAmount().compareTo(fixedAsset.getGrossValue()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                IExceptionMessage
                    .IMMO_FIXED_ASSET_FAILOVER_CONTROL_PAST_DEPRECIATION_GREATER_THAN_GROSS_VALUE));
      }

      if (fixedAsset.getFailoverDate() != null) {
        ChronoUnit chronoUnit =
            fixedAsset.getFiscalPeriodicityTypeSelect()
                    == FixedAssetRepository.PERIODICITY_TYPE_MONTH
                ? ChronoUnit.MONTHS
                : ChronoUnit.YEARS;
        if (fixedAssetCategory.getFirstDepreciationDateInitSelect()
                == FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_DATE_ACQUISITION
            && (chronoUnit.between(fixedAsset.getAcquisitionDate(), fixedAsset.getFailoverDate())
                >= fixedAsset.getFiscalNumberOfDepreciation() || fixedAsset.getFailoverDate().isBefore(fixedAsset.getAcquisitionDate()))) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_FAILOVER_CONTROL_DATE_NOT_CONFORM));
        } else if (fixedAssetCategory.getFirstDepreciationDateInitSelect()
                == FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
            && (chronoUnit.between(fixedAsset.getFirstServiceDate(), fixedAsset.getFailoverDate())
                >= fixedAsset.getFiscalNumberOfDepreciation() || fixedAsset.getFailoverDate().isBefore(fixedAsset.getFirstServiceDate()))) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_FAILOVER_CONTROL_DATE_NOT_CONFORM));
        }
      }
    }
  }

  @Override
  public boolean isFailOver(FixedAsset fixedAsset) {
    return fixedAsset.getFailoverDate() != null
        || (fixedAsset.getNbrOfPastDepreciations() != null
            && fixedAsset.getNbrOfPastDepreciations() > 0)
        || (fixedAsset.getAlreadyDepreciatedAmount() != null
            && fixedAsset.getAlreadyDepreciatedAmount().signum() > 0);
  }
}
