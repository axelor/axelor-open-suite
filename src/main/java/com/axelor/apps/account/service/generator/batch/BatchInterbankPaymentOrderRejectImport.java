package com.axelor.apps.account.service.generator.batch;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.CfonbService;
import com.axelor.apps.account.service.InterbankPaymentOrderRejectImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchInterbankPaymentOrderRejectImport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchInterbankPaymentOrderRejectImport.class);

	private boolean stop = false;
	
	private BigDecimal totalAmount = BigDecimal.ZERO;

	
	@Inject
	public BatchInterbankPaymentOrderRejectImport(InterbankPaymentOrderRejectImportService interbankPaymentOrderRejectImportService, CfonbService cfonbService, RejectImportService rejectImportService) {
		
		super(interbankPaymentOrderRejectImportService, cfonbService, rejectImportService);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
	
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
		}
	}
	
	
	
	public void runInterbankPaymentOrderRejectImport(Company company) {
		
		List<String[]> rejectFile = null;
				
		try {
			
			rejectFile = interbankPaymentOrderRejectImportService.getCFONBFile(Company.find(company.getId()));	
			
			if(rejectFile != null)  {
				this.runProcessCreateRejectMove(rejectFile, company);
			}
			
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format("Batch d'import des rejets de paiement par TIP et TIP chèque %s", batch.getId()), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
			
			incrementAnomaly();
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format("Batch d'import des rejets de paiement par TIP et TIP chèque %s", batch.getId()), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
			
			incrementAnomaly();
			
			LOG.error("Bug(Anomalie) généré(e) pour le batch d'import des rejets de paiement par TIP et TIP chèque {}", batch.getId());
			
		}
		
		
	}
	
	
	public void runProcessCreateRejectMove(List<String[]> rejectFile, Company company)  {
		int i = 0;
		
		for(String[] reject : rejectFile)  {
			
			try {
				
				Invoice invoice = interbankPaymentOrderRejectImportService.createInterbankPaymentOrderRejectMove(reject, Company.find(company.getId()));
				
				if(invoice != null)  {
					updateInvoice(invoice);
					this.totalAmount = this.totalAmount.add(new BigDecimal(reject[2]));
					i++;
				}
						
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Rejet de paiement de la facture %s", reject[1]), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Rejet de paiement de la facture %s", reject[1]), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le rejet de paiement de la facture {}", reject[1]);
				
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

		String comment = "Compte rendu de l'import des rejets de paiement par TIP et TIP chèque :\n";
		comment += String.format("\t* %s paiement(s) traité(s)\n", batch.getDone());
		comment += String.format("\t* Montant total : %s \n", this.totalAmount);
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}

}
