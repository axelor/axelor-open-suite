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

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Singleton
public class DurationServiceImpl implements DurationService {

  public LocalDate computeDuration(Duration duration, LocalDate date) {

    if (duration == null) {
      return date;
    }

    switch (duration.getTypeSelect()) {
      case DurationRepository.TYPE_MONTH:
        return date.plusMonths(duration.getValue());

      case DurationRepository.TYPE_DAY:
        return date.plusDays(duration.getValue());

      default:
        return date;
    }
  }

  public BigDecimal computeDurationInDays(ZonedDateTime startDate, ZonedDateTime endDate) {
    return new BigDecimal(ChronoUnit.DAYS.between(startDate, endDate));
  }
}
