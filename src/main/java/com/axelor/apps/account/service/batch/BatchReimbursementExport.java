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
package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.cfonb.CfonbExportService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
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
	public BatchReimbursementExport(ReimbursementExportService reimbursementExportService, CfonbExportService cfonbExportService, BatchAccountCustomer batchAccountCustomer) {
		
		super(reimbursementExportService, cfonbExportService, batchAccountCustomer);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
				
		switch (batch.getAccountingBatch().getReimbursementExportTypeSelect()) {
		
		case AccountingBatch.REIMBURSEMENT_EXPORT_TYPE_GENERATE:
			try {
				this.testAccountingBatchBankDetails(batch.getAccountingBatch());
				reimbursementExportService.testCompanyField(company);
			} catch (AxelorException e) {
				TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
				incrementAnomaly();
				stop = true;
			}
			break;
			
		case AccountingBatch.REIMBURSEMNT_EXPORT_TYPE_EXPORT:
			try {
				this.testAccountingBatchBankDetails(batch.getAccountingBatch());
				reimbursementExportService.testCompanyField(company);
				cfonbExportService.testCompanyExportCFONBField(company);
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
			case AccountingBatch.REIMBURSEMENT_EXPORT_TYPE_GENERATE:
				
				this.runCreateReimbursementExport(company);
				
				break;
				
			case AccountingBatch.REIMBURSEMNT_EXPORT_TYPE_EXPORT:
				
				this.runReimbursementExportProcess(company);
				
				updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(Company.find(company.getId()));
				
				break;
			
			default:
				break;
			}
		}
	}
	
	
	public void runCreateReimbursementExport(Company company)  {
		
		List<Reimbursement> reimbursementList = (List<Reimbursement>) Reimbursement.filter("self.statusSelect != ?1 AND self.statusSelect != ?2 AND self.company = ?3", 
				Reimbursement.STATUS_REIMBURSED, Reimbursement.STATUS_CANCELED, company).fetch();
		
		int i=0;

		for(Reimbursement reimbursement : reimbursementList)  {
			
			LOG.debug("Remboursement n° {}", reimbursement.getRef());
			
			updateReimbursement(Reimbursement.find(reimbursement.getId()));
		}
		
		List<Partner> partnerList = (List<Partner>) Partner.filter("?1 IN self.companySet = ?1", company).fetch();
		
		for(Partner partner : partnerList)  {
			
			try {
				partner = Partner.find(partner.getId());
				
				LOG.debug("Tiers n° {}", partner.getName());
				
				if(reimbursementExportService.canBeReimbursed(partner, Company.find(company.getId())))  {
				
					List<MoveLine> moveLineList = (List<MoveLine>) MoveLine.all().filter("self.account.reconcileOk = 'true' AND self.fromSchedulePaymentOk = 'false' " +
							"AND self.move.statusSelect = ?1 AND self.amountRemaining > 0 AND self.credit > 0 AND self.partner = ?2 AND self.company = ?3 AND " +
							"self.reimbursementStatusSelect = ?4 ",
							Move.STATUS_VALIDATED ,Partner.find(partner.getId()), Company.find(company.getId()), MoveLine.REIMBURSEMENT_STATUS_NULL).fetch();
					
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
		List<Reimbursement> reimbursementToCancelList = (List<Reimbursement>) Reimbursement
				.filter("self.company = ?1 and self.statusSelect = ?2 and self.amountToReimburse = 0", Reimbursement.STATUS_VALIDATED, company).fetch();
		
		// On annule les remboursements
		for(Reimbursement reimbursement : reimbursementToCancelList)  {
			reimbursement.setStatusSelect(Reimbursement.STATUS_CANCELED);
		}
		
		// On récupère les remboursement à rembourser
		List<Reimbursement> reimbursementList = (List<Reimbursement>) Reimbursement
				.filter("self.company = ?1 and self.statusSelect = ?2 and self.amountToReimburse > 0", company, Reimbursement.STATUS_VALIDATED).fetch();
		
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
				
				cfonbExportService.exportCFONB(Company.find(company.getId()), Batch.find(batch.getId()).getStartDate(), reimbursementToExport, Batch.find(batch.getId()).getAccountingBatch().getBankDetails());
				
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
		case AccountingBatch.REIMBURSEMENT_EXPORT_TYPE_GENERATE:
			comment = "Compte rendu de création de remboursement :\n";
			comment += String.format("\t* %s remboursement(s) créé(s)\n", batch.getDone());
			comment += String.format("\t* Montant total : %s \n", this.totalAmount);

			break;
			
		case AccountingBatch.REIMBURSEMNT_EXPORT_TYPE_EXPORT:
			
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
