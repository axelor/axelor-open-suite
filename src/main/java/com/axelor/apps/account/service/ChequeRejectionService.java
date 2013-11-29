/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.ChequeRejection;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ChequeRejectionService {
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	/**
	 * procédure de validation du rejet de chèque
	 * @param chequeRejection
	 * 			Un rejet de chèque brouillon
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validateChequeRejection(ChequeRejection chequeRejection) throws AxelorException   {
		
		Company company = chequeRejection.getCompany();
		
		this.testCompanyField(company);
				
		this.setSequence(chequeRejection);
		
		Move move = this.createChequeRejectionMove(chequeRejection, company);
		
		chequeRejection.setMove(move);
	
		chequeRejection.setStatus(Status.all().filter("self.code = 'val'").fetchOne());
		
		chequeRejection.save();
	}
	
	/**
	 * Méthode permettant de créer une écriture de rejet de chèque (L'extourne de l'écriture de paiement)
	 * @param chequeRejection
	 * 			Un rejet de cheque brouillon
	 * @param company
	 * 			Une société
	 * @return
	 * 			L'écriture de rejet de chèque
	 * @throws AxelorException
	 */
	public Move createChequeRejectionMove(ChequeRejection chequeRejection, Company company) throws AxelorException  {
		this.testCompanyField(company);
		
		Journal journal = company.getAccountConfig().getRejectJournal();

		PaymentVoucher paymentVoucher = chequeRejection.getPaymentVoucher();
		
		Move paymentMove = paymentVoucher.getGeneratedMove();
		
		Partner partner =  paymentVoucher.getPartner();
		
		InterbankCodeLine interbankCodeLine = chequeRejection.getInterbankCodeLine();
		
		String description = chequeRejection.getDescription();
		
		LocalDate rejectionDate = chequeRejection.getRejectionDate();
		
		// Move
		Move move = moveService.createMove(journal, company, null, partner, rejectionDate, null, false);
		
		int ref = 1;
		
		for(MoveLine moveLine : paymentMove.getMoveLineList())  {
			
			if(moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
				// Debit MoveLine
				MoveLine debitMoveLine = moveLineService.createMoveLine(move, partner, moveLine.getAccount(), moveLine.getCredit(), true, false, rejectionDate, ref, null);
				move.getMoveLineList().add(debitMoveLine);
				debitMoveLine.setInterbankCodeLine(interbankCodeLine);
				debitMoveLine.setDescription(description);
				
			}
			else  {
				// Credit MoveLine
				MoveLine creditMoveLine = moveLineService.createMoveLine(move, partner, moveLine.getAccount(), moveLine.getDebit(), false, false, rejectionDate, ref, null);
				move.getMoveLineList().add(creditMoveLine);
				creditMoveLine.setInterbankCodeLine(interbankCodeLine);
				creditMoveLine.setDescription(description);
			}
			
			ref++;
		}
		
		move.setRejectOk(true);
		
		moveService.validateMove(move);
		
		return move;
	}
	
	
	/**
	 * Procédure permettant de vérifier les champs d'une société
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
		
		accountConfigService.getRejectJournal(accountConfigService.getAccountConfig(company));
	}
	
	
	
	/**
	 * Procédure permettant d'assigner une séquence de rejet de chèque
	 * @param chequeRejection
	 *				Un rejet de chèque
	 * @throws AxelorException
	 */
	public void setSequence(ChequeRejection chequeRejection) throws AxelorException  {
		String seq = sequenceService.getSequence(IAdministration.CHEQUE_REJECT, chequeRejection.getCompany(), false);
		if(seq == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Rejet de chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),chequeRejection.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		chequeRejection.setName(seq);
	}
	
	
	
	
	
	
}
