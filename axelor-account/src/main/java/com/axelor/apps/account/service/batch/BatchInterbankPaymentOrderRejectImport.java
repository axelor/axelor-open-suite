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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.account.service.InterbankPaymentOrderRejectImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

public class BatchInterbankPaymentOrderRejectImport extends BatchStrategy {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected boolean stop = false;
	
	protected BigDecimal totalAmount = BigDecimal.ZERO;
	
	protected String updateCustomerAccountLog = "";
	
	
	@Inject
	public BatchInterbankPaymentOrderRejectImport(InterbankPaymentOrderRejectImportService interbankPaymentOrderRejectImportService, 
			RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		
		super(interbankPaymentOrderRejectImportService, rejectImportService, batchAccountCustomer);
		
		AccountingService.setUpdateCustomerAccount(false);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
		
		try {
			interbankPaymentOrderRejectImportService.testCompanyField(company);
		} catch (AxelorException e) {
			TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
			incrementAnomaly();
			stop = true;
		}
			
		checkPoint();

	}

	@Override
	protected void process() {
		if(!stop)  {
			this.runInterbankPaymentOrderRejectImport(batch.getAccountingBatch().getCompany());
			
			updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(null);
		}
	}
	
	
	
	public void runInterbankPaymentOrderRejectImport(Company company) {
		
		List<String[]> rejectFile = null;
				
		try {
			
			rejectFile = interbankPaymentOrderRejectImportService.getCFONBFile(companyRepo.find(company.getId()));	
			
			if(rejectFile != null)  {
				this.runProcessCreateRejectMove(rejectFile, company);
			}
			
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_REJECT_IMPORT_1), batch.getId()), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
			
			incrementAnomaly();
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_REJECT_IMPORT_1), batch.getId()), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
			
			incrementAnomaly();
			
			log.error("Bug(Anomalie) généré(e) pour le batch d'import des rejets de paiement par TIP et TIP chèque {}", batch.getId());
			
		}
		
		
	}
	
	
	public void runProcessCreateRejectMove(List<String[]> rejectFile, Company company)  {
		int i = 0;
		
		for(String[] reject : rejectFile)  {
			
			try {
				
				Invoice invoice = interbankPaymentOrderRejectImportService.createInterbankPaymentOrderRejectMove(reject, companyRepo.find(company.getId()));
				
				if(invoice != null)  {
					updateInvoice(invoice);
					this.totalAmount = this.totalAmount.add(new BigDecimal(reject[2]));
					i++;
				}
						
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_REJECT_IMPORT_2), reject[1]), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_REJECT_IMPORT_2), reject[1]), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
				log.error("Bug(Anomalie) généré(e) pour le rejet de paiement de la facture {}", reject[1]);
				
			} finally {
				
				if (i % 10 == 0) { JPA.clear(); }
	
			}
		}
	}
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_REJECT_IMPORT_3)+" :\n";
		comment += String.format("\t* "+I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_4)+"\n", batch.getDone());
		comment += String.format("\t* "+I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_5)+" : %s \n", this.totalAmount);
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		comment += String.format("\t* ------------------------------- \n");
		comment += String.format("\t* %s ", updateCustomerAccountLog);
		
		super.stop();
		addComment(comment);
		
	}

}
