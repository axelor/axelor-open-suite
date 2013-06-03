package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.base.db.Status;

public class PaymentScheduleLineService {

	private static final Logger LOG = LoggerFactory.getLogger(PaymentScheduleLineService.class);
	
	
	/**
	 * Création d'une ligne d'échéancie0r
	 * 
	 * @param paymentSchedule
	 * 			L'échéancié attaché.
	 * @param invoiceTerm
	 * 			La facture d'échéance.
	 * @param scheduleLineSeq
	 * 			Le numéro d'échéance.
	 * @param scheduleDate
	 * 			La date d'échéance.
	 * 
	 * @return
	 */
	public PaymentScheduleLine createPaymentScheduleLine(PaymentSchedule paymentSchedule, BigDecimal inTaxAmount, int scheduleLineSeq, LocalDate scheduleDate, Status status) {
		
		PaymentScheduleLine paymentScheduleLine = new PaymentScheduleLine();
		
		paymentScheduleLine.setPaymentSchedule(paymentSchedule);
		paymentScheduleLine.setScheduleLineSeq(scheduleLineSeq);
		paymentScheduleLine.setScheduleDate(scheduleDate);
		paymentScheduleLine.setInTaxAmount(inTaxAmount);
		paymentScheduleLine.setStatus(status);
		
		LOG.debug("Création de la ligne de l'échéancier numéro {} pour la date du {} et la somme de {}", new Object[] {paymentScheduleLine.getScheduleLineSeq(), paymentScheduleLine.getScheduleDate(), paymentScheduleLine.getInTaxAmount()});
		
		return paymentScheduleLine;
		
	}

	/**
	 * En fonction des infos d'entête d'un échéancier, crée les lignes d'échéances
	 * 
	 * @param paymentSchedule
	 * 
	 * @return Un map pour remplir les lignes, éventuellement un message d'erreur
	 */
	public List<PaymentScheduleLine> createPaymentScheduleLines(PaymentSchedule paymentSchedule, Set<Invoice> invoices){
		
		List<PaymentScheduleLine> paymentScheduleLines = new ArrayList<PaymentScheduleLine>();
		
		BigDecimal totalInvoice = BigDecimal.ZERO;
		int nbrTerm = paymentSchedule.getNbrTerm();
		
		for (Invoice invoice : invoices)  {
			totalInvoice = totalInvoice.add(invoice.getInTaxTotalRemaining());
		}
		
		LOG.debug("Création de lignes pour l'échéancier numéro {} (nombre d'échéance : {}, total de la facture : {})", new Object[]{paymentSchedule.getScheduleId(), nbrTerm, totalInvoice});
		
		if (nbrTerm > 0 && totalInvoice.compareTo(BigDecimal.ZERO) == 1){
			
			BigDecimal termAmount = totalInvoice.divide(new BigDecimal(nbrTerm), 2, RoundingMode.HALF_EVEN);
			BigDecimal cumul = BigDecimal.ZERO;
			
			for (int i = 1; i < nbrTerm + 1; i++){
				
				if (i == nbrTerm)  {
					termAmount = totalInvoice.subtract(cumul);
				}
				else  {
					cumul = cumul.add(termAmount);
				}
				
				paymentScheduleLines.add(this.createPaymentScheduleLine(paymentSchedule, termAmount, i, paymentSchedule.getStartDate().plusMonths(i-1), Status.all().filter("code = 'upr'").fetchOne()));
				
			}
		}		
		
		return paymentScheduleLines;
	}
	
}
