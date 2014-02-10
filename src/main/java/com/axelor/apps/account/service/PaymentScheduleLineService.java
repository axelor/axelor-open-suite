/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
import com.axelor.apps.base.db.Status;

public class PaymentScheduleLineService {

	private static final Logger LOG = LoggerFactory.getLogger(PaymentScheduleLineService.class);
	
	
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
	public PaymentScheduleLine createPaymentScheduleLine(PaymentSchedule paymentSchedule, BigDecimal inTaxAmount, int scheduleLineSeq, LocalDate scheduleDate, Status status) {
		
		PaymentScheduleLine paymentScheduleLine = new PaymentScheduleLine();
		
		paymentScheduleLine.setPaymentSchedule(paymentSchedule);
		paymentScheduleLine.setScheduleLineSeq(scheduleLineSeq);
		paymentScheduleLine.setScheduleDate(scheduleDate);
		paymentScheduleLine.setInTaxAmount(inTaxAmount);
		paymentScheduleLine.setStatus(status);
		
		LOG.debug("Création de la ligne de l'échéancier numéro {} pour la date du {} et la somme de {}", 
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
		
		LOG.debug("Création de lignes pour l'échéancier numéro {} (nombre d'échéance : {}, montant : {})", new Object[]{paymentSchedule.getScheduleId(), nbrTerm, inTaxAmount});
		
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
								paymentSchedule, termAmount, i, paymentSchedule.getStartDate().plusMonths(i-1), Status.all().filter("code = 'upr'").fetchOne()));
				
			}
		}		
		
		return paymentScheduleLines;
	}
	
}
