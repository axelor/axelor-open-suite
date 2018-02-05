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
package com.axelor.apps.contract.test;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class DateTest {

	@Test
	public void test() {
		LocalDate date = LocalDate.of(2017, 5, 15);

		// 15-05-2017 + 1 month == 15-06-2017
		Assert.assertEquals(date.plusMonths(1), LocalDate.of(2017, 6, 15));

		// 31-05-2017 + 1 month == 30-06-2017
		Assert.assertEquals(date.withDayOfMonth(31).plusMonths(1), LocalDate.of(2017, 6, 30));
	}
}
