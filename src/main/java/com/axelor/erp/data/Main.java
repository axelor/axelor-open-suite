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