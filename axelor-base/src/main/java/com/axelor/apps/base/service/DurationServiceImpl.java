/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.google.inject.Singleton;

@Singleton
public class DurationServiceImpl implements DurationService  {

	private static final BigDecimal DAYS_IN_MONTH = BigDecimal.valueOf(30);

	@Override
	public LocalDate computeDuration(Duration duration, LocalDate date) {
		if(duration == null) { return date; }
		switch (duration.getTypeSelect()) {
			case DurationRepository.TYPE_MONTH:
				return date.plusMonths(duration.getValue());
			case DurationRepository.TYPE_DAY:
				return date.plusDays(duration.getValue());
			default:
				return date;
		}
	}

	@Override
	public BigDecimal overflowRatio(LocalDate start, LocalDate end, Duration duration) {
		if (duration.getTypeSelect() == DurationRepository.TYPE_MONTH) {
			end = end.plus(1, ChronoUnit.DAYS);
		}
		LocalDate theoryEnd = computeDuration(duration, start);
		long restMonths = ChronoUnit.MONTHS.between(theoryEnd, end);
		if (restMonths > 0) {
			theoryEnd = theoryEnd.plus(restMonths, ChronoUnit.MONTHS);
		}
		BigDecimal restDays = new BigDecimal(ChronoUnit.DAYS.between(theoryEnd, end));

		return BigDecimal.ONE.add(BigDecimal.valueOf(restMonths)).add(restDays.divide(DAYS_IN_MONTH, MathContext.DECIMAL32));
	}

}
