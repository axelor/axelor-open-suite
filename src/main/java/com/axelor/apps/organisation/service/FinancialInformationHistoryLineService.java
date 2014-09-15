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
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;

import org.joda.time.LocalDateTime;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.organisation.db.FinancialInformationHistoryLine;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.repo.FinancialInformationHistoryLineRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;


public class FinancialInformationHistoryLineService extends FinancialInformationHistoryLineRepository{

	private LocalDateTime todayTime;
	private User user;

	@Inject
	public FinancialInformationHistoryLineService(UserService userService) {
		
		this.todayTime = GeneralService.getTodayDateTime().toLocalDateTime();
		this.user = userService.getUser();
		
	}
	
	
	public FinancialInformationHistoryLine createFinancialInformationHistoryLine(Task task, BigDecimal turnover, BigDecimal cost, BigDecimal margin)  {
		
		FinancialInformationHistoryLine financialInformationHistoryLine = new FinancialInformationHistoryLine();
		financialInformationHistoryLine.setTask(task);
		financialInformationHistoryLine.setUser(user);
		financialInformationHistoryLine.setDateT(todayTime);
		financialInformationHistoryLine.setTurnover(turnover);
		financialInformationHistoryLine.setCost(cost);
		financialInformationHistoryLine.setMargin(margin);
		
		return financialInformationHistoryLine;
	}
	
}
