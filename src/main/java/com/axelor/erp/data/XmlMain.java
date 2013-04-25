package com.axelor.erp.data;

import java.io.IOException;

import com.axelor.data.Launcher;
import com.axelor.data.xml.XMLImporter;
import com.google.inject.AbstractModule;

/**
 * Main XML importer class.
 * Just need to configure arguments in method run()
 * 
 * @author axelor
 *
 */
public class XmlMain extends Launcher {

	@Override
	protected AbstractModule createModule() {
		return new MyModule(XMLImporter.class);
	}

	/**
	 * Launch XML import.
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
		XmlMain launcher = new XmlMain();
		launcher.run("-c", "data/xml-config.xml", "-d", "data/xml");
	}
}
