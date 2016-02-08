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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.account.service.InterbankPaymentOrderImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.account.service.cfonb.CfonbImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

public class BatchInterbankPaymentOrderImport extends BatchStrategy {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected boolean stop = false;
	
	protected BigDecimal totalAmount = BigDecimal.ZERO;
	
	protected String updateCustomerAccountLog = "";
	
	protected PaymentVoucherRepository paymentVoucherRepo;

	
	@Inject
	public BatchInterbankPaymentOrderImport(InterbankPaymentOrderImportService interbankPaymentOrderImportService, CfonbImportService cfonbImportService, 
			RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer, PaymentVoucherRepository paymentVoucherRepo) {
		
		super(interbankPaymentOrderImportService, cfonbImportService, rejectImportService, batchAccountCustomer);
		
		this.paymentVoucherRepo = paymentVoucherRepo;
		
		AccountingService.setUpdateCustomerAccount(false);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
		
		try {
			interbankPaymentOrderImportService.testCompanyField(company);
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
		
			Company company = batch.getAccountingBatch().getCompany();
				
			List<String[]> paymentFile = null;
			
			try {
				
				AccountConfig accountConfig = company.getAccountConfig();
				
				String dest = rejectImportService.getDestCFONBFile(accountConfig.getInterbankPaymentOrderImportPathCFONB(), accountConfig.getTempInterbankPaymentOrderImportPathCFONB());
				
				// Récupération des enregistrements
				paymentFile = cfonbImportService.importCFONB(dest, company, 3, 4);	
				
				if(paymentFile != null)  {
					this.runInterbankPaymentOrderImport(paymentFile, company);
				}
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_1), batch.getId()), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_1), batch.getId()), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
				log.error("Bug(Anomalie) généré(e) pour le batch d'import des paiements par TIP et TIP chèque {}", batch.getId());
				
			}
			
			updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(companyRepo.find(company.getId()));
		}
	}
	
	
	public void runInterbankPaymentOrderImport(List<String[]> paymentFile, Company company)  {
		int i = 0;
		
		for(String[] payment : paymentFile)  {
			try {
				
				PaymentVoucher paymentVoucher = interbankPaymentOrderImportService.runInterbankPaymentOrder(payment, companyRepo.find(company.getId()));
				
				if(paymentVoucher != null)  {
					updatePaymentVoucher(paymentVoucher);
					this.totalAmount = this.totalAmount.add(paymentVoucherRepo.find(paymentVoucher.getId()).getPaidAmount());
				}
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_2), payment[1]), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_2), payment[1]), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
				log.error("Bug(Anomalie) généré(e) pour le paiement de la facture {}", payment[1]);
				
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

		String comment = I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_3)+"\n";
		comment += String.format("\t*"+I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_4)+"\n", batch.getDone());
		comment += String.format("\t*"+I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_5)+": %s \n", this.totalAmount);
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		comment += String.format("\t* ------------------------------- \n");
		comment += String.format("\t* %s ", updateCustomerAccountLog);
		
		super.stop();
		addComment(comment);
		
	}

}
