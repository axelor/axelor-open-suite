/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class AnalyticFixedAssetServiceImpl implements AnalyticFixedAssetService {

  public LocalDate computeFirstDepreciationDate(FixedAsset fixedAsset, LocalDate date) {
    if (fixedAsset.getPeriodicityTypeSelect() == null) {
      return date;
    }
    if (fixedAsset.getPeriodicityTypeSelect() == FixedAssetRepository.PERIODICITY_TYPE_YEAR) {
      return LocalDate.of(date.getYear(), 12, 31);
    }
    if (fixedAsset.getPeriodicityTypeSelect() == FixedAssetRepository.PERIODICITY_TYPE_MONTH) {
      return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    return date;
  }
}
