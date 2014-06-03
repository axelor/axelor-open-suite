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
