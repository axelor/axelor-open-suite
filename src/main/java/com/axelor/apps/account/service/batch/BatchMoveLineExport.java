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
package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchMoveLineExport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchMoveLineExport.class);

	private boolean stop = false;
	
	private long moveLineDone = 0;
	private long moveDone = 0;
	private BigDecimal debit = BigDecimal.ZERO;
	private BigDecimal credit = BigDecimal.ZERO;
	private BigDecimal balance = BigDecimal.ZERO;
	
	@Inject
	public BatchMoveLineExport(MoveLineExportService moveLineExportService) {
		
		super(moveLineExportService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
		
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
				
				moveLineReport = MoveLineReport.find(moveLineReport.getId());
				
				moveLineDone = MoveLine.all().filter("self.move.moveLineReport = ?1", moveLineReport).count();
				moveDone = Move.all().filter("self.moveLineReport = ?1", moveLineReport).count();
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
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une société pour le configurateur de batch %s",
					GeneralService.getExceptionAccountingMsg(), accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
		if(accountingBatch.getEndDate() == null)  {
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une date de fin pour le configurateur de batch %s",
					GeneralService.getExceptionAccountingMsg(), accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
		if(accountingBatch.getMoveLineExportTypeSelect() == null)  {
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer un type d'export pour le configurateur de batch %s",
					GeneralService.getExceptionAccountingMsg(), accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
	}
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = "Compte rendu du batch d'export des écritures :\n";
		comment += String.format("\t* %s (%s) Lignes d'écritures (Ecritures) exportées\n", moveLineDone, moveDone);
		comment += String.format("\t* Débit : %s\n", debit);
		comment += String.format("\t* Crédit : %s\n", credit);
		comment += String.format("\t* Solde : %s\n", balance);
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}

}
