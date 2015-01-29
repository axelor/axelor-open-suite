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
package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.account.service.MoveLineReportService;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

public class BatchMoveLineExport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchMoveLineExport.class);

	private boolean stop = false;
	
	private long moveLineDone = 0;
	private long moveDone = 0;
	private BigDecimal debit = BigDecimal.ZERO;
	private BigDecimal credit = BigDecimal.ZERO;
	private BigDecimal balance = BigDecimal.ZERO;
	
	@Inject
	private MoveLineReportService moveLineReportService;
	
	@Inject
	public BatchMoveLineExport(MoveLineExportService moveLineExportService) {
		
		super(moveLineExportService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
		
		super.start();
		
		try {
			
			this.testAccountingBatchField();
			
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.MOVE_LINE_EXPORT_ORIGIN, batch.getId());
			incrementAnomaly();
			stop = true;
		}
		
		checkPoint();

	}

	
	@Override
	protected void process() {
	
		if(!stop)  {
			try  {
				Company company = batch.getAccountingBatch().getCompany();
				LocalDate startDate = batch.getAccountingBatch().getStartDate();
				LocalDate endDate = batch.getAccountingBatch().getEndDate();
				int exportTypeSelect = batch.getAccountingBatch().getMoveLineExportTypeSelect();
				
				MoveLineReport moveLineReport = moveLineExportService.createMoveLineReport(company, exportTypeSelect, startDate, endDate);
				moveLineExportService.exportMoveLine(moveLineReport);
				
				JPA.clear();
				
				moveLineReport = moveLineReportService.find(moveLineReport.getId());
				
				moveLineDone = moveLineService.all().filter("self.move.moveLineReport = ?1", moveLineReport).count();
				moveDone = moveService.all().filter("self.moveLineReport = ?1", moveLineReport).count();
				debit = moveLineReport.getTotalDebit();
				credit = moveLineReport.getTotalCredit();
				balance = moveLineReport.getBalance();
				
				updateMoveLineReport(moveLineReport);
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("%s", e), e, e.getcategory()), IException.MOVE_LINE_EXPORT_ORIGIN, batch.getId());
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("%s", e), e), IException.MOVE_LINE_EXPORT_ORIGIN, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le batch {}", batch.getId());
				
			}
		}
		
	}
	
	
	public void testAccountingBatchField() throws AxelorException  {
		AccountingBatch accountingBatch = batch.getAccountingBatch();
		if(accountingBatch.getCompany() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_MOVELINE_EXPORT_1),
					GeneralServiceAccount.getExceptionAccountingMsg(), accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
		if(accountingBatch.getEndDate() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_MOVELINE_EXPORT_2),
					GeneralServiceAccount.getExceptionAccountingMsg(), accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
		if(accountingBatch.getMoveLineExportTypeSelect() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_MOVELINE_EXPORT_3),
					GeneralServiceAccount.getExceptionAccountingMsg(), accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
	}
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = I18n.get(IExceptionMessage.BATCH_MOVELINE_EXPORT_4);
		comment += String.format("\t* %s (%s)"+I18n.get(IExceptionMessage.BATCH_MOVELINE_EXPORT_5)+"\n", moveLineDone, moveDone);
		comment += String.format("\t* "+I18n.get("Debit")+" : %s\n", debit);
		comment += String.format("\t* "+I18n.get("Credit")+" : %s\n", credit);
		comment += String.format("\t* "+I18n.get("Balance")+" : %s\n", balance);
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}

}
