/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.organisation.service;

import java.util.ArrayList;

import com.axelor.apps.organisation.db.FinancialInformationHistoryLine;
import com.axelor.apps.organisation.db.Task;
import com.google.inject.Inject;


public class FinancialInformationHistoryService {

	@Inject
	private FinancialInformationHistoryLineService financialInformationHistoryLineService;
	
	
	public void addFinancialInformationHistoryLine(Task task, FinancialInformationHistoryLine financialInformationHistoryLine)  {
		
		if(task.getFinancialInformationHistoryLineList() == null)  {
			task.setFinancialInformationHistoryLineList(new ArrayList<FinancialInformationHistoryLine>());
		}
		task.getFinancialInformationHistoryLineList().add(financialInformationHistoryLine);
	}
	
	
	public void updateFinancialInformationInitialEstimatedHistory(Task task)  {
		
		this.addFinancialInformationHistoryLine(
				task, 
				financialInformationHistoryLineService.createFinancialInformationHistoryLine(
						task, 
						task.getInitialEstimatedTurnover(), 
						task.getInitialEstimatedCost(), 
						task.getInitialEstimatedMargin()));
	}
	
}
