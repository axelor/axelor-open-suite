package com.axelor.apps.base.web

import groovy.util.logging.Slf4j

import com.axelor.apps.base.db.Import
import com.axelor.apps.base.service.imports.ImportService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class ImportController {
	
	@Inject
	private ImportService is
	
	def void launchImport(ActionRequest request,ActionResponse response){
		
		Import context = request.context as Import
		
		String path = context.path
		String configPath = context.configPath
		String type = context.typeSelect
		
		log.debug("Using {} importer for config file: {} on directory: {}",type,configPath, path)
		
		try{
			
			def log = is.importer(type, configPath, path)
			
			response.flash = "Import terminé"
			
			response.values = [
				"log": log
			]
			
		}
		catch(Exception ex){ 
			TraceBackService.trace(response,ex) 
		}
	}
	
}
