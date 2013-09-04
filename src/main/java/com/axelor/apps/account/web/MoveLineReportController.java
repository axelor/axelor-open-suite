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

	private static final Logger LOG = LoggerFactory.getLogger(MoveLineReportController.class);

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
		String msgExport = "Veuillez configurer le chemin où le fichier exporté devra être stocké pour la société "+moveLineReport.getCompany().getName();
		
		if(moveLineReport.getCompany().getExportPath() == null) {
			response.setFlash(msgExport);
			return;
		}
		MoveLineExportService mles = injector.getInstance(MoveLineExportService.class);

		try {
			switch(moveLineReport.getTypeSelect()) {
			
				case 6:
					mles.exportMoveLineTypeSelect6(moveLineReport, true);
					break;
				case 7:
					mles.exportMoveLineTypeSelect7(moveLineReport, true);
					break;
				case 8:
					mles.exportMoveLineTypeSelect8(moveLineReport, true);
					break;
				case 9:
					mles.exportMoveLineTypeSelect9(moveLineReport, true);
					break;
				default:
					break;
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}

	public void printExportMoveLine(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		moveLineReport = MoveLineReport.find(moveLineReport.getId());

		try {	
			if(moveLineReport.getExportTypeSelect() == null || moveLineReport.getExportTypeSelect().isEmpty() || moveLineReport.getTypeSelect() == 0) {
				response.setFlash("Veuillez selectionner un type d'export");
				response.setReload(true);
				return;
			}

			LOG.debug("Type selected : {}" , moveLineReport.getTypeSelect());
			String msgExport = "Veuillez configurer le chemin où le fichier exporté devra être stocké pour la société "+moveLineReport.getCompany().getName();

			MoveLineReportService mlrs = injector.getInstance(MoveLineReportService.class);

			if (moveLineReport.getRef() == null) {

				String seq = mlrs.getSequence(moveLineReport);
				mlrs.setSequence(moveLineReport, seq);
			}

			mlrs.setStatus(moveLineReport);

			if(moveLineReport.getTypeSelect() >= 6 && moveLineReport.getTypeSelect() <= 9) {
				
				if(moveLineReport.getCompany().getExportPath() == null) {
					response.setFlash(msgExport);
					return;
				}
				MoveLineExportService mles = injector.getInstance(MoveLineExportService.class);

				switch(moveLineReport.getTypeSelect()) {
					case 6:
						mles.exportMoveLineTypeSelect6(moveLineReport, false);
						break;
					case 7:
						mles.exportMoveLineTypeSelect7(moveLineReport, false);
						break;
					case 8:
						mles.exportMoveLineTypeSelect8(moveLineReport, false);
						break;
					case 9:
						mles.exportMoveLineTypeSelect9(moveLineReport, false);
						break;
					default:
						break;
				}
			}
			else {

				if (moveLineReport.getId() != null) {

					StringBuilder url = new StringBuilder();
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
		catch(Exception e) { TraceBackService.trace(response, e); }	
	}
}
