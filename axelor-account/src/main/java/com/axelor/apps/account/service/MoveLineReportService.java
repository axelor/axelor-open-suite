/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import java.math.BigDecimal;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface MoveLineReportService {


	public String getMoveLineList(MoveLineReport moveLineReport) throws AxelorException;
	
	
	public String buildQuery(MoveLineReport moveLineReport) throws AxelorException;



	public String addParams(String paramQuery, Object param);

	public String addParams(String paramQuery);


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setSequence(MoveLineReport moveLineReport, String sequence);

	public String getSequence(MoveLineReport moveLineReport) throws AxelorException;

	public JournalType getJournalType(MoveLineReport moveLineReport) throws AxelorException;

	public Account getAccount(MoveLineReport moveLineReport);


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setStatus(MoveLineReport moveLineReport);

	/**
	 * @param moveLineReport
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setPublicationDateTime(MoveLineReport moveLineReport);

	/**
	 * @param queryFilter
	 * @return
	 */
	public BigDecimal getDebitBalance();


	/**
	 * @param queryFilter
	 * @return
	 */
	public BigDecimal getCreditBalance();


	public BigDecimal getDebitBalanceType4();


	public BigDecimal getCreditBalance(MoveLineReport moveLineReport, String queryFilter);

	public BigDecimal getCreditBalanceType4();
}
