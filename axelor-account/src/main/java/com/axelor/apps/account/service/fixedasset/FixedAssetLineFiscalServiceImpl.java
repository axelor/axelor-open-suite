/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.YearService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequestScoped
public class FixedAssetLineFiscalServiceImpl extends AbstractFixedAssetLineServiceImpl {

  protected FixedAssetLineFiscalComputationServiceImpl fixedAssetLineFiscalComputationService;

  @Inject
  public FixedAssetLineFiscalServiceImpl(
      FixedAssetLineRepository fixedAssetLineRepository,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      YearService yearService,
      PeriodService periodService,
      FixedAssetLineFiscalComputationServiceImpl fixedAssetLineFiscalComputationService) {
    super(fixedAssetLineRepository, fixedAssetDerogatoryLineService, yearService, periodService);
    this.fixedAssetLineFiscalComputationService = fixedAssetLineFiscalComputationService;
  }

  @Override
  protected int getPeriodicityTypeSelect(FixedAsset fixedAsset) {
    return fixedAsset.getFiscalPeriodicityTypeSelect();
  }

  @Override
  protected int getPeriodicityInMonth(FixedAsset fixedAsset) {
    return fixedAsset.getFiscalPeriodicityInMonth();
  }

  @Override
  protected List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset) {
    return fixedAsset.getFiscalFixedAssetLineList();
  }

  @Override
  protected BigDecimal computeProrataBetween(
      FixedAsset fixedAsset,
      LocalDate previousRealizedDate,
      LocalDate disposalDate,
      LocalDate nextPlannedDate) {
    return fixedAssetLineFiscalComputationService.computeProrataBetween(
        fixedAsset, previousRealizedDate, disposalDate, nextPlannedDate);
  }

  @Override
  protected int getTypeSelect() {
    return FixedAssetLineRepository.TYPE_SELECT_FISCAL;
  }
}
