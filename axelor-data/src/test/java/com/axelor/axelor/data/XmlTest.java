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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.data.ImportException;
import com.axelor.data.ImportTask;
import com.axelor.data.xml.XMLImporter;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Unit XML import test class
 * 
 * @author axelor
 *
 */
@RunWith(GuiceRunner.class)
@GuiceModules(MyTestModule.class)
public class XmlTest {
	
	@Inject
    Injector injector;
    
    @Test
    public void test() throws FileNotFoundException {
        XMLImporter importer = new XMLImporter(injector, "data/xml-config.xml");
        
        importer.runTask(new ImportTask() {
			
			@Override
			public void configure() throws IOException {
				input("[me.id]", new File("data/xml/test.xml"));
			}
			
			@Override
			public boolean handle(ImportException exception) {
                System.err.println("Import error : \n");
                exception.printStackTrace();
                return true;
            }
		});
    }
}
