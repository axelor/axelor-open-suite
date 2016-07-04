package com.axelor.studio.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import com.axelor.data.ImportTask;
import com.axelor.data.xml.XMLImporter;
import com.axelor.studio.service.template.DataXmlCreator;
import com.axelor.studio.service.template.StudioTemplateService;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModules({ TestModule.class })
public class TestData{
	
	@Inject
	DataXmlCreator dxc;
	
	@Inject
	private StudioTemplateService manager;
	
//	@Test
	public void dataExport() throws ParserConfigurationException, SAXException, IOException{
		List<String> models = new ArrayList<String>();
		models.add("MetaSelect,self.name in (select metaSelect.name from MetaField where customised = true)");
		models.add("MetaModel,customised = true");
		models.add("ViewEditor,viewType != 'dashboard'");
		models.add("ViewEditor,viewType = 'dashboard'");
//		models.add(new String[]{"MenuEditor"});
//		models.add(new String[]{"Wkf"});
//		models.add(new String[]{"WkfTransition"});
//		models.add(new String[]{"WkfNode"});
		dxc.createXml(models, new File("/home/axelor/Desktop/studio-data.xml"));
		
	}
	
	@Test
	public void testManager(){
		manager.importXml(new File("/home/axelor/studio/studio-app/modules/axelor-studio/src/main/resources/import-config/studio-config.xml"), 
				  new File("/home/axelor/Downloads/axelor-custom.xml"));
	}
	
//	@Test
	public void testDataImport(){
		
		XMLImporter importer = new XMLImporter("/home/axelor/studio/studio-app/modules/axelor-studio/src/main/resources/import-config/input-config.xml");
		importer.run(new ImportTask() {
			@Override
			public void configure() throws IOException {
				input("workflow.xml", new File("/home/axelor/studio/studio-app/modules/axelor-studio/src/main/resources/data-demo/input/workflow.xml"));
			}
			
		});
		
	}
	
//	@Test
	public void testEnv(){
		 System.out.println(System.getProperty("catalina.base"));
	}
	
}
