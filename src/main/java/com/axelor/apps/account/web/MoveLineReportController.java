/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.account.service.MoveLineReportService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MoveLineReportController {

	@Inject
	private Provider<MoveLineReportService> moveLineReportProvider;
	
	@Inject
	private Provider<MoveLineExportService> moveLineExportProvider;

	private static final Logger LOG = LoggerFactory.getLogger(MoveLineReportController.class);

	/**
	 * @param request
	 * @param response
	 */
	public void searchMoveLine(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);

		try  {
			MoveLineReportService moveLineReportService = moveLineReportProvider.get();
			
			String queryFilter = moveLineReportService.getMoveLineList(MoveLineReport.find(moveLineReport.getId()));
			BigDecimal debitBalance = moveLineReportService.getDebitBalance(queryFilter);
			BigDecimal creditBalance = moveLineReportService.getCreditBalance(queryFilter);
			
			if (moveLineReport.getRef() == null) {
				response.setValue("ref", moveLineReportService.getSequence(moveLineReport));
			}

			response.setValue("totalDebit", debitBalance);
			response.setValue("totalCredit", creditBalance);
			response.setValue("balance", debitBalance.subtract(creditBalance));
			
			Map<String, Object> view = Maps.newHashMap();
			
			view.put("title", "Lignes d'écritures récupérées");
			view.put("resource", MoveLine.class.getName());
			view.put("domain", queryFilter);
			
			response.setView(view);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}

	/**
	 * @param request
	 * @param response
	 */
	public void getSequence(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);

		try  {
			if (moveLineReport.getRef() == null) {

				response.setValue(
						"ref", 
						moveLineReportProvider.get().getSequence(moveLineReport));				
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}

	/**
	 * @param request
	 * @param response
	 */
	public void getJournalType(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);

		try  {

			JournalType journalType = moveLineReportProvider.get().getJournalType(moveLineReport);
			if(journalType != null)  {
				String domainQuery = "self.type.id = "+journalType.getId();
				response.setAttr("journal", "domain", domainQuery);
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}

	/**
	 * @param request
	 * @param response
	 */
	public void getAccount(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);

		try  {
			Account account = moveLineReportProvider.get().getAccount(moveLineReport);
			LOG.debug("Compte : {}", account);
			response.setValue("account", account);			
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}

	/**
	 * @param request
	 * @param response
	 */
	public void getReload(ActionRequest request, ActionResponse response) {

		response.setReload(true);
	}

	/**
	 * @param request
	 * @param response
	 */
	public void replayExport(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		moveLineReport = MoveLineReport.find(moveLineReport.getId());		
		
		try {
			switch(moveLineReport.getTypeSelect()) {
			
				case 6:
					moveLineExportProvider.get().exportMoveLineTypeSelect6(moveLineReport, true);
					break;
				case 7:
					moveLineExportProvider.get().exportMoveLineTypeSelect7(moveLineReport, true);
					break;
				case 8:
					moveLineExportProvider.get().exportMoveLineTypeSelect8(moveLineReport, true);
					break;
				case 9:
					moveLineExportProvider.get().exportMoveLineTypeSelect9(moveLineReport, true);
					break;
				default:
					break;
			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}

	/**
	 * @param request
	 * @param response
	 */
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

			MoveLineReportService moveLineReportService = moveLineReportProvider.get();
			
			if (moveLineReport.getRef() == null) {

				String seq = moveLineReportService.getSequence(moveLineReport);
				moveLineReportService.setSequence(moveLineReport, seq);
			}

			moveLineReportService.setStatus(moveLineReport);

			if(moveLineReport.getTypeSelect() >= 6 && moveLineReport.getTypeSelect() <= 9) {
				
				MoveLineExportService moveLineExportService = moveLineExportProvider.get();

				switch(moveLineReport.getTypeSelect()) {
					case 6:
						moveLineExportService.exportMoveLineTypeSelect6(moveLineReport, false);
						break;
					case 7:
						moveLineExportService.exportMoveLineTypeSelect7(moveLineReport, false);
						break;
					case 8:
						moveLineExportService.exportMoveLineTypeSelect8(moveLineReport, false);
						break;
					case 9:
						moveLineExportService.exportMoveLineTypeSelect9(moveLineReport, false);
						break;
					default:
						break;
				}
			}
			else {

				if (moveLineReport.getId() != null) {

					StringBuilder url = new StringBuilder();
					moveLineReportService.setPublicationDateTime(moveLineReport);

					url.append(new ReportSettings(String.format(IReport.MOVE_LINE_REPORT_TYPE, moveLineReport.getTypeSelect()), moveLineReport.getExportTypeSelect())
								.addParam("__locale", "fr_FR")
								.addParam("MoveLineReportId", moveLineReport.getId().toString())
								.getUrl());

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
	
	
	public void showMoveExported(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Ecritures exportées");
		mapView.put("resource", Move.class.getName());
		mapView.put("domain", "self.moveLineReport.id = "+moveLineReport.getId());
		response.setView(mapView);		
	}
}
