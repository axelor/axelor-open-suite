/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.test;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.apps.base.test.TestModule;
//import com.axelor.csv.script.ImportDate;
import com.axelor.csv.script.ImportDateTime;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
@RunWith(GuiceRunner.class)
@GuiceModules(TestModule.class)
public class TestImportDateTime {
	@Test
	public void testDateTimeImport(){
//		ImportDate idd = new ImportDate();
		ImportDateTime idt = new ImportDateTime();
//		System.out.println("Groovy\n"+idt.importDate("TODAY[-2y]"));
		System.out.println("JAVA \n"+idt.importDateTime("NOW[-2y]"));
//		System.out.println(idt.importDateTime("TODAY[2013y-2M] 10:00:10"));
	}
}
