/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
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
