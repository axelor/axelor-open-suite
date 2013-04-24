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
		launcher.run("-c", "src/main/resources/config_files/csv-config.xml", "-d", "src/main/resources/data/koala/");
	}
	
}

