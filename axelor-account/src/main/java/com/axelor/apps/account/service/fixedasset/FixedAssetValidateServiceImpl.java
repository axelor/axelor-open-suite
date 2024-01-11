/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

      Optional<FixedAssetLine> lastRealizedLine =
          fixedAssetLineService.findNewestFixedAssetLine(
              fixedAsset.getFixedAssetLineList(), FixedAssetLineRepository.STATUS_REALIZED, 0);
      if (lastRealizedLine.isPresent()) {
        fixedAsset.setAccountingValue(lastRealizedLine.get().getAccountingValue());
      } else if (fixedAsset.getIsEqualToFiscalDepreciation()) {
        fixedAsset.setAccountingValue(fixedAsset.getGrossValue());
      } else if (fixedAsset.getDepreciationPlanSelect().isEmpty()
          || fixedAsset
              .getDepreciationPlanSelect()
              .equals(FixedAssetRepository.DEPRECIATION_PLAN_NONE)) {
        fixedAsset.setAccountingValue(BigDecimal.ZERO);
      } else {
        fixedAsset.setAccountingValue(
            fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue()));
      }
    }
    if (fixedAsset.getStatusSelect() == FixedAssetRepository.STATUS_DRAFT) {
      fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_VALIDATED);
      fixedAsset.setInitialPeriodicityInMonth(fixedAsset.getPeriodicityInMonth());
    }
    fixedAssetRepo.save(fixedAsset);
  }

  @Override
  public int massValidation(List<Long> fixedAssetIds) throws AxelorException {
    int count = 0;
    for (Long id : fixedAssetIds) {
      FixedAsset fixedAsset = fixedAssetRepo.find(id);
      if (fixedAsset.getStatusSelect() == FixedAssetRepository.STATUS_DRAFT) {
        try {
          validate(fixedAsset);
        } catch (AxelorException e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(AccountExceptionMessage.FIXED_ASSET_MASS_VALIDATION_EXCEPTION),
              fixedAsset.getId(),
              e.getLocalizedMessage());
        }

        JPA.clear();
        count++;
      }
    }
    return count;
  }
}
