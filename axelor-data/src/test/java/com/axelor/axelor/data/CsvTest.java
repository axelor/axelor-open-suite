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
package com.axelor.axelor.data;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.data.Launcher;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.inject.AbstractModule;

/**
 * Unit CSV import test class
 * 
 * @author axelor
 *
 */
@RunWith(GuiceRunner.class)
@GuiceModules(MyTestModule.class)
public class CsvTest {
			
	static class MyLauncher extends Launcher {

		@Override
		protected AbstractModule createModule() {
			return new MyTestModule();
		}
	}

	@Test
	public void test() throws IOException {
		MyLauncher launcher = new MyLauncher();
		launcher.run("-c", "src/main/resources/config_files/csv-config.xml", "-d", "src/main/resources/data/base/");
	}

}
