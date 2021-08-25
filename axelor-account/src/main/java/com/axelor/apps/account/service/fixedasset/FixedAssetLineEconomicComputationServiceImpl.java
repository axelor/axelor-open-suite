/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticFixedAssetService;
import com.axelor.apps.tool.date.DateTool;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;

@RequestScoped
public class FixedAssetLineEconomicComputationServiceImpl
    extends AbstractFixedAssetLineComputationServiceImpl {

  protected AnalyticFixedAssetService analyticFixedAssetService;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;

  @Inject
  public FixedAssetLineEconomicComputationServiceImpl(
      AnalyticFixedAssetService analyticFixedAssetService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService) {
    this.analyticFixedAssetService = analyticFixedAssetService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
  }

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsEqualToFiscalDepreciation()) {
      return analyticFixedAssetService.computeFirstDepreciationDate(
          fixedAsset, fixedAsset.getFirstServiceDate());
    }
    return fixedAsset.getFirstDepreciationDate();
  }

  @Override
  protected BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsEqualToFiscalDepreciation()
        && fixedAsset
            .getComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)) {
      return fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue());
    }
    return fixedAsset.getGrossValue();
  }

  @Override
  protected BigDecimal computeInitialDepreciation(FixedAsset fixedAsset, BigDecimal baseValue) {

    if (fixedAsset.getComputationMethodSelect() != null
        && fixedAsset
            .getComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      // Theses cases is for when user want to depreciate in one year.
      // This case is if list is not empty when calling this method
      if (fixedAsset.getFixedAssetLineList() != null
          && fixedAsset.getFixedAssetLineList().size()
              == fixedAsset.getNumberOfDepreciation() - 1) {
        return baseValue;
      }
      if (fixedAsset.getFixedAssetLineList() == null && fixedAsset.getNumberOfDepreciation() == 1) {
        return baseValue;
      }
      // In case of economic type, boolean argument is always false, since there is copy before.
      return computeInitialDegressiveDepreciation(fixedAsset, baseValue);
    } else {
      return computeInitialLinearDepreciation(fixedAsset, baseValue);
    }
  }

  @Override
  protected LocalDate computeProrataFirstDepreciationDate(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsEqualToFiscalDepreciation()) {
      return analyticFixedAssetService.computeFirstDepreciationDate(
          fixedAsset, fixedAsset.getFirstServiceDate());
    }
    return fixedAsset.getFirstDepreciationDate();
  }

  @Override
  protected LocalDate computeProrataAcquisitionDate(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsEqualToFiscalDepreciation()) {
      return fixedAsset.getFirstServiceDate();
    }
    return fixedAsset.getAcquisitionDate();
  }

  @Override
  protected BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {

    if (fixedAsset
        .getComputationMethodSelect()
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      if (!fixedAsset.getIsEqualToFiscalDepreciation()) {
        return previousFixedAssetLine
            .getDepreciationBase()
            .subtract(previousFixedAssetLine.getDepreciation());
      }
      return previousFixedAssetLine.getAccountingValue();
    }

    return previousFixedAssetLine.getDepreciationBase();
  }

  @Override
  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, BigDecimal baseValue) {
    BigDecimal depreciation;
    // case of economic type
    if (fixedAsset
        .getComputationMethodSelect()
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      if (fixedAsset.getFixedAssetLineList() != null
          && fixedAsset.getFixedAssetLineList().size()
              == fixedAsset.getNumberOfDepreciation() - 1) {
        depreciation = previousFixedAssetLine.getAccountingValue();
      } else {
        // In case of economic type, boolean argument is always false. We did a copy of the fiscal
        // values,
        // we can pick values from non fiscal field.
        depreciation = computeOnGoingDegressiveDepreciation(fixedAsset, previousFixedAssetLine);
      }

    } else {
      // In case of linear, we must filter line that have a correctedAccountingValue and line that
      // are realized and not count them to know if we are computing the last line.
      // Because when recomputing, number of depreciation is overwrite as follow (nbDepreciation -
      // list.size())
      if (fixedAsset.getFixedAssetLineList() != null
          && super.countNotCorrectedPlannedLines(fixedAsset.getFixedAssetLineList())
              == fixedAsset.getNumberOfDepreciation() - 1) {
        // So we must depreciate the remaining accounting value.
        depreciation = previousFixedAssetLine.getAccountingValue();
      } else {
        depreciation = computeLinearDepreciation(fixedAsset, baseValue);
      }
    }
    if (BigDecimal.ZERO.compareTo(
            previousFixedAssetLine.getAccountingValue().subtract(depreciation))
        > 0) {
      depreciation = previousFixedAssetLine.getAccountingValue();
    }
    return depreciation;
  }

  @Override
  protected LocalDate computeDepreciationDate(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    LocalDate depreciationDate;
    depreciationDate =
        DateTool.plusMonths(
            previousFixedAssetLine.getDepreciationDate(), fixedAsset.getPeriodicityInMonth());

    return depreciationDate;
  }
}
