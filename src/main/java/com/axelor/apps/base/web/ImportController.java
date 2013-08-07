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
