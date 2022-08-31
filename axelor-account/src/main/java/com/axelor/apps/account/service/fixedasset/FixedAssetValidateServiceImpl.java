package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class FixedAssetValidateServiceImpl implements FixedAssetValidateService {

  protected FixedAssetLineService fixedAssetLineService;

  protected FixedAssetGenerationService fixedAssetGenerationService;

  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;

  protected FixedAssetRepository fixedAssetRepo;

  @Inject
  public FixedAssetValidateServiceImpl(
      FixedAssetLineService fixedAssetLineService,
      FixedAssetGenerationService fixedAssetGenerationService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetRepository fixedAssetRepo) {
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetGenerationService = fixedAssetGenerationService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetRepo = fixedAssetRepo;
  }

  /**
   * {@inheritDoc}
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAsset is null
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(FixedAsset fixedAsset) throws AxelorException {
    Objects.requireNonNull(fixedAsset);

    if (fixedAsset.getGrossValue().signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_VALIDATE_GROSS_VALUE_0),
          fixedAsset.getReference());
    }

    if (fixedAsset.getGrossValue().compareTo(BigDecimal.ZERO) > 0) {

      if (StringUtils.isEmpty(fixedAsset.getFixedAssetSeq())) {
        fixedAsset.setFixedAssetSeq(fixedAssetGenerationService.generateSequence(fixedAsset));
      }

      if (fixedAsset.getFixedAssetLineList() != null
          && !fixedAsset.getFixedAssetLineList().isEmpty()) {
        fixedAssetLineService.clear(fixedAsset.getFixedAssetLineList());
      }
      if (fixedAsset.getFiscalFixedAssetLineList() != null
          && !fixedAsset.getFiscalFixedAssetLineList().isEmpty()) {
        fixedAssetLineService.clear(fixedAsset.getFiscalFixedAssetLineList());
      }
      if (fixedAsset.getFixedAssetDerogatoryLineList() != null
          && !fixedAsset.getFixedAssetDerogatoryLineList().isEmpty()) {
        fixedAssetDerogatoryLineService.clear(fixedAsset.getFixedAssetDerogatoryLineList());
      }
      if (fixedAsset.getIfrsFixedAssetLineList() != null
          && !fixedAsset.getIfrsFixedAssetLineList().isEmpty()) {
        fixedAssetLineService.clear(fixedAsset.getIfrsFixedAssetLineList());
      }

      if (fixedAsset.getDepreciationPlanSelect() != null) {
        if (!fixedAsset
            .getDepreciationPlanSelect()
            .contains(FixedAssetRepository.DEPRECIATION_PLAN_NONE)) {
          fixedAsset = fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
        } else {
          fixedAsset.setNumberOfDepreciation(fixedAsset.getNumberOfDepreciation() - 1);
        }

        if (fixedAsset.getIsEqualToFiscalDepreciation()) {
          fixedAsset.setAccountingValue(fixedAsset.getGrossValue());
        } else if (fixedAsset
            .getDepreciationPlanSelect()
            .equals(FixedAssetRepository.DEPRECIATION_PLAN_NONE)) {
          fixedAsset.setAccountingValue(BigDecimal.ZERO);
        } else {
          fixedAsset.setAccountingValue(
              fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue()));
        }
      }
    }
    fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_VALIDATED);
    fixedAssetRepo.save(fixedAsset);
  }

  @Override
  public int massValidation(List<Long> fixedAssetIds) throws AxelorException {
    int count = 0;
    for (Long id : fixedAssetIds) {
      FixedAsset fixedAsset = fixedAssetRepo.find(id);
      if (fixedAsset.getStatusSelect() == FixedAssetRepository.STATUS_DRAFT) {
        validate(fixedAsset);
        JPA.clear();
        count++;
      }
    }
    return count;
  }
}
