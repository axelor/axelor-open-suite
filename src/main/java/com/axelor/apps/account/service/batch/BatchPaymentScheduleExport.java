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
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.service.PaymentScheduleExportService;
import com.axelor.apps.account.service.cfonb.CfonbExportService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchPaymentScheduleExport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchPaymentScheduleExport.class);
	
	private boolean stop = false;
	
	private BigDecimal totalAmount = BigDecimal.ZERO;
	
	private String updateCustomerAccountLog = "";
	
	@Inject
	public BatchPaymentScheduleExport(PaymentScheduleExportService paymentScheduleExportService, PaymentModeService paymentModeService, CfonbExportService cfonbExportService, BatchAccountCustomer batchAccountCustomer) {
		
		super(paymentScheduleExportService, paymentModeService, cfonbExportService, batchAccountCustomer);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
		
		company = Company.find(company.getId());
				

		boolean sepa = batch.getAccountingBatch().getIsSepaDirectDebit();

		paymentScheduleExportService.setSepa(sepa);
		cfonbExportService.setSepa(sepa);
		
		switch (batch.getAccountingBatch().getDirectDebitExportTypeSelect()) {
		
		case IAccount.INVOICE_EXPORT:
			try {
				paymentScheduleExportService.checkDebitDate(batch.getAccountingBatch());
				paymentScheduleExportService.checkInvoiceExportCompany(company);
				cfonbExportService.testCompanyExportCFONBField(company);
				this.testAccountingBatchBankDetails(batch.getAccountingBatch());
			} catch (AxelorException e) {
				TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
				incrementAnomaly();
				stop = true;
			}
			break;
			
		case IAccount.MONTHLY_EXPORT:
			try {
				paymentScheduleExportService.checkDebitDate(batch.getAccountingBatch());
				paymentScheduleExportService.checkMonthlyExportCompany(company);
				cfonbExportService.testCompanyExportCFONBField(company);
				this.testAccountingBatchBankDetails(batch.getAccountingBatch());
			} catch (AxelorException e) {
				TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
				incrementAnomaly();
				stop = true;
			}
			break;	
			
		default:
			TraceBackService.trace(new AxelorException(String.format("Type de donnée inconnu pour le traitement %s", batch.getAccountingBatch().getActionSelect()),IException.INCONSISTENCY ), IException.DIRECT_DEBIT, batch.getId());
			incrementAnomaly();
			stop = true;
		}
		
		checkPoint();

	}
	

	@Override
	protected void process() {

		if(!stop)  {

			if(batch.getAccountingBatch().getHasToReExportCfbonb())  {

				this.exportProcess();

			}
			else  {

				this.directDebitProcess();

			}
		}
		
	}
	
	
	protected void directDebitProcess() {
		if(!stop)  {
			
			switch (batch.getAccountingBatch().getDirectDebitExportTypeSelect()) {
				
				case IAccount.INVOICE_EXPORT:
					
					this.exportInvoice();
					
					this.createInvoiceCfonbFile(batch);
					
					break;
					
				case IAccount.MONTHLY_EXPORT:
					
					this.exportMajorMonthlyPayment();
					
					this.createMonthlyCfonbFile(batch);
					
					break;	
			}	
				
			updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(Batch.find(batch.getId()).getAccountingBatch().getCompany());
		}	
		
	}
	
	
	protected void exportProcess() {
		if(!stop)  {
			
			switch (batch.getAccountingBatch().getDirectDebitExportTypeSelect()) {
				
				case IAccount.INVOICE_EXPORT:
					
					this.createInvoiceCfonbFile(batch.getAccountingBatch().getBatchToReExport());
					
					break;
					
				case IAccount.MONTHLY_EXPORT:
					
					this.createMonthlyCfonbFile(batch.getAccountingBatch().getBatchToReExport());
					
					break;	
			}	
				
		}	
		
	}
	
	
	protected void exportMajorMonthlyPayment()  {

		// Génération de l'écriture de paiement pour Mensu Grand Compte
		LOG.debug("Génération de l'écriture de paiement pour Mensu Grand Compte");
		
		this.generateAllExportMensu(
				paymentScheduleExportService.getPaymentScheduleLineToDebit(Batch.find(batch.getId()).getAccountingBatch()));

	}
	
	
	protected void createMonthlyCfonbFile(Batch batchToExport)  {

		// Création du fichier d'export au format CFONB
		try  {

			AccountingBatch accountingBatch = Batch.find(batch.getId()).getAccountingBatch();

			// CFONB 160 ou CFONB 160 E (SEPA) suivant le booleen SepaDirectDebitOk
			cfonbExportService.exportPaymentScheduleCFONB(
					batch.getStartDate(),
					accountingBatch.getDebitDate(),
					PaymentScheduleLine.filter("?1 MEMBER OF self.batchSet", batchToExport).fetch(),
					accountingBatch.getCompany(),
					accountingBatch.getBankDetails());

		} catch (AxelorException e) {

			TraceBackService.trace(new AxelorException(String.format("Batch d'export des prélèvements %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

		} catch (Exception e) {

			TraceBackService.trace(new Exception(String.format("Batch d'export des prélèvements %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

			LOG.error("Bug(Anomalie) généré(e) pour le batch d'export des prélèvements {}", batch.getId());

		}

	}
	

	/**
	 * Méthode permettant de générer l'ensemble des exports des prélèvements pour Mensu
	 * @param paymentScheduleExport
	 * 			Un objet d'export des prélèvements
	 * @param company
	 * 			Une société
	 * @param paymentMode
	 * 			Un mode de paiement
	 * @param statusVal
	 * 			Un status
	 * @param journal
	 * 			Un journal
	 * @param isMajorAccount
	 * 			Le traitement concerne le prélèvement des échéances de mensu grand compte ?
	 * @return 
	 * @return
	 * @throws AxelorException
	 */
	public void generateAllExportMensu (List<PaymentScheduleLine> paymentscheduleLineList)  {
		
		if(!stop)  {
			
			if(paymentscheduleLineList == null || paymentscheduleLineList.isEmpty())  {  return;  }

			Company company = Batch.find(batch.getId()).getAccountingBatch().getCompany();
			
			JPA.clear();
			
			for(PaymentScheduleLine paymentScheduleLine : paymentscheduleLineList)  {
				
				try  {
					if(!paymentScheduleExportService.isDebitBlocking(paymentScheduleLine))  {
					
						PaymentScheduleLine paymentScheduleLineToExport = paymentScheduleExportService.generateExportMensu(
								PaymentScheduleLine.find(paymentScheduleLine.getId()), paymentscheduleLineList, 
								Company.find(company.getId()));
						
						if(paymentScheduleLineToExport != null)  {
							this.totalAmount = this.totalAmount.add(paymentScheduleLineToExport.getDirectDebitAmount());
							
							updatePaymentScheduleLine(paymentScheduleLineToExport);
						}
					}
					
				} catch (AxelorException e) {
					
					TraceBackService.trace(new AxelorException(String.format("Prélèvement de l'échéance %s", PaymentScheduleLine.find(paymentScheduleLine.getId()).getName()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
					
					incrementAnomaly();
					
				} catch (Exception e) {
					
					TraceBackService.trace(new Exception(String.format("Prélèvement de l'échéance %s", PaymentScheduleLine.find(paymentScheduleLine.getId()).getName()), e), IException.DIRECT_DEBIT, batch.getId());
					
					incrementAnomaly();
					
					LOG.error("Bug(Anomalie) généré(e) pour le Prélèvement de l'échéance {}", PaymentScheduleLine.find(paymentScheduleLine.getId()).getName());
					
				} finally {
					
					JPA.clear();
		
				}	
			}
		}
	}

	/**
	 * Procédure permettant d'exporter des factures
	 * @param company
	 * 			Une société
	 * @param pse
	 * 			Un Export des prélèvement
	 * @param pm
	 * 			Un mode de paiement
	 * @throws AxelorException
	 */
	public void exportInvoice()  {
		
		AccountingBatch accountingBatch = Batch.find(batch.getId()).getAccountingBatch();
		
		Company company = accountingBatch.getCompany();
		
		List<MoveLine> moveLineList = paymentScheduleExportService.getInvoiceToExport(company, accountingBatch.getDebitDate(), accountingBatch.getCurrency());
				
		long directDebitManagementMaxId = paymentScheduleExportService.getDirectDevitManagementMaxId();
		
		JPA.clear();

		for(MoveLine moveLine : moveLineList)  {
			
			try  {
				
				moveLine = MoveLine.find(moveLine.getId());
				
				LOG.debug("Paiement par prélèvement de l'écriture {}", moveLine.getName());
				
				Invoice invoice = paymentScheduleExportService.exportInvoice(
						MoveLine.find(moveLine.getId()), 
						moveLineList, 
						Company.find(company.getId()), 
						directDebitManagementMaxId);
				
				if(invoice != null)  {
					
					this.totalAmount = this.totalAmount.add(invoice.getDirectDebitAmount());
					
					updateInvoice(invoice);
				}
			
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Batch d'export des prélèvements %s", this.getInvoiceName(moveLine)), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Batch d'export des prélèvements %s", this.getInvoiceName(moveLine)), e), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le batch d'export des prélèvements {}", this.getInvoiceName(moveLine));
				
			} finally {
				
				JPA.clear();
	
			}
		}
	}

	protected void createInvoiceCfonbFile(Batch batchToExport)  {
		
		// Création du fichier d'export au format CFONB
		try  {
			
			AccountingBatch accountingBatch = Batch.find(batch.getId()).getAccountingBatch();
			
			cfonbExportService.exportInvoiceCFONB(
					batch.getStartDate(), 
					accountingBatch.getDebitDate(), 
					Invoice.filter("?1 MEMBER OF self.batchSet", batchToExport).fetch(), 
					accountingBatch.getCompany(), 
					accountingBatch.getBankDetails());
		
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format("Batch d'export des prélèvements %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format("Batch d'export des prélèvements %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
			LOG.error("Bug(Anomalie) généré(e) pour le batch d'export des prélèvements {}", batch.getId());
		
		}	
	}
	

	protected String getInvoiceName(MoveLine moveLine)  {

		moveLine = MoveLine.find(moveLine.getId());

		if(moveLine.getMove() != null && moveLine.getMove().getInvoice() != null)  {
			return moveLine.getMove().getInvoice().getInvoiceId();
		}
		else if(moveLine.getMove() != null && moveLine.getInvoiceReject() != null) {
			return moveLine.getInvoiceReject().getInvoiceId();
		}
		else  {
			return moveLine.getName();
		}

	}
	
	
	
	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {
		String comment = ""; 
		
		
		switch (Batch.find(batch.getId()).getAccountingBatch().getDirectDebitExportTypeSelect()) {
		
			case IAccount.INVOICE_EXPORT:
				comment = "Compte rendu d'export des prélèvements factures et échéances de paiement :\n";
				comment += String.format("\t* %s prélèvements(s) facture(s) et échéance(s) traité(s)\n", batch.getDone());
				comment += String.format("\t* Montant total : %s \n", this.totalAmount);
				comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
				
				break;
				
			case IAccount.MONTHLY_EXPORT:
	            comment = "Compte rendu d'export des prélèvements de mensualité :\n";
	            comment += String.format("\t* %s prélèvements(s) mensualité(s) traité(s)\n", batch.getDone());
	            comment += String.format("\t* Montant total : %s \n", this.totalAmount);
	            comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
	            
	            break;	
				
		}
		
		comment += String.format("\t* ------------------------------- \n");
		comment += String.format("\t* %s ", updateCustomerAccountLog);
		
		addComment(comment);
		super.stop();
	}

}
