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
package com.axelor.erp.data;

import com.axelor.auth.AuthModule;
import com.axelor.data.Commander;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JpaModule;
import com.axelor.db.Model;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * Main importer class. 
 * Can be launched by the script axelor-data.sh
 * 
 * Use to import only CSV files.
 * 
 * @author axelor
 *
 */
public class Main {

	/**
	 * Main method.
	 * Can be launched by the script axelor-data.sh
	 * 
	 * @param args
	 * 		Arguments
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		final Commander cmd = new Commander();
		try {
			if (args == null || args.length == 0)
				throw new Exception();
			cmd.parse(args);
			if (!cmd.getDataDir().isDirectory())
				throw new Exception("invalid data directory");
			if (!cmd.getConfig().isFile())
				throw new Exception("invalid config file");
		} catch (Exception e) {
			String message = e.getMessage();
			if (!Strings.isNullOrEmpty(message))
				System.err.println(e.getMessage());
			Commander.usage();
			return;
		}
		
		if (cmd.getShowHelp() == Boolean.TRUE) {
			Commander.usage();
			return;
		}
		
		Injector injector = Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				install(new JpaModule("persistenceImportUnit", true, true));
				install(new AuthModule.Simple());
				bindConstant().annotatedWith(Names.named("axelor.data.config")).to(cmd.getConfig().toString());
				bindConstant().annotatedWith(Names.named("axelor.data.dir")).to(cmd.getDataDir().toString());
			}
		});
		
		CSVImporter importer = injector.getInstance(CSVImporter.class);
		
		importer.addListener(new Listener() {
			
			@Override
			public void imported(Model bean) {
			}

			@Override
			public void imported(Integer total, Integer success) {
				System.out.println("/*** Records total : "+total);
				System.out.println("/*** Records success : "+success);
			}
			
			@Override
			public void handle(Model bean, Exception e) {
				
			}
		});
		
		importer.run(null);
	}

	
}