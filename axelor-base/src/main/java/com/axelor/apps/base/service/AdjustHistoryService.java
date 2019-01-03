/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.AdjustHistory;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.AdjustHistoryRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class AdjustHistoryService {
  private AdjustHistoryRepository adjustHistoryRepo;

  @Inject
  public AdjustHistoryService(AdjustHistoryRepository adjustHistoryRepo) {
    this.adjustHistoryRepo = adjustHistoryRepo;
  }

  @Transactional
  public void setStartDate(Year year) {
    AdjustHistory adjustHistory = new AdjustHistory();
    adjustHistory.setFiscalYear(year);
    adjustHistory.setStartDate(LocalDateTime.now());
    adjustHistoryRepo.save(adjustHistory);
  }

  @Transactional
  public void setStartDate(Period period) {
    AdjustHistory adjustHistory = new AdjustHistory();
    adjustHistory.setPeriod(period);
    adjustHistory.setStartDate(LocalDateTime.now());
    adjustHistoryRepo.save(adjustHistory);
  }

  @Transactional
  public AdjustHistory setEndDate(Year year) {
    AdjustHistory adjustHistory =
        adjustHistoryRepo
            .all()
            .filter("self.fiscalYear.id = ? AND self.endDate IS NULL", year.getId())
            .fetchOne();
    adjustHistory.setEndDate(LocalDateTime.now());
    adjustHistoryRepo.save(adjustHistory);

    return adjustHistory;
  }

  @Transactional
  public void setEndDate(Period period) {
    AdjustHistory adjustHistory =
        adjustHistoryRepo
            .all()
            .filter("self.period.id = ? AND self.endDate IS NULL", period.getId())
            .fetchOne();
    adjustHistory.setEndDate(LocalDateTime.now());
    adjustHistoryRepo.save(adjustHistory);
  }
}
