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
package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.account.service.PaymentScheduleExportService;
import com.axelor.apps.account.service.cfonb.CfonbExportService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

public class BatchPaymentScheduleExport extends BatchStrategy {

	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected boolean stop = false;
	
	protected BigDecimal totalAmount = BigDecimal.ZERO;
	
	protected String updateCustomerAccountLog = "";
	
	protected PaymentScheduleLineRepository paymentScheduleLineRepo;
	
	protected InvoiceRepository invoiceRepo;
	
	@Inject
	public BatchPaymentScheduleExport(PaymentScheduleExportService paymentScheduleExportService, PaymentModeService paymentModeService, CfonbExportService cfonbExportService, 
			BatchAccountCustomer batchAccountCustomer, PaymentScheduleLineRepository paymentScheduleLineRepo, InvoiceRepository invoiceRepo) {
		
		super(paymentScheduleExportService, paymentModeService, cfonbExportService, batchAccountCustomer);
		
		this.paymentScheduleLineRepo = paymentScheduleLineRepo;
		this.invoiceRepo = invoiceRepo;
		
		AccountingService.setUpdateCustomerAccount(false);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
		
		company = companyRepo.find(company.getId());
				

		boolean sepa = batch.getAccountingBatch().getIsSepaDirectDebit();

		paymentScheduleExportService.setSepa(sepa);
		cfonbExportService.setSepa(sepa);
		
		paymentScheduleExportService.checkDirectDebitSequence(company);
		
		switch (batch.getAccountingBatch().getDirectDebitExportTypeSelect()) {
		
		case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_INVOICE:
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
			
		case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_MONTHLY:
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
			TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_1), batch.getAccountingBatch().getActionSelect()),IException.INCONSISTENCY ), IException.DIRECT_DEBIT, batch.getId());
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
				
				case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_INVOICE:
					
					this.exportInvoice();
					
					this.createInvoiceCfonbFile(batch);
					
					break;
					
				case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_MONTHLY:
					
					this.exportMajorMonthlyPayment();
					
					this.createMonthlyCfonbFile(batch);
					
					break;	
			}	
				
			updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(batchRepo.find(batch.getId()).getAccountingBatch().getCompany());
		}	
		
	}
	
	
	protected void exportProcess() {
		if(!stop)  {
			
			switch (batch.getAccountingBatch().getDirectDebitExportTypeSelect()) {
				
				case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_INVOICE:
					
					this.createInvoiceCfonbFile(batch.getAccountingBatch().getBatchToReExport());
					
					break;
					
				case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_MONTHLY:
					
					this.createMonthlyCfonbFile(batch.getAccountingBatch().getBatchToReExport());
					
					break;	
			}	
				
		}	
		
	}
	
	
	protected void exportMajorMonthlyPayment()  {

		// Génération de l'écriture de paiement pour Mensu Grand Compte
		log.debug("Génération de l'écriture de paiement pour Mensu Grand Compte");
		
		this.generateAllExportMensu(
				paymentScheduleExportService.getPaymentScheduleLineToDebit(batchRepo.find(batch.getId()).getAccountingBatch()));

	}
	
	
	protected void createMonthlyCfonbFile(Batch batchToExport)  {

		// Création du fichier d'export au format CFONB
		try  {

			AccountingBatch accountingBatch = batchRepo.find(batch.getId()).getAccountingBatch();

			// CFONB 160 ou CFONB 160 E (SEPA) suivant le booleen SepaDirectDebitOk
			cfonbExportService.exportPaymentScheduleCFONB(
					batch.getStartDate(),
					accountingBatch.getDebitDate(),
					(List<PaymentScheduleLine>) paymentScheduleLineRepo.all().filter("?1 MEMBER OF self.batchSet", batchToExport).fetch(),
					accountingBatch.getCompany(),
					accountingBatch.getBankDetails());

		} catch (AxelorException e) {

			TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_2), batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

		} catch (Exception e) {

			TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_2), batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

			log.error("Bug(Anomalie) généré(e) pour le batch d'export des prélèvements {}", batch.getId());

		}

	}
	

	/**
	 * Méthode permettant de générer l'ensemble des exports des prélèvements pour Mensu
	 * @return
	 * @throws AxelorException
	 */
	public void generateAllExportMensu (List<PaymentScheduleLine> paymentscheduleLineList)  {
		
		if(!stop)  {
			
			if(paymentscheduleLineList == null || paymentscheduleLineList.isEmpty())  {  return;  }

			Company company = batchRepo.find(batch.getId()).getAccountingBatch().getCompany();
			
			JPA.clear();
			
			for(PaymentScheduleLine paymentScheduleLine : paymentscheduleLineList)  {
				
				try  {
					if(!paymentScheduleExportService.isDebitBlocking(paymentScheduleLine))  {
					
						PaymentScheduleLine paymentScheduleLineToExport = paymentScheduleExportService.generateExportMensu(
								paymentScheduleLineRepo.find(paymentScheduleLine.getId()), paymentscheduleLineList, 
								companyRepo.find(company.getId()));
						
						if(paymentScheduleLineToExport != null)  {
							this.totalAmount = this.totalAmount.add(paymentScheduleLineToExport.getDirectDebitAmount());
							
							updatePaymentScheduleLine(paymentScheduleLineToExport);
						}
					}
					
				} catch (AxelorException e) {
					
					TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_3), paymentScheduleLineRepo.find(paymentScheduleLine.getId()).getName()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
					
					incrementAnomaly();
					
				} catch (Exception e) {
					
					TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_3), paymentScheduleLineRepo.find(paymentScheduleLine.getId()).getName()), e), IException.DIRECT_DEBIT, batch.getId());
					
					incrementAnomaly();
					
					log.error("Bug(Anomalie) généré(e) pour le Prélèvement de l'échéance {}", paymentScheduleLineRepo.find(paymentScheduleLine.getId()).getName());
					
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
		
		AccountingBatch accountingBatch = batchRepo.find(batch.getId()).getAccountingBatch();
		
		Company company = accountingBatch.getCompany();
		
		List<MoveLine> moveLineList = paymentScheduleExportService.getInvoiceToExport(company, accountingBatch.getDebitDate(), accountingBatch.getCurrency());
				
		long directDebitManagementMaxId = paymentScheduleExportService.getDirectDevitManagementMaxId();
		
		JPA.clear();

		for(MoveLine moveLine : moveLineList)  {
			
			try  {
				
				moveLine = moveLineRepo.find(moveLine.getId());
				
				log.debug("Paiement par prélèvement de l'écriture {}", moveLine.getName());
				
				Invoice invoice = paymentScheduleExportService.exportInvoice(
						moveLineRepo.find(moveLine.getId()), 
						moveLineList, 
						companyRepo.find(company.getId()), 
						directDebitManagementMaxId);
				
				if(invoice != null)  {
					
					this.totalAmount = this.totalAmount.add(invoice.getDirectDebitAmount());
					
					updateInvoice(invoice);
				}
			
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_2), this.getInvoiceName(moveLine)), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_2), this.getInvoiceName(moveLine)), e), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
				log.error("Bug(Anomalie) généré(e) pour le batch d'export des prélèvements {}", this.getInvoiceName(moveLine));
				
			} finally {
				
				JPA.clear();
	
			}
		}
	}

	protected void createInvoiceCfonbFile(Batch batchToExport)  {
		
		// Création du fichier d'export au format CFONB
		try  {
			
			AccountingBatch accountingBatch = batchRepo.find(batch.getId()).getAccountingBatch();
			
			cfonbExportService.exportInvoiceCFONB(
					batch.getStartDate(), 
					accountingBatch.getDebitDate(), 
					(List<Invoice>) invoiceRepo.all().filter("?1 MEMBER OF self.batchSet", batchToExport).fetch(), 
					accountingBatch.getCompany(), 
					accountingBatch.getBankDetails());
		
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_2), batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_2), batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
			log.error("Bug(Anomalie) généré(e) pour le batch d'export des prélèvements {}", batch.getId());
		
		}	
	}
	

	protected String getInvoiceName(MoveLine moveLine)  {

		moveLine = moveLineRepo.find(moveLine.getId());

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
		
		
		switch (batchRepo.find(batch.getId()).getAccountingBatch().getDirectDebitExportTypeSelect()) {
		
			case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_INVOICE:
				comment = I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_4);
				comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_5)+"\n", batch.getDone());
				comment += String.format("\t* "+I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_5)+" : %s \n", this.totalAmount);
				comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());
				
				break;
				
			case AccountingBatchRepository.DIRECT_DEBIT_EXPORT_TYPE_MONTHLY:
	            comment = I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_6);
	            comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_7)+"\n", batch.getDone());
	            comment += String.format("\t* "+I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_5)+" : %s \n", this.totalAmount);
	            comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());
	            
	            break;	
				
		}
		
		comment += String.format("\t* ------------------------------- \n");
		comment += String.format("\t* %s ", updateCustomerAccountLog);
		
		addComment(comment);
		super.stop();
	}

}
