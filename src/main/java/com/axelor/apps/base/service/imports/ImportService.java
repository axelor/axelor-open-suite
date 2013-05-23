package com.axelor.apps.base.service.imports;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.axelor.apps.base.db.IImports;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.data.csv.CSVImporter;
import com.axelor.data.xml.XMLImporter;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class ImportService {
	
	@Inject
	private Injector injector;
	
	public String importer(String type, final String configPath, final String path) throws AxelorException, IOException, SQLException  {
		
		File folder = new File(path);
		File configFile = new File(configPath);
		
		String log = "";
		
		if (type != null && !type.equals(IImports.BDD) && !folder.exists()) {
			throw new AxelorException(String.format("%s :\n Erreur : Dossier inacessible.",
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		} else if (!configFile.exists()) {
			throw new AxelorException(String.format("%s :\n Erreur : Fichier de mapping inacessible.",
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		
			
		Injector injector = Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("axelor.data.config")).to(configPath);
				bindConstant().annotatedWith(Names.named("axelor.data.dir")).to(path);
			}
			
		});
		
		if(type != null && type.equals(IImports.XML)){
			
			XMLImporter importer = injector.getInstance(XMLImporter.class);
			
			importer.run(null);
		}
		else if(type != null && type.equals(IImports.CSV)){
			
			CSVImporter importer = injector.getInstance(CSVImporter.class);
			
			importer.run(null);
		}
		
		
		return log;
	}

}
