package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.account.service.MoveLineReportService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MoveLineReportController {

	@Inject
	private Injector injector;
	
	private static final Logger LOG = LoggerFactory.getLogger(MoveLineReport.class);
	
	public void searchMoveLine(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		
		MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class);
		
		try  {
			if (moveLineReport.getRef() == null) {
				
				String seq = mlrs.getSequence(moveLineReport);
				
				List<MoveLine> moveLineList = mlrs.getMoveLineList(moveLineReport);
				response.setValue("moveLineSet", moveLineList);
				response.setValue("ref", seq);				
			}
			else  {
				
				List<MoveLine> moveLineList = mlrs.getMoveLineList(moveLineReport);
				response.setValue("moveLineSet", moveLineList);
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void getSequence(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);

		try  {
			if (moveLineReport.getRef() == null) {
				
				MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class);
				
				String seq = mlrs.getSequence(moveLineReport);
				response.setValue("ref", seq);				
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}
	
	public void getJournalType(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		
		MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class);
		
		try  {
			
			JournalType journalType = mlrs.getJournalType(moveLineReport);
			if(journalType != null)  {
				String domainQuery = "self.type.id = "+journalType.getId();
				response.setAttr("journal", "domain", domainQuery);
				// Groovy:
				//response.attrs = [
				//					"journal": ["domain":domainQuery]
				//				   ]		
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}
	
	public void getAccount(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		
		MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class);
		
		try  {
			Account account = mlrs.getAccount(moveLineReport);
			LOG.debug("Compte : {}", account);
			response.setValue("account", account);			
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}
	
	public void getReload(ActionRequest request, ActionResponse response) {
		
		response.setReload(true);
	}
	
	public void replayExport(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		
		moveLineReport = MoveLineReport.find(moveLineReport.getId());
		
		MoveLineExportService mles = injector.getInstance(MoveLineExportService.class);
		
		try {
			String msgExport = "Veuillez configurer le chemin où le fichier exporté devra être stocké pour la société "+moveLineReport.getCompany().getName();
			
			if(moveLineReport.getTypeSelect()==6){
				
				if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect6(moveLineReport, true); }
				else { response.setFlash(msgExport); }			
			}
			else if(moveLineReport.getTypeSelect()==7){
				
				if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect7(moveLineReport, true); }
				else { response.setFlash(msgExport); }	
			}
			else if(moveLineReport.getTypeSelect()==8){
				
				if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect8(moveLineReport, true); }
				else { response.setFlash(msgExport); }
			}
			else if(moveLineReport.getTypeSelect()==9){
				
				if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect9(moveLineReport, true); }
				else { response.setFlash(msgExport); }
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}
	
	public void printExportMoveLine(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		
		moveLineReport = MoveLineReport.find(moveLineReport.getId());
		
		try {
			
			LOG.debug("Type selected : {}" , moveLineReport.getTypeSelect());
			String msgExport = "Veuillez configurer le chemin où le fichier exporté devra être stocké pour la société "+moveLineReport.getCompany().getName();
			
			MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class);
			
			if (moveLineReport.getRef() == null) {
				
				String seq = mlrs.getSequence(moveLineReport);
				mlrs.setSequence(moveLineReport, seq);
			}
			
			mlrs.setStatus(moveLineReport);
			
			if(moveLineReport.getTypeSelect() >= 6 && moveLineReport.getTypeSelect() <= 9) {
				
				MoveLineExportService mles = injector.getInstance(MoveLineExportService.class);
				
				if(moveLineReport.getTypeSelect()==6){
					
					if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect6(moveLineReport, false); }
					else { response.setFlash(msgExport); }
				}
				else if(moveLineReport.getTypeSelect()==7){
					
					if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect7(moveLineReport, false); }
					else { response.setFlash(msgExport); }
				}
				else if(moveLineReport.getTypeSelect()==8){
					
					if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect8(moveLineReport, false); }
					else { response.setFlash(msgExport); }
				}
				else if(moveLineReport.getTypeSelect()==9){
					
					if(moveLineReport.getCompany().getExportPath()!=null) { mles.exportMoveLineTypeSelect9(moveLineReport, false); }
					else { response.setFlash(msgExport); }
				}
			}
			else {
				
				if (moveLineReport.getId() != null){
					
					StringBuilder url = new StringBuilder();
					
					if(moveLineReport.getExportTypeSelect() == null || moveLineReport.getExportTypeSelect().isEmpty() || moveLineReport.getTypeSelect() == 0){
						response.setFlash("Veuillez selectionner un type d'export");
						response.setReload(true);
					} else {
					
					    mlrs.setPublicationDateTime(moveLineReport);
					
						int typeSelect = moveLineReport.getTypeSelect();
						
						AxelorSettings axelorSettings = AxelorSettings.get();
						url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/MoveLineReportType"+typeSelect+".rptdesign&__format="+moveLineReport.getExportTypeSelect()+"&MoveLineReportId="+moveLineReport.getId()+"&code_taxe=${taxCode}&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
						//url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/MoveLineReportType${typeSelect}.rptdesign&__format=${moveLineReport.exportTypeSelect}&MoveLineReportId=${moveLineReport.id}&code_taxe=${taxCode}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")
						
						LOG.debug("URL : {}", url);
						
						Map<String,Object> viewMap = new HashMap<String,Object>();
						viewMap.put("title", "Reporting comptable "+moveLineReport.getRef());
						viewMap.put("resource", url);
						viewMap.put("viewType", "html");
						response.setView(viewMap);						
					}			
				}
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }	
	}
}
