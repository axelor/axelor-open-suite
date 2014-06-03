/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.script.test;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.apps.base.test.TestModule;
import com.axelor.csv.script.ImportDateTime;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
@RunWith(GuiceRunner.class)
@GuiceModules(TestModule.class)
public class TestImportDateTime {
	@Test
	public void testDateTimeImport(){
		ImportDateTime idt = new ImportDateTime();
		System.out.println(idt.importDate("2013-01-01"));
		System.out.println(idt.importDateTime("2013-05-10 10:00:10"));
	}
}
