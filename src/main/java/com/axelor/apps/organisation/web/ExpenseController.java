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
package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.organisation.db.Expense;
import com.axelor.apps.organisation.db.ExpenseLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;

public class ExpenseController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExpenseController.class);
	
	public void checkValidationStatus(ActionRequest request, ActionResponse response) {
	
		Expense expense = request.getContext().asType(Expense.class);
		List<ExpenseLine> list = expense.getExpenseLineList();
		boolean checkFileReceived = false;
		
		if(list != null && !list.isEmpty()) {
			for(ExpenseLine expenseLine : list) {
				if(expenseLine.getFileReceived() == 2) {
					checkFileReceived = true;
					break;
				}
			}
		}
		if ((list != null && list.isEmpty()) || checkFileReceived) {
			response.setValue("validationStatusSelect", 2);
			response.setFlash("All expenses proof haven't been provided (file received for each expense line).");
		}
		else {
			response.setValue("validationStatusSelect", 1);
		}
	}
	
	/**
	 * Fonction appeler par le notes de frais
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printExpenses(ActionRequest request, ActionResponse response) {


		Expense expense = request.getContext().asType(Expense.class);

		StringBuilder url = new StringBuilder();

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(
				new ReportSettings(IReport.EXPENSE)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("ExpenseId", expense.getId().toString())
				.getUrl());
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Name "+expense.getUserInfo().getFullName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
}
