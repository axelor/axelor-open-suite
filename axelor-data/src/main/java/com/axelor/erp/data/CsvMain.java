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
package com.axelor.erp.data;

import java.io.IOException;

import com.axelor.data.Launcher;
import com.axelor.data.csv.CSVImporter;
import com.google.inject.AbstractModule;

/**
 * Main CSV importer class.
 * Just need to configure arguments in method run()
 * 
 * @author axelor
 *
 */
public class CsvMain extends Launcher {

	@Override
	protected AbstractModule createModule() {
		return new MyModule(CSVImporter.class);
	}
	
	/**
	 * Launch CSV import.
	 * 
	 * Options :
	 *  <ul>
	 *  <li>-c : data binding configuration file</li>
	 *  <li>-d : location of data directory</li>
	 *  </ul>
	 *  
	 * @param args
	 * 		Arguments
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		CsvMain launcher = new CsvMain();
		launcher.run("-c", "src/main/resources/config_files/csv-config.xml", "-d", "src/main/resources/data/base/");
	}
	
}

