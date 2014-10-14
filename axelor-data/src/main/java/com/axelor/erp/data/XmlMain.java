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
