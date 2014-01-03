/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.CfonbService;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchReimbursementExport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchReimbursementExport.class);

	private boolean stop = false;
	
	private BigDecimal totalAmount = BigDecimal.ZERO;
	
	private String updateCustomerAccountLog = "";
	
	@Inject
	public BatchReimbursementExport(ReimbursementExportService reimbursementExportService, CfonbService cfonbService, BatchAccountCustomer batchAccountCustomer) {
		
		super(reimbursementExportService, cfonbService, batchAccountCustomer);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
				
		switch (batch.getAccountingBatch().getReimbursementExportTypeSelect()) {
		
		case IAccount.REIMBURSEMENT_EXPORT_GENERATE:
			try {
				this.testAccountingBatchBankDetails(batch.getAccountingBatch());
				reimbursementExportService.testCompanyField(company);
			} catch (AxelorException e) {
				TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
				incrementAnomaly();
				stop = true;
			}
			break;
			
		case IAccount.REIMBURSEMNT_EXPORT_EXPORT:
			try {
				this.testAccountingBatchBankDetails(batch.getAccountingBatch());
				reimbursementExportService.testCompanyField(company);
				cfonbService.testCompanyExportCFONBField(company);
			} catch (AxelorException e) {
				TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
				incrementAnomaly();
				stop = true;
			}
			break;
			
		default:
			TraceBackService.trace(new AxelorException(String.format("Type de donnée inconnu pour le traitement %s", batch.getAccountingBatch().getActionSelect()), IException.INCONSISTENCY));
			incrementAnomaly();
			stop = true;
		}
		
		checkPoint();

	}

	@Override
	protected void process() {
		if(!stop)  {
			Company company = batch.getAccountingBatch().getCompany();
			
			switch (batch.getAccountingBatch().getReimbursementExportTypeSelect()) {
			case IAccount.REIMBURSEMENT_EXPORT_GENERATE:
				
				this.runCreateReimbursementExport(company);
				
				break;
				
			case IAccount.REIMBURSEMNT_EXPORT_EXPORT:
				
				this.runReimbursementExportProcess(company);
				
				updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(Company.find(company.getId()));
				
				break;
			
			default:
				break;
			}
		}
	}
	
	
	public void runCreateReimbursementExport(Company company)  {
		
		List<Reimbursement> reimbursementList = Reimbursement.all().filter("self.status.code != 'rei' AND self.status.code != 'can' AND self.company = ?1", company).fetch();
		
		List<Partner> partnerList = Partner.all().filter("?1 IN self.companySet = ?1", company).fetch();
		
		int i=0;

		for(Reimbursement reimbursement : reimbursementList)  {
			
			LOG.debug("Remboursement n° {}", reimbursement.getRef());
			
			updateReimbursement(Reimbursement.find(reimbursement.getId()));
		}
		
		for(Partner partner : partnerList)  {
			
			try {
				partner = Partner.find(partner.getId());
				
				LOG.debug("Tiers n° {}", partner.getName());
				
				if(reimbursementExportService.canBeReimbursed(partner, Company.find(company.getId())))  {
				
					List<MoveLine> moveLineList = MoveLine.all().filter("self.account.reconcileOk = 'true' AND self.fromSchedulePaymentOk = 'false' " +
							"AND self.move.state = ?1 AND self.amountRemaining > 0 AND self.credit > 0 AND self.partner = ?2 AND self.company = ?3 AND " +
							"self.reimbursementStateSelect = ?4 "
							,IAccount.VALIDATED_MOVE ,Partner.find(partner.getId()), Company.find(company.getId()), IAccount.NULL).fetch();
					
					LOG.debug("Liste des trop perçus : {}", moveLineList);
					
					if(moveLineList != null && moveLineList.size() != 0)  {
						
						Reimbursement reimbursement = reimbursementExportService.runCreateReimbursement(moveLineList, Company.find(company.getId()), Partner.find(partner.getId()));
						if(reimbursement != null)  {
							updateReimbursement(Reimbursement.find(reimbursement.getId()));
							this.totalAmount = this.totalAmount.add(Reimbursement.find(reimbursement.getId()).getAmountToReimburse());
							i++;
						}
					}
				}
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Tiers %s", Partner.find(partner.getId()).getName()), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Tiers %s", Partner.find(partner.getId()).getName()), e), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le tiers {}", Partner.find(partner.getId()).getName());
				
			} finally {
				
				if (i % 10 == 0) { JPA.clear(); }
	
			}
		}
	}
	
	
	public void runReimbursementExportProcess(Company company)  {
		
		int i=0;
		
		// On récupère les remboursements dont les trop perçu ont été annulés
		List<Reimbursement> reimbursementToCancelList = Reimbursement.all()
				.filter("self.company = ?1 and self.status.code = 'val' and self.amountToReimburse = 0", company).fetch();
		
		// On annule les remboursements
		Status statusCan = Status.all().filter("self.code = 'can'").fetchOne();
		for(Reimbursement reimbursement : reimbursementToCancelList)  {
			reimbursement.setStatus(statusCan);
		}
		
		// On récupère les remboursement à rembourser
		List<Reimbursement> reimbursementList = Reimbursement.all()
				.filter("self.company = ?1 and self.status.code = 'val' and self.amountToReimburse > 0", company).fetch();
		
		List<Reimbursement> reimbursementToExport = new ArrayList<Reimbursement>();
		
		for(Reimbursement reimbursement : reimbursementList)  {
			try {
				reimbursement = Reimbursement.find(reimbursement.getId());
				
				if(reimbursementExportService.canBeReimbursed(reimbursement.getPartner(), reimbursement.getCompany()))  {
				
					reimbursementExportService.reimburse(reimbursement, company);
					updateReimbursement(Reimbursement.find(reimbursement.getId()));
					reimbursementToExport.add(reimbursement);
					this.totalAmount = this.totalAmount.add(Reimbursement.find(reimbursement.getId()).getAmountReimbursed());
					i++;
				}
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Remboursement %s", Reimbursement.find(reimbursement.getId()).getRef()), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Remboursement %s", Reimbursement.find(reimbursement.getId()).getRef()), e), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour l'export du remboursement {}", Reimbursement.find(reimbursement.getId()).getRef());
				
			} finally {
				
				if (i % 10 == 0) { JPA.clear(); }
	
			}
		}
		
		if(reimbursementToExport != null && reimbursementToExport.size() != 0)  {
		
			try {
				
				reimbursementExportService.exportSepa(Company.find(company.getId()), Batch.find(batch.getId()).getStartDate(), reimbursementToExport, Batch.find(batch.getId()).getAccountingBatch().getBankDetails());
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Bug(Anomalie) généré(e)e dans l'export SEPA - Batch %s", batch.getId()), e), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e)e dans l'export SEPA - Batch {}", batch.getId());
				
			}
			
			try {
				
				cfonbService.exportCFONB(Company.find(company.getId()), Batch.find(batch.getId()).getStartDate(), reimbursementToExport, Batch.find(batch.getId()).getAccountingBatch().getBankDetails());
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Bug(Anomalie) généré(e)e dans l'export CFONB - Batch %s", batch.getId()), e), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e)e dans l'export CFONB - Batch {}", batch.getId());
				
			}
		}
	}
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {
		String comment = "";
		batch = Batch.find(batch.getId());
		switch (batch.getAccountingBatch().getReimbursementExportTypeSelect()) {
		case IAccount.REIMBURSEMENT_EXPORT_GENERATE:
			comment = "Compte rendu de création de remboursement :\n";
			comment += String.format("\t* %s remboursement(s) créé(s)\n", batch.getDone());
			comment += String.format("\t* Montant total : %s \n", this.totalAmount);

			break;
			
		case IAccount.REIMBURSEMNT_EXPORT_EXPORT:
			
			comment = "Compte rendu d'export de remboursement :\n";
			comment += String.format("\t* %s remboursement(s) traité(s)\n", batch.getDone());
			comment += String.format("\t* Montant total : %s \n", this.totalAmount);

			comment += String.format("\t* ------------------------------- \n");
			comment += String.format("\t* %s ", updateCustomerAccountLog);
			
			break;
		
		default:
			break;
		}
		
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}

}
