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
