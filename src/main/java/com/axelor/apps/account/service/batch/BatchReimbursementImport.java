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
package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.ReimbursementImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchReimbursementImport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchReimbursementImport.class);

	private boolean stop = false;
	
	private BigDecimal totalAmount = BigDecimal.ZERO;
	
	private String updateCustomerAccountLog = "";

	
	@Inject
	public BatchReimbursementImport(ReimbursementImportService reimbursementImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		
		super(reimbursementImportService, rejectImportService, batchAccountCustomer);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
		
		company = Company.find(company.getId());
				
		try {
			reimbursementImportService.testCompanyField(company);
		} catch (AxelorException e) {
			TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
			incrementAnomaly();
			stop = true;
		}
		checkPoint();

	}

	@Override
	protected void process() {
		if(!stop)  {
		
			Company company = batch.getAccountingBatch().getCompany();
			
			company = Company.find(company.getId());
			
			AccountConfig accountConfig = company.getAccountConfig();
			
			Map<List<String[]>,String> data = null;
			
			try  {
				data = rejectImportService.getCFONBFileByLot(accountConfig.getReimbursementImportFolderPathCFONB(), accountConfig.getTempReimbImportFolderPathCFONB(),company, 0);

				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Batch d'import des remboursements %s", batch.getId()), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				stop();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Batch d'import des remboursements %s", batch.getId()), e), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());
			
				stop();
			}	
			
			int seq = 1;
			
			int i = 0;
			
			for(List<String[]> rejectList : data.keySet())  {

				LocalDate rejectDate = rejectImportService.createRejectDate(data.get(rejectList));
								
				Move move = this.createMove(company, rejectDate);
				
				for(String[] reject : rejectList)  {
					
					try  {
						
						Reimbursement reimbursement = reimbursementImportService.createReimbursementRejectMoveLine(reject, Company.find(company.getId()), seq, Move.find(move.getId()), rejectDate);
						if(reimbursement != null)  {
							LOG.debug("Remboursement n° {} traité", reimbursement.getRef());
							seq++;
							i++;
							updateReimbursement(reimbursement);
						}
					} catch (AxelorException e) {
						
						TraceBackService.trace(new AxelorException(String.format("Rejet de remboursement %s", reject[1]), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
						
						incrementAnomaly();
						
					} catch (Exception e) {
						
						TraceBackService.trace(new Exception(String.format("Rejet de remboursement %s", reject[1]), e), IException.REIMBURSEMENT, batch.getId());
						
						incrementAnomaly();
						
						LOG.error("Bug(Anomalie) généré(e) pour le rejet de remboursement {}", reject[1]);
						
					} finally {
						
						if (i % 10 == 0) { JPA.clear(); }
			
					}	
				}
				
				this.validateMove(move, rejectDate, seq);
				
			}
				
			updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(company);
		}
	}
	
	
	public Move createMove(Company company, LocalDate rejectDate)  {
		
		Move move = null;
		
		try  {
			move = reimbursementImportService.createMoveReject(Company.find(company.getId()), rejectDate);
			
			
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format("Batch d'import des remboursements %s", batch.getId()), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
			
			incrementAnomaly();
			
			stop();
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format("Batch d'import des remboursements %s", batch.getId()), e), IException.REIMBURSEMENT, batch.getId());
			
			incrementAnomaly();
			
			LOG.error("Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());
		
			stop();
		}	
		
		return move;
	}
	
	
	public void validateMove(Move move, LocalDate rejectDate, int seq)  {
		try  {
			if(seq != 1)  {
				MoveLine oppositeMoveLine = reimbursementImportService.createOppositeRejectMoveLine(Move.find(move.getId()), seq, rejectDate);
				reimbursementImportService.validateMove(Move.find(move.getId()));
				this.totalAmount = this.totalAmount.add(MoveLine.find(oppositeMoveLine.getId()).getDebit());
			}
			else {
				reimbursementImportService.deleteMove(Move.find(move.getId()));
			}
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format("Batch d'import des remboursements %s", batch.getId()), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
			
			incrementAnomaly();
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format("Batch d'import des remboursements %s", batch.getId()), e), IException.REIMBURSEMENT, batch.getId());
			
			incrementAnomaly();
			
			LOG.error("Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());
		
		}
	}
	
	
	
	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {
		String comment = "";
		comment = "Compte rendu de l'import des rejets de remboursement :\n";
		comment += String.format("\t* %s remboursement(s) rejeté(s)\n", batch.getDone());
		comment += String.format("\t* Montant total : %s \n", this.totalAmount);
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());

		comment += String.format("\t* ------------------------------- \n");
		comment += String.format("\t* %s ", updateCustomerAccountLog);
		
		super.stop();
		addComment(comment);
		
	}

}
