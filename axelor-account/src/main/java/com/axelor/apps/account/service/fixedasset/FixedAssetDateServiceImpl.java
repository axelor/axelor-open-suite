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
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.YearService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class FixedAssetDateServiceImpl implements FixedAssetDateService {

  protected PeriodService periodService;
  protected YearService yearService;

  @Inject
  public FixedAssetDateServiceImpl(PeriodService periodService, YearService yearService) {
    this.periodService = periodService;
    this.yearService = yearService;
  }

  @Override
  public void computeFirstDepreciationDate(FixedAsset fixedAsset) {
    computeEconomicFirstDepreciationDate(fixedAsset);
    computeFiscalFirstDepreciationDate(fixedAsset);
    computeIfrsFirstDepreciationDate(fixedAsset);
  }

  @Override
  public void computeFiscalFirstDepreciationDate(FixedAsset fixedAsset) {
    LocalDate fiscalDate =
        fixedAsset.getFiscalFirstDepreciationDateInitSelect()
                    == FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                && fixedAsset.getFirstServiceDate() != null
            ? fixedAsset.getFirstServiceDate()
            : fixedAsset.getAcquisitionDate();
    fixedAsset.setFiscalFirstDepreciationDate(
        computeFirstDepreciationDate(
            fixedAsset.getCompany(), fiscalDate, fixedAsset.getFiscalPeriodicityTypeSelect()));
  }

  @Override
  public void computeEconomicFirstDepreciationDate(FixedAsset fixedAsset) {
    LocalDate economicDate =
        fixedAsset.getFirstDepreciationDateInitSelect()
                    == FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                && fixedAsset.getFirstServiceDate() != null
            ? fixedAsset.getFirstServiceDate()
            : fixedAsset.getAcquisitionDate();

    fixedAsset.setFirstDepreciationDate(
        computeFirstDepreciationDate(
            fixedAsset.getCompany(), economicDate, fixedAsset.getPeriodicityTypeSelect()));
  }

  @Override
  public void computeIfrsFirstDepreciationDate(FixedAsset fixedAsset) {
    LocalDate ifrsDate =
        fixedAsset.getIfrsFirstDepreciationDateInitSelect()
                    == FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
                && fixedAsset.getFirstServiceDate() != null
            ? fixedAsset.getFirstServiceDate()
            : fixedAsset.getAcquisitionDate();

    fixedAsset.setIfrsFirstDepreciationDate(
        computeFirstDepreciationDate(
            fixedAsset.getCompany(), ifrsDate, fixedAsset.getIfrsPeriodicityTypeSelect()));
  }

  protected LocalDate computeFirstDepreciationDate(
      Company company, LocalDate date, Integer periodicityTypeSelect) {

    if (periodicityTypeSelect == FixedAssetRepository.PERIODICITY_TYPE_MONTH) {
      return computeLastDayOfFiscalPeriod(company, date, periodicityTypeSelect);
    } else {
      return computeLastDayOfFiscalYear(company, date, periodicityTypeSelect);
    }
  }

  @Override
  public LocalDate computeLastDayOfFiscalPeriod(
      Company company, LocalDate date, Integer periodicityTypeSelect) {
    Period period = periodService.getPeriod(date, company, YearRepository.TYPE_FISCAL);
    if (period == null) {
      // Last day of the month of date
      return computeLastDayOfPeriodicity(periodicityTypeSelect, date);
    }
    return period.getToDate();
  }

  @Override
  public LocalDate computeLastDayOfFiscalYear(
      Company company, LocalDate date, Integer periodicityTypeSelect) {
    Year year = yearService.getYear(date, company, YearRepository.TYPE_FISCAL);
    if (year == null) {
      // Last day of the year of date
      return computeLastDayOfPeriodicity(periodicityTypeSelect, date);
    }
    return year.getToDate();
  }

  @Override
  public LocalDate computeLastDayOfPeriodicity(Integer periodicityType, LocalDate date) {
    if (periodicityType == null || date == null) {
      return date;
    }
    if (periodicityType == FixedAssetRepository.PERIODICITY_TYPE_YEAR) {
      return LocalDate.of(date.getYear(), 12, 31);
    }
    if (periodicityType == FixedAssetRepository.PERIODICITY_TYPE_MONTH) {
      return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    return date;
  }
}
