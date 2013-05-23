package com.axelor.apps.account.service.generator.batch;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.service.CfonbService;
import com.axelor.apps.account.service.InterbankPaymentOrderImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchInterbankPaymentOrderImport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchInterbankPaymentOrderImport.class);

	private boolean stop = false;
	
	private BigDecimal totalAmount = BigDecimal.ZERO;

	
	@Inject
	public BatchInterbankPaymentOrderImport(InterbankPaymentOrderImportService interbankPaymentOrderImportService, CfonbService cfonbService, RejectImportService rejectImportService) {
		
		super(interbankPaymentOrderImportService, cfonbService, rejectImportService);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
	
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
				
				String dest = rejectImportService.getDestCFONBFile(company.getInterbankPaymentOrderImportPathCFONB(), company.getTempInterbankPaymentOrderImportPathCFONB());
				
				// Récupération des enregistrements
				paymentFile = cfonbService.importCFONB(dest, company, 3, 4);	
				
				if(paymentFile != null)  {
					this.runInterbankPaymentOrderImport(paymentFile, company);
				}
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Batch d'import des paiements par TIP et TIP chèque %s", batch.getId()), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Batch d'import des paiements par TIP et TIP chèque %s", batch.getId()), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le batch d'import des paiements par TIP et TIP chèque {}", batch.getId());
				
			}
		}
	}
	
	
	public void runInterbankPaymentOrderImport(List<String[]> paymentFile, Company company)  {
		int i = 0;
		
		for(String[] payment : paymentFile)  {
			try {
				
				PaymentVoucher paymentVoucher = interbankPaymentOrderImportService.runInterbankPaymentOrder(payment, Company.find(company.getId()));
				
				if(paymentVoucher != null)  {
					updatePaymentVoucher(paymentVoucher);
					this.totalAmount = this.totalAmount.add(PaymentVoucher.find(paymentVoucher.getId()).getPaidAmount());
				}
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Paiement de la facture %s", payment[1]), e, e.getcategory()), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Paiement de la facture %s", payment[1]), e), IException.INTERBANK_PAYMENT_ORDER, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le paiement de la facture {}", payment[1]);
				
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

		String comment = "Compte rendu de l'import des paiements par TIP et TIP chèque :\n";
		comment += String.format("\t* %s paiement(s) traité(s)\n", batch.getDone());
		comment += String.format("\t* Montant total : %s \n", this.totalAmount);
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}

}
