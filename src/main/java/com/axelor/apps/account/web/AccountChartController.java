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
package com.axelor.apps.account.web;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountChart;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.service.AccountChartService;
import com.axelor.apps.base.db.Company;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AccountChartController {
	
	@Inject
	AccountChartService acts;
	
	public void installChart(ActionRequest request, ActionResponse response){
		AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
		AccountChart act = AccountChart.find(accountConfig.getAccountChart().getId());
		Company company = Company.find(accountConfig.getCompany().getId());
		accountConfig = AccountConfig.find(accountConfig.getId());
		List<? extends Account> accountList = Account.all_().filter("self.company.id = ?1 AND self.parent != null", company.getId()).fetch();
		if(accountList.isEmpty()){
			if(acts.installAccountChart(act,company,accountConfig))
				response.setFlash("The chart of account has been loaded successfully");
			else
				response.setFlash("Error in account chart import please check log");
			response.setReload(true);
		}
		else 
			response.setFlash("A chart or chart structure of accounts already exists, please delete the hierarchy between accounts in order to import a new chart.");
		
	}
}
