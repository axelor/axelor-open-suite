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
