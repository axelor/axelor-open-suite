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
package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.ChequeRejection;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.config.AccountConfigService;
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
	
		chequeRejection.setStatus(Status.findByCode("val"));
		
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
		Move move = moveService.createMove(journal, company, null, partner, rejectionDate, null);
		
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
		
		String seq = sequenceService.getSequenceNumber(IAdministration.CHEQUE_REJECT, chequeRejection.getCompany());
		
		if(seq == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Rejet de chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),chequeRejection.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		chequeRejection.setName(seq);
	}
	
	
	
	
	
	
}
