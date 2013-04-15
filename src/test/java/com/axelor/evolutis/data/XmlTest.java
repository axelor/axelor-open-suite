package com.axelor.evolutis.data;

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
				input("[me.id]", new File("data/xml/ME_MOD_C3.xml"));
				
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
