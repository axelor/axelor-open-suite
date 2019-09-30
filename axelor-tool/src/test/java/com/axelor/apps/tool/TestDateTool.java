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
package com.axelor.apps.tool;

import com.axelor.apps.tool.date.DateTool;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

public class TestDateTool {

  @Test
  public void testGetNbDay() {
    Assert.assertEquals(1, DateTool.daysBetween(LocalDate.now(), LocalDate.now(), false));
    Assert.assertEquals(
        30, DateTool.daysBetween(LocalDate.of(2011, 9, 1), LocalDate.of(2011, 9, 30), false));
    Assert.assertEquals(
        26, DateTool.daysBetween(LocalDate.of(2011, 2, 2), LocalDate.of(2011, 2, 27), true));
    Assert.assertEquals(
        26, DateTool.daysBetween(LocalDate.of(2011, 2, 2), LocalDate.of(2011, 2, 27), false));
    Assert.assertEquals(
        -26, DateTool.daysBetween(LocalDate.of(2011, 2, 27), LocalDate.of(2011, 2, 2), false));
    Assert.assertEquals(
        -26, DateTool.daysBetween(LocalDate.of(2011, 2, 27), LocalDate.of(2011, 2, 2), true));
    Assert.assertEquals(
        30, DateTool.daysBetween(LocalDate.of(2011, 2, 1), LocalDate.of(2011, 2, 28), true));
    Assert.assertEquals(
        1, DateTool.daysBetween(LocalDate.of(2011, 7, 30), LocalDate.of(2011, 7, 31), true));
    Assert.assertEquals(
        54, DateTool.daysBetween(LocalDate.of(2011, 7, 12), LocalDate.of(2011, 9, 5), true));
    Assert.assertEquals(
        30, DateTool.daysBetween(LocalDate.of(2011, 7, 15), LocalDate.of(2011, 8, 14), true));
    Assert.assertEquals(
        30, DateTool.daysBetween(LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 31), true));
    Assert.assertEquals(
        31, DateTool.daysBetween(LocalDate.of(2012, 2, 29), LocalDate.of(2012, 3, 30), true));
    Assert.assertEquals(
        31, DateTool.daysBetween(LocalDate.of(2011, 2, 28), LocalDate.of(2011, 3, 30), true));
    Assert.assertEquals(
        33, DateTool.daysBetween(LocalDate.of(2012, 2, 28), LocalDate.of(2012, 3, 30), true));
    Assert.assertEquals(
        181, DateTool.daysBetween(LocalDate.of(2011, 12, 31), LocalDate.of(2012, 6, 30), true));
    Assert.assertEquals(
        -68, DateTool.daysBetween(LocalDate.of(2011, 12, 9), LocalDate.of(2011, 10, 2), true));
  }

  @Test
  public void testIsProrata() {

    // dateFrame1<date1<dateFrame2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 7, 10),
            LocalDate.of(2011, 7, 30)));
    // dateFrame1<date2<dateFrame2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 6, 1),
            LocalDate.of(2011, 7, 10)));
    // date1<dateFrame1 and dateFrame2<date2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 6, 1),
            LocalDate.of(2011, 7, 30)));
    // dateFrame1=date1 and dateFrame2=date2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15)));
    // date1=dateFrame1
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 30)));
    // date1=dateFrame2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 7, 30)));
    // date2=dateFrame1
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 6, 1),
            LocalDate.of(2011, 7, 1)));
    // date2=dateFrame2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 6, 1),
            LocalDate.of(2011, 7, 15)));
    // date2=null and date1<dateFrame1
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 15), LocalDate.of(2011, 6, 1), null));
    // date2=null and date1=dateFrame1
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 15), LocalDate.of(2011, 7, 1), null));
    // date2=null and date1>dateFrame1 and date1<dateFrame2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 15), LocalDate.of(2011, 7, 10), null));
    // date2=null and date1=dateFrame2
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 15), LocalDate.of(2011, 7, 15), null));
    // date2=null and date1<dateFrame1
    Assert.assertTrue(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 15), LocalDate.of(2011, 6, 1), null));
    // date2=null and date1>dateFrame2
    Assert.assertFalse(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 15), LocalDate.of(2011, 8, 1), null));
    // date2<dateFrame1
    Assert.assertFalse(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 6, 1),
            LocalDate.of(2011, 6, 30)));
    // date1>dateFrame2
    Assert.assertFalse(
        DateTool.isProrata(
            LocalDate.of(2011, 7, 1),
            LocalDate.of(2011, 7, 15),
            LocalDate.of(2011, 8, 1),
            LocalDate.of(2011, 8, 30)));
  }

  @Test
  public void testGetNbFullMonth() {
    Assert.assertEquals(
        1, DateTool.days360MonthsBetween(LocalDate.of(2011, 1, 1), LocalDate.of(2011, 2, 5)));
    Assert.assertEquals(
        0, DateTool.days360MonthsBetween(LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 25)));
    Assert.assertEquals(
        1, DateTool.days360MonthsBetween(LocalDate.of(2011, 12, 15), LocalDate.of(2012, 1, 25)));
    Assert.assertEquals(
        1, DateTool.days360MonthsBetween(LocalDate.of(2011, 12, 15), LocalDate.of(2012, 1, 15)));
    Assert.assertEquals(
        1, DateTool.days360MonthsBetween(LocalDate.of(2011, 12, 15), LocalDate.of(2012, 1, 14)));
    Assert.assertEquals(
        0, DateTool.days360MonthsBetween(LocalDate.of(2011, 12, 15), LocalDate.of(2012, 1, 13)));
    Assert.assertEquals(
        5, DateTool.days360MonthsBetween(LocalDate.of(2011, 10, 7), LocalDate.of(2012, 3, 9)));
    Assert.assertEquals(
        1, DateTool.days360MonthsBetween(LocalDate.of(2011, 1, 31), LocalDate.of(2011, 2, 28)));
    Assert.assertEquals(
        1, DateTool.days360MonthsBetween(LocalDate.of(2011, 3, 31), LocalDate.of(2011, 4, 30)));
    Assert.assertEquals(
        -5, DateTool.days360MonthsBetween(LocalDate.of(2012, 3, 9), LocalDate.of(2011, 10, 7)));
    Assert.assertEquals(
        -1, DateTool.days360MonthsBetween(LocalDate.of(2011, 4, 30), LocalDate.of(2011, 3, 31)));
  }

  @Test
  public void testNextOccurency() {
    Assert.assertEquals(
        LocalDate.of(2010, 11, 9),
        DateTool.nextOccurency(LocalDate.of(2010, 10, 7), LocalDate.of(2011, 3, 9), 2));
    Assert.assertEquals(
        LocalDate.of(2010, 11, 9),
        DateTool.nextOccurency(LocalDate.of(2010, 10, 7), LocalDate.of(2011, 5, 9), 2));
    Assert.assertEquals(
        LocalDate.of(2010, 8, 31),
        DateTool.nextOccurency(LocalDate.of(2010, 8, 7), LocalDate.of(2011, 4, 30), 1));
    Assert.assertEquals(
        LocalDate.of(2010, 5, 9),
        DateTool.nextOccurency(LocalDate.of(2010, 3, 9), LocalDate.of(2011, 3, 9), 2));
  }

  @Test
  public void testLastOccurency() {
    Assert.assertEquals(
        LocalDate.of(2011, 3, 9),
        DateTool.lastOccurency(LocalDate.of(2010, 11, 9), LocalDate.of(2011, 5, 9), 4));
    Assert.assertEquals(
        LocalDate.of(2011, 7, 9),
        DateTool.lastOccurency(LocalDate.of(2010, 11, 9), LocalDate.of(2011, 9, 9), 4));
    Assert.assertEquals(
        LocalDate.of(2011, 7, 9),
        DateTool.lastOccurency(LocalDate.of(2010, 11, 9), LocalDate.of(2011, 10, 9), 4));
    Assert.assertEquals(
        LocalDate.of(2011, 1, 9),
        DateTool.lastOccurency(LocalDate.of(2010, 11, 9), LocalDate.of(2011, 1, 9), 2));
    Assert.assertEquals(
        LocalDate.of(2011, 7, 31),
        DateTool.lastOccurency(LocalDate.of(2007, 4, 30), LocalDate.of(2011, 8, 6), 1));
  }
}
