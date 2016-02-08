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
package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;

public class PaymentScheduleLineService {

	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	
	/**
	 * Création d'une ligne d'échéancier
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
	public PaymentScheduleLine createPaymentScheduleLine(PaymentSchedule paymentSchedule, BigDecimal inTaxAmount, int scheduleLineSeq, LocalDate scheduleDate) {
		
		PaymentScheduleLine paymentScheduleLine = new PaymentScheduleLine();
		
		paymentScheduleLine.setPaymentSchedule(paymentSchedule);
		paymentScheduleLine.setScheduleLineSeq(scheduleLineSeq);
		paymentScheduleLine.setScheduleDate(scheduleDate);
		paymentScheduleLine.setInTaxAmount(inTaxAmount);
		paymentScheduleLine.setStatusSelect(PaymentScheduleLineRepository.STATUS_IN_PROGRESS);
		
		log.debug("Création de la ligne de l'échéancier numéro {} pour la date du {} et la somme de {}", 
				new Object[] {paymentScheduleLine.getScheduleLineSeq(), paymentScheduleLine.getScheduleDate(), paymentScheduleLine.getInTaxAmount()});
		
		return paymentScheduleLine;
		
	}

	/**
	 * En fonction des infos d'entête d'un échéancier, crée les lignes d'échéances
	 * 
	 * @param paymentSchedule
	 * 
	 */
	public List<PaymentScheduleLine> createPaymentScheduleLines(PaymentSchedule paymentSchedule){
		
		List<PaymentScheduleLine> paymentScheduleLines = new ArrayList<PaymentScheduleLine>();
		
		int nbrTerm = paymentSchedule.getNbrTerm();
		
		BigDecimal inTaxAmount = paymentSchedule.getInTaxAmount();
		
		log.debug("Création de lignes pour l'échéancier numéro {} (nombre d'échéance : {}, montant : {})", new Object[]{paymentSchedule.getScheduleId(), nbrTerm, inTaxAmount});
		
		if (nbrTerm > 0 && inTaxAmount.compareTo(BigDecimal.ZERO) == 1){
			
			BigDecimal termAmount = inTaxAmount.divide(new BigDecimal(nbrTerm), 2, RoundingMode.HALF_EVEN);
			BigDecimal cumul = BigDecimal.ZERO;
			
			for (int i = 1; i < nbrTerm + 1; i++){
				
				if (i == nbrTerm)  {
					termAmount = inTaxAmount.subtract(cumul);
				}
				else  {
					cumul = cumul.add(termAmount);
				}
				
				paymentScheduleLines.add(
						this.createPaymentScheduleLine(
								paymentSchedule, termAmount, i, paymentSchedule.getStartDate().plusMonths(i-1)));
				
			}
		}		
		
		return paymentScheduleLines;
	}
	
}
