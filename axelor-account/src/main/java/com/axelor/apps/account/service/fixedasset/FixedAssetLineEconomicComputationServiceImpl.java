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
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticFixedAssetService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequestScoped
public class FixedAssetLineEconomicComputationServiceImpl
    extends AbstractFixedAssetLineComputationServiceImpl {

  protected AnalyticFixedAssetService analyticFixedAssetService;

  @Inject
  public FixedAssetLineEconomicComputationServiceImpl(
      AnalyticFixedAssetService analyticFixedAssetService,
      FixedAssetFailOverControlService fixedAssetFailOverControlService) {
    super(fixedAssetFailOverControlService);
    this.analyticFixedAssetService = analyticFixedAssetService;
  }

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      return fixedAsset.getFailoverDate();
    }
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
  protected LocalDate computeProrataTemporisFirstDepreciationDate(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsEqualToFiscalDepreciation()) {
      return analyticFixedAssetService.computeFirstDepreciationDate(
          fixedAsset, fixedAsset.getFirstServiceDate());
    }
    return fixedAsset.getFirstDepreciationDate();
  }

  @Override
  protected LocalDate computeProrataTemporisAcquisitionDate(FixedAsset fixedAsset) {
    if (!fixedAsset.getIsEqualToFiscalDepreciation()) {
      return fixedAsset.getFirstServiceDate();
    }
    return fixedAsset.getAcquisitionDate();
  }

  @Override
  protected List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset) {
    return fixedAsset.getFixedAssetLineList();
  }

  @Override
  protected Integer getNumberOfDepreciation(FixedAsset fixedAsset) {
    return fixedAsset.getNumberOfDepreciation();
  }

  @Override
  protected String getComputationMethodSelect(FixedAsset fixedAsset) {
    return fixedAsset.getComputationMethodSelect();
  }

  @Override
  protected BigDecimal getDegressiveCoef(FixedAsset fixedAsset) {
    return fixedAsset.getDegressiveCoef();
  }

  @Override
  protected Integer getPeriodicityInMonth(FixedAsset fixedAsset) {
    return fixedAsset.getPeriodicityInMonth();
  }

  @Override
  protected Integer getTypeSelect() {

    return FixedAssetLineRepository.TYPE_SELECT_ECONOMIC;
  }

  @Override
  protected Boolean isProrataTemporis(FixedAsset fixedAsset) {
    return fixedAsset.getFixedAssetCategory().getIsProrataTemporis();
  }

  @Override
  public FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset)
      throws AxelorException {
    FixedAssetLine line = super.computeInitialPlannedFixedAssetLine(fixedAsset);
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      line.setCumulativeDepreciation(
          line.getCumulativeDepreciation().add(getAlreadyDepreciatedAmount(fixedAsset)));
      line.setAccountingValue(
          line.getAccountingValue().subtract(getAlreadyDepreciatedAmount(fixedAsset)));
    }
    return line;
  }

  @Override
  protected int numberOfDepreciationDone(FixedAsset fixedAsset) {
    List<FixedAssetLine> fixedAssetLineList = getFixedAssetLineList(fixedAsset);
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      if (fixedAssetLineList == null) {
        return fixedAsset.getNbrOfPastDepreciations();
      }
      return fixedAssetLineList.size() + fixedAsset.getNbrOfPastDepreciations();
    }
    if (fixedAssetLineList == null) {
      return 0;
    }
    return fixedAssetLineList.size();
  }
}
