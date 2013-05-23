package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.AxelorSettings
import com.axelor.apps.account.db.Irrecoverable
import com.axelor.apps.account.service.IrrecoverableService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
class IrrecoverableController {

	@Inject 
	private IrrecoverableService is
	
	def void getIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		Irrecoverable irrecoverable = request.context as Irrecoverable
		irrecoverable = Irrecoverable.find(irrecoverable.id)
		
		try  {
			
			is.getIrrecoverable(irrecoverable)
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }	
		
	}
	
	def void createIrrecoverableReport(ActionRequest request, ActionResponse response)  {
		
		Irrecoverable irrecoverable = request.context as Irrecoverable
		irrecoverable = Irrecoverable.find(irrecoverable.id)
		
		try  {
			
			is.createIrrecoverableReport(irrecoverable)
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
	def void passInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		Irrecoverable irrecoverable = request.context as Irrecoverable
		irrecoverable = Irrecoverable.find(irrecoverable.id)
		
		try  {
			is.passInIrrecoverable(irrecoverable)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	
	def void printIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		Irrecoverable irrecoverable = request.context as Irrecoverable
		
		if(irrecoverable.exportTypeSelect == null){
			
			response.flash= "Veuillez selectionner un type d'impression"
			
		} else {
		
			AxelorSettings gieSettings = AxelorSettings.get()
			StringBuilder url = new StringBuilder()
			url.append("${gieSettings.get('gie.report.engine', '')}/frameset?__report=report/Irrecoverable.rptdesign&__format=${irrecoverable.exportTypeSelect}&IrrecoverableID=${irrecoverable.id}${gieSettings.get('gie.report.engine.datasource')}")
			
			log.debug("URL : {}", url)
			
			response.view = [
				"title": "Passage en irrécouvrable ${irrecoverable.name}",
				"resource": url,
				"viewType": "html"
			]
			
		}
		
	}

				
	
}
