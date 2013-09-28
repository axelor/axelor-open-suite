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
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;

import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.organisation.db.FinancialInformationHistoryLine;
import com.axelor.apps.organisation.db.Task;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;


public class FinancialInformationHistoryLineService {

	@Inject
	private UnitConversionService unitConversionService;
	
	private LocalDateTime todayTime;
	private UserInfo user;

	@Inject
	public FinancialInformationHistoryLineService(UserInfoService userInfoService) {
		
		this.todayTime = GeneralService.getTodayDateTime().toLocalDateTime();
		this.user = userInfoService.getUserInfo();
		
	}
	
	
	public FinancialInformationHistoryLine createFinancialInformationHistoryLine(Task task, BigDecimal turnover, BigDecimal cost, BigDecimal margin) throws AxelorException  {
		
		FinancialInformationHistoryLine financialInformationHistoryLine = new FinancialInformationHistoryLine();
		financialInformationHistoryLine.setTask(task);
		financialInformationHistoryLine.setUserInfo(user);
		financialInformationHistoryLine.setDateT(todayTime);
		financialInformationHistoryLine.setTurnover(turnover);
		financialInformationHistoryLine.setCost(cost);
		financialInformationHistoryLine.setMargin(margin);
		
		return financialInformationHistoryLine;
	}
	
}
