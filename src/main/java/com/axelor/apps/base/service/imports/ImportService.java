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
package com.axelor.apps.base.service.imports;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.axelor.apps.base.db.IImports;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

public class ImportService {
	
	@Inject
	private Injector injector;
	
	private String log;
	
	public String importer(String type, final String configPath, final String path) throws AxelorException, IOException, SQLException  {
		
		File folder = new File(path);
		File configFile = new File(configPath);
		log = "";
		
		
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
				bindConstant().annotatedWith(Names.named("axelor.error.dir")).to("");
			}
			
		});
		
		if(type != null && type.equals(IImports.XML)){
			
			XMLImporter importer = injector.getInstance(XMLImporter.class);
			
			importer.run(null);
		}
		else if(type != null && type.equals(IImports.CSV)){
			
			CSVImporter importer = injector.getInstance(CSVImporter.class);
			
			importer.addListener(new Listener() {
				@Override
				public void imported(Model bean) {
				}

				@Override
				public void imported(Integer total, Integer success) {
					if(log.isEmpty()){
						log = log+"Records total : "+total+"\n";
						log = log+"Records success : "+success+"\n";
					}
				}
				
				@Override
				public void handle(Model bean, Exception e) {
					log = e.getMessage();
				}
			});
			importer.run(null);
		}
		
		
		return log;
	}

}
