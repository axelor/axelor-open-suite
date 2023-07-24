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
package com.axelor.apps.base.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.csv.script.ImportDateTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestImportDateTime {

  protected ImportDateTime importDateTime;

  protected LocalDateTime todayDateTime = LocalDateTime.of(2022, 5, 13, 0, 0, 0);

  @Before
  public void setUp() {
    LocalDate todayDate = LocalDate.of(2022, 5, 13);
    AppBaseService appBaseService = mock(AppBaseService.class);
    when(appBaseService.getTodayDate(any())).thenReturn(todayDate);
    importDateTime = new ImportDateTime(appBaseService);
  }

  @Test
  public void testDateImportToday() {
    Assert.assertEquals(LocalDate.of(2022, 5, 13).toString(), importDateTime.importDate("TODAY"));
  }

  @Test
  public void testDateImportTodayMinusFourYears() {
    Assert.assertEquals(
        todayDateTime.minusYears(4).toString(), importDateTime.importDate("TODAY[-4y]"));
  }

  @Test
  public void testDateImportTodayMinusOneMonth() {
    Assert.assertEquals(
        todayDateTime.minusMonths(1).toString(), importDateTime.importDate("TODAY[-1M]"));
  }

  @Test
  public void testDateImportTodayMinusOneDay() {
    Assert.assertEquals(
        todayDateTime.minusDays(1).toString(), importDateTime.importDate("TODAY[-1d]"));
  }

  @Test
  public void testDateImportTodayMinusFourYearsFixedMonthFixedDay() {
    Assert.assertEquals(
        todayDateTime.minusYears(4).withDayOfMonth(1).withMonth(1).toString(),
        importDateTime.importDate("TODAY[-4y=1M=1d]"));
  }

  @Test
  public void testDateImportTodayFixedMonthFixedDay() {
    Assert.assertEquals(
        todayDateTime.withDayOfMonth(1).withMonth(1).toString(),
        importDateTime.importDate("TODAY[=1M=1d]"));
  }

  @Test
  public void testDateImportTodayFixedDay() {
    Assert.assertEquals(
        todayDateTime.withDayOfMonth(1).toString(), importDateTime.importDate("TODAY[=1d]"));
  }

  @Test
  public void testDateImportTodayFixedMonth() {
    Assert.assertEquals(
        todayDateTime.withMonth(1).toString(), importDateTime.importDate("TODAY[=1M]"));
  }

  @Test
  public void testDateImportTodayFixedYear() {
    Assert.assertEquals(
        todayDateTime.withYear(2014).toString(), importDateTime.importDate("TODAY[=2014y]"));
  }
}
