/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.tool;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import com.axelor.apps.tool.date.DateTool;


public class TestDateTool {
	
	@Test
	public void testGetNbDay() {
		Assert.assertEquals(1, DateTool.daysBetween(new LocalDate(), new LocalDate(), false));
		Assert.assertEquals(30, DateTool.daysBetween(new LocalDate(2011, 9, 1), new LocalDate(2011, 9, 30), false));
		Assert.assertEquals(26, DateTool.daysBetween(new LocalDate(2011, 2, 2), new LocalDate(2011, 2, 27), true));
		Assert.assertEquals(26, DateTool.daysBetween(new LocalDate(2011, 2, 2), new LocalDate(2011, 2, 27), false));
		Assert.assertEquals(-26, DateTool.daysBetween(new LocalDate(2011, 2, 27), new LocalDate(2011, 2, 2), false));
		Assert.assertEquals(-26, DateTool.daysBetween(new LocalDate(2011, 2, 27), new LocalDate(2011, 2, 2), true));
		Assert.assertEquals(30, DateTool.daysBetween(new LocalDate(2011, 2, 1), new LocalDate(2011, 2, 28), true));
		Assert.assertEquals(1, DateTool.daysBetween(new LocalDate(2011, 7, 30), new LocalDate(2011, 7, 31), true));
		Assert.assertEquals(54, DateTool.daysBetween(new LocalDate(2011, 7, 12), new LocalDate(2011, 9, 5), true));
		Assert.assertEquals(30, DateTool.daysBetween(new LocalDate(2011, 7, 15), new LocalDate(2011, 8, 14), true));
		Assert.assertEquals(30, DateTool.daysBetween(new LocalDate(2011, 7, 1), new LocalDate(2011, 7, 31), true));
		Assert.assertEquals(31, DateTool.daysBetween(new LocalDate(2012, 2, 29), new LocalDate(2012, 3, 30), true));
		Assert.assertEquals(31, DateTool.daysBetween(new LocalDate(2011, 2, 28), new LocalDate(2011, 3, 30), true));
		Assert.assertEquals(33, DateTool.daysBetween(new LocalDate(2012, 2, 28), new LocalDate(2012, 3, 30), true));
		Assert.assertEquals(181, DateTool.daysBetween(new LocalDate(2011, 12, 31), new LocalDate(2012,6, 30), true));
		Assert.assertEquals(-68, DateTool.daysBetween(new LocalDate(2011, 12, 9), new LocalDate(2011,10, 2), true));
	}
	
	@Test
	public void testIsProrata() {

		//dateFrame1<date1<dateFrame2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,7,10), new LocalDate(2011,7,30)));
		//dateFrame1<date2<dateFrame2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,6,1), new LocalDate(2011,7,10)));
		//date1<dateFrame1 and dateFrame2<date2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,6,1), new LocalDate(2011,7,30)));
		//dateFrame1=date1 and dateFrame2=date2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,7,1), new LocalDate(2011,7,15)));
		//date1=dateFrame1
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,7,1), new LocalDate(2011,7,30)));
		//date1=dateFrame2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,7,15), new LocalDate(2011,7,30)));
		//date2=dateFrame1
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,6,1), new LocalDate(2011,7,1)));
		//date2=dateFrame2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,6,1), new LocalDate(2011,7,15)));
		//date2=null and date1<dateFrame1
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,6,1), null));
		//date2=null and date1=dateFrame1
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,7,1), null));
		//date2=null and date1>dateFrame1 and date1<dateFrame2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,7,10), null));
		//date2=null and date1=dateFrame2
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,7,15), null));
		//date2=null and date1<dateFrame1
		Assert.assertTrue(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,6,1), null));
		//date2=null and date1>dateFrame2
		Assert.assertFalse(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,8,1), null));
		//date2<dateFrame1
		Assert.assertFalse(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,6,1), new LocalDate(2011,6,30)));
		//date1>dateFrame2
		Assert.assertFalse(DateTool.isProrata(new LocalDate(2011,7,1), new LocalDate(2011,7,15), new LocalDate(2011,8,1), new LocalDate(2011,8,30)));

	}

	@Test
	public void testGetNbFullMonth() {
		Assert.assertEquals(1, DateTool.days360MonthsBetween(new LocalDate(2011,1,1), new LocalDate(2011,2,5)));
		Assert.assertEquals(0, DateTool.days360MonthsBetween(new LocalDate(2011,1,1), new LocalDate(2011,1,25)));
		Assert.assertEquals(1, DateTool.days360MonthsBetween(new LocalDate(2011,12,15), new LocalDate(2012,1,25)));
		Assert.assertEquals(1, DateTool.days360MonthsBetween(new LocalDate(2011,12,15), new LocalDate(2012,1,15)));
		Assert.assertEquals(1, DateTool.days360MonthsBetween(new LocalDate(2011,12,15), new LocalDate(2012,1,14)));
		Assert.assertEquals(0, DateTool.days360MonthsBetween(new LocalDate(2011,12,15), new LocalDate(2012,1,13)));
		Assert.assertEquals(5, DateTool.days360MonthsBetween(new LocalDate(2011,10,7), new LocalDate(2012,3,9)));
		Assert.assertEquals(1, DateTool.days360MonthsBetween(new LocalDate(2011,1,31), new LocalDate(2011,2,28)));
		Assert.assertEquals(1, DateTool.days360MonthsBetween(new LocalDate(2011,3,31), new LocalDate(2011,4,30)));
		Assert.assertEquals(-5, DateTool.days360MonthsBetween(new LocalDate(2012,3,9), new LocalDate(2011,10,7)));
		Assert.assertEquals(-1, DateTool.days360MonthsBetween(new LocalDate(2011,4,30), new LocalDate(2011,3,31)));
	}

	@Test
	public void testNextOccurency() {
		Assert.assertEquals(new LocalDate(2010,11,9), DateTool.nextOccurency(new LocalDate(2010,10,7), new LocalDate(2011,3,9), 2));
		Assert.assertEquals(new LocalDate(2010,11,9), DateTool.nextOccurency(new LocalDate(2010,10,7), new LocalDate(2011,5,9), 2));
		Assert.assertEquals(new LocalDate(2010,8,31), DateTool.nextOccurency(new LocalDate(2010,8,7), new LocalDate(2011,4,30), 1));
		Assert.assertEquals(new LocalDate(2010,5,9), DateTool.nextOccurency(new LocalDate(2010,3,9), new LocalDate(2011,3,9), 2));
		

	}
	
	@Test
	public void testLastOccurency() {
		Assert.assertEquals(new LocalDate(2011,3,9), DateTool.lastOccurency(new LocalDate(2010,11,9), new LocalDate(2011,5,9), 4));
		Assert.assertEquals(new LocalDate(2011,7,9), DateTool.lastOccurency(new LocalDate(2010,11,9), new LocalDate(2011,9,9), 4));
		Assert.assertEquals(new LocalDate(2011,7,9), DateTool.lastOccurency(new LocalDate(2010,11,9), new LocalDate(2011,10,9), 4));
		Assert.assertEquals(new LocalDate(2011,1,9), DateTool.lastOccurency(new LocalDate(2010,11,9), new LocalDate(2011,1,9), 2));
		Assert.assertEquals(new LocalDate(2011,7,31), DateTool.lastOccurency(new LocalDate(2007,4,30), new LocalDate(2011,8,6), 1));

	}

}

