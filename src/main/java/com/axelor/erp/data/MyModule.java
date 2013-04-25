package com.axelor.erp.data;

import com.axelor.auth.AuthModule;
import com.axelor.data.Importer;
import com.axelor.db.JpaModule;
import com.google.inject.AbstractModule;

/**
 * This class will define bindings
 * 
 * @author axelor
 *
 */
public class MyModule extends AbstractModule {

	Class<? extends Importer> importer;
	
	public MyModule(Class<? extends Importer> importer) {
		this.importer = importer;
	}
	
	@Override
	protected void configure() {
		install(new JpaModule("persistenceImportUnit", true, true));
		install(new AuthModule.Simple());
		bind(Importer.class).to(importer); 
	}
}
