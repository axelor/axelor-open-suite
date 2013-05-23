package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.AxelorSettings
import com.axelor.apps.account.db.Account
import com.axelor.apps.account.db.Journal
import com.axelor.apps.account.db.JournalType
import com.axelor.apps.account.db.MoveLine
import com.axelor.apps.account.db.MoveLineReport
import com.axelor.apps.account.service.MoveLineExportService
import com.axelor.apps.account.service.MoveLineReportService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Injector;

@Slf4j
public class MoveLineReportController {
	
	@Inject
	private Injector injector
	
	def void searchMoveLine (ActionRequest request, ActionResponse response)  {
		
		MoveLineReport moveLineReport = request.context as MoveLineReport
		
		MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class)
		
		try  {
			if (moveLineReport.ref == null) {
				
				String seq = mlrs.getSequence(moveLineReport)
				
				List<MoveLine> moveLineList = mlrs.getMoveLineList(moveLineReport)
				response.values = [
					"moveLineSet" : moveLineList,
					"ref" : seq
				]
				
			}
			else  {
				
				List<MoveLine> moveLineList = mlrs.getMoveLineList(moveLineReport)
				response.values = [
					"moveLineSet" : moveLineList
				]
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	def void getSequence (ActionRequest request, ActionResponse response)  {
		
		MoveLineReport moveLineReport = request.context as MoveLineReport

				try  {
			
			if (moveLineReport.ref == null) {
				
				MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class)
				
				String seq = mlrs.getSequence(moveLineReport)
				
				response.values = [
					"ref" : seq
				]
				
			}
			
		}
		catch(Exception e) { TraceBackService.trace(response, e) }
	}
	
	
	def void getJournalType (ActionRequest request, ActionResponse response)  {
		
		MoveLineReport moveLineReport = request.context as MoveLineReport
		
		MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class)
		
		try  {
			
			JournalType journalType = mlrs.getJournalType(moveLineReport)
			if(journalType != null)  {
				String domainQuery = "self.type.id = "+journalType.id
				response.attrs = [
					"journal": ["domain":domainQuery]
				   ]
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e) }
	}
	
	
	def void getAccount (ActionRequest request, ActionResponse response)  {
		
		MoveLineReport moveLineReport = request.context as MoveLineReport
		
		MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class)
		
		try  {
			
			Account account = mlrs.getAccount(moveLineReport)
			log.debug("Compte : {}", account)
			response.values = [
				"account" : account
			]
			
		}
		catch(Exception e) { TraceBackService.trace(response, e) }
	}	
	
	def void getReload (ActionRequest request, ActionResponse response)  {
		
		response.reload = true
	}
	
	
	def void replayExport (ActionRequest request, ActionResponse response)  {
		
		MoveLineReport moveLineReport = request.context as MoveLineReport
		
		moveLineReport = MoveLineReport.find(moveLineReport.id)
		
		MoveLineExportService mles = injector.getInstance(MoveLineExportService.class)
		
		try {
			
			String msgExport = "Veuillez configurer le chemin où le fichier exporté devra être stocké pour la société ${moveLineReport.company.name}"
			
			if(moveLineReport.typeSelect==6){
				
				if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect6(moveLineReport, true) }
				else { response.flash = msgExport }
				
			}
			else if(moveLineReport.typeSelect==7){
				
				if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect7(moveLineReport, true) }
				else { response.flash = msgExport }
				
			}
			else if(moveLineReport.typeSelect==8){
				
				if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect8(moveLineReport, true) }
				else { response.flash = msgExport }
				
			}
			else if(moveLineReport.typeSelect==9){
				
				if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect9(moveLineReport, true) }
				else { response.flash = msgExport }
				
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e) }
		
	}
	
	
	def printExportMoveLine(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.context as MoveLineReport
		
		moveLineReport = MoveLineReport.find(moveLineReport.id)
		
		try {
			
			log.debug("Type selected : {}" , moveLineReport.typeSelect)
			String msgExport = "Veuillez configurer le chemin où le fichier exporté devra être stocké pour la société ${moveLineReport.company.name}"
			
			MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class)
			
			if (moveLineReport.ref == null) {
				
				String seq = mlrs.getSequence(moveLineReport)
				
				mlrs.setSequence(moveLineReport, seq)
				
			}
			
			mlrs.setStatus(moveLineReport)
			
			if(moveLineReport.typeSelect >= 6 && moveLineReport.typeSelect <= 9)  {
				
				MoveLineExportService mles = injector.getInstance(MoveLineExportService.class)
				
				if(moveLineReport.typeSelect==6){
					
					if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect6(moveLineReport, false) }
					else { response.flash = msgExport }
					
				}
				else if(moveLineReport.typeSelect==7){
					
					if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect7(moveLineReport, false) }
					else { response.flash = msgExport }
					
				}
				else if(moveLineReport.typeSelect==8){
					
					if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect8(moveLineReport, false) }
					else { response.flash = msgExport }
					
				}
				else if(moveLineReport.typeSelect==9){
					
					if(moveLineReport.company.exportPath!=null) { mles.exportMoveLineTypeSelect9(moveLineReport, false) }
					else { response.flash = msgExport }
					
				}
			}
			else {
				
				if (moveLineReport.id != null){
					
					StringBuilder url = new StringBuilder()
					
					if(moveLineReport.exportTypeSelect == null || moveLineReport.exportTypeSelect.isEmpty() || moveLineReport.typeSelect == 0){
						response.flash= "Veuillez selectionner un type d'export"	
						response.reload = true
					} else {
					
					    mlrs.setPublicationDateTime(moveLineReport)
					
						int typeSelect = moveLineReport.typeSelect
						
						AxelorSettings axelorSettings = AxelorSettings.get()
						url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/MoveLineReportType${typeSelect}.rptdesign&__format=${moveLineReport.exportTypeSelect}&MoveLineReportId=${moveLineReport.id}&code_taxe=${taxCode}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")
						
						log.debug("URL : {}", url)
						
						response.view = [
							"title": "Reporting comptable ${moveLineReport.ref}",
							"resource": url,
							"viewType": "html"
						]
						
					}	
					
				}
				
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e) }	
	}
}