/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.db.repo.MoveLineReportRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.account.service.MoveLineReportService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class MoveLineReportController {

	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	MoveLineReportService moveLineReportService;
	
	@Inject
	MoveLineReportRepository  moveLineReportRepo;
	/**
	 * @param request
	 * @param response
	 */
	public void searchMoveLine(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);

		try  {
			moveLineReport = moveLineReportRepo.find(moveLineReport.getId());
			
			String query = moveLineReportService.getMoveLineList(moveLineReport);
			BigDecimal debitBalance = moveLineReportService.getDebitBalance();
			BigDecimal creditBalance = moveLineReportService.getCreditBalance();

			response.setValue("totalDebit", debitBalance);
			response.setValue("totalCredit", creditBalance);
			response.setValue("balance", debitBalance.subtract(creditBalance));
			
			Map<String, Object> view = Maps.newHashMap();
			
			view.put("title", I18n.get(IExceptionMessage.MOVE_LINE_REPORT_3));
			view.put("resource", MoveLine.class.getName());
			view.put("domain", query); 
			
			response.setView(view);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}


	/**
	 * @param request
	 * @param response
	 */
	public void getJournalType(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		
		try  {

			JournalType journalType = Beans.get(MoveLineReportService.class).getJournalType(moveLineReport);
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
			Account account = Beans.get(MoveLineReportService.class).getAccount(moveLineReport);
			logger.debug("Compte : {}", account);
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
		moveLineReport = moveLineReportRepo.find(moveLineReport.getId());		
		MoveLineExportService moveLineExportService = Beans.get(MoveLineExportService.class);
		
		try {
		    moveLineExportService.replayExportMoveLine(moveLineReport);
		}
		catch(Exception e) { TraceBackService.trace(response, e); }
	}

	/**
	 * @param request
	 * @param response
	 */
	public void printExportMoveLine(ActionRequest request, ActionResponse response) {

		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		moveLineReport = moveLineReportRepo.find(moveLineReport.getId());

		try {	
			if(moveLineReport.getExportTypeSelect() == null || moveLineReport.getExportTypeSelect().isEmpty() || moveLineReport.getTypeSelect() == 0) {
				response.setFlash(I18n.get(IExceptionMessage.MOVE_LINE_REPORT_4));
				response.setReload(true);
				return;
			}

			logger.debug("Type selected : {}" , moveLineReport.getTypeSelect());

			if((moveLineReport.getTypeSelect() >= MoveLineReportRepository.EXPORT_ADMINISTRATION
					&& moveLineReport.getTypeSelect() < MoveLineReportRepository.REPORT_ANALYTIC_BALANCE )) {
				
				MoveLineExportService moveLineExportService = Beans.get(MoveLineExportService.class);

				moveLineExportService.exportMoveLine(moveLineReport);
				
			}
			else {

				moveLineReportService.setPublicationDateTime(moveLineReport);
				
				User user = AuthUtils.getUser();
				String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

				String name = I18n.get("Accounting reporting") + " " + moveLineReport.getRef();
				
				String fileLink = ReportFactory.createReport(String.format(IReport.MOVE_LINE_REPORT_TYPE, moveLineReport.getTypeSelect()), name+"-${date}")
						.addParam("MoveLineReportId", moveLineReport.getId())
						.addParam("Locale", language)
						.addFormat(moveLineReport.getExportTypeSelect())
						.toAttach(moveLineReport)
						.generate()
						.getFileLink();

				logger.debug("Printing "+name);
			
				response.setView(ActionView
						.define(name)
						.add("html", fileLink).map());
				
				moveLineReportService.setStatus(moveLineReport);

			}
		}
		catch(Exception e) { TraceBackService.trace(response, e); }	
	}
	
	
	public void showMoveExported(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", I18n.get(IExceptionMessage.MOVE_LINE_REPORT_6));
		mapView.put("resource", Move.class.getName());
		mapView.put("domain", "self.moveLineReport.id = "+moveLineReport.getId());
		response.setView(mapView);		
	}
}
