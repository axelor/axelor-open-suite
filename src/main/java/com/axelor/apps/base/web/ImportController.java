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
package com.axelor.apps.base.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Import;
import com.axelor.apps.base.service.imports.ImportService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ImportController {

	@Inject
	private ImportService is;
	
	private static final Logger LOG = LoggerFactory.getLogger(ImportController.class);
	
	public void launchImport(ActionRequest request,ActionResponse response){
		
		Import context = request.getContext().asType(Import.class);
		
		String path = context.getPath();
		String configPath = context.getConfigPath();
		String type = context.getTypeSelect();
		
		LOG.debug("Using {} importer for config file: {} on directory: {}",type,configPath, path);
		
		try{
			String log = is.importer(type, configPath, path);
			
			response.setFlash("Import terminé");
			response.setValue("log", log);			
		}
		catch(Exception ex){ 
			TraceBackService.trace(response,ex);
		}
	}
	
}
