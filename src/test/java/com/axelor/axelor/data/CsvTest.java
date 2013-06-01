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
