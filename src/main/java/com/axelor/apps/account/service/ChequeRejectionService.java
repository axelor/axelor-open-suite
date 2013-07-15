package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.ChequeRejection;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ChequeRejectionService {
	
	@Inject
	private MoveService ms;
	
	@Inject
	private MoveLineService mls;
	
	@Inject
	private SequenceService sgs;
	
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
		
		Journal journal = company.getRejectJournal();

		PaymentVoucher paymentVoucher = chequeRejection.getPaymentVoucher();
		
		Move paymentMove = paymentVoucher.getGeneratedMove();
		
		Partner partner =  paymentVoucher.getPartner();
		
		InterbankCodeLine interbankCodeLine = chequeRejection.getInterbankCodeLine();
		
		String description = chequeRejection.getDescription();
		
		LocalDate rejectionDate = chequeRejection.getRejectionDate();
		
		// Move
		Move move = ms.createMove(journal, company, null, partner, rejectionDate, null, false);
		
		int ref = 1;
		
		for(MoveLine moveLine : paymentMove.getMoveLineList())  {
			
			if(moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
				// Debit MoveLine
				MoveLine debitMoveLine = mls.createMoveLine(move, partner, moveLine.getAccount(), moveLine.getCredit(), true, false, rejectionDate,
							ref, false, false, false, null);
				move.getMoveLineList().add(debitMoveLine);
				debitMoveLine.setInterbankCodeLine(interbankCodeLine);
				debitMoveLine.setDescription(description);
				
			}
			else  {
				// Credit MoveLine
				MoveLine creditMoveLine = mls.createMoveLine(move, partner, moveLine.getAccount(), moveLine.getDebit(), false, false, rejectionDate,
							ref, false, false, false, null);
				move.getMoveLineList().add(creditMoveLine);
				creditMoveLine.setInterbankCodeLine(interbankCodeLine);
				creditMoveLine.setDescription(description);
			}
			
			ref++;
		}
		
		move.setRejectOk(true);
		
		ms.validateMove(move);
		
		return move;
	}
	
	
	/**
	 * Procédure permettant de vérifier les champs d'une société
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
		if(company.getRejectJournal() == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal de rejet pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	
	/**
	 * Procédure permettant d'assigner une séquence de rejet de chèque
	 * @param chequeRejection
	 *				Un rejet de chèque
	 * @throws AxelorException
	 */
	public void setSequence(ChequeRejection chequeRejection) throws AxelorException  {
		String seq = sgs.getSequence(IAdministration.CHEQUE_REJECT, chequeRejection.getCompany(), false);
		if(seq == null)   {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Rejet de chèque pour la société %s",
					GeneralService.getExceptionAccountingMsg(),chequeRejection.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		
		chequeRejection.setName(seq);
	}
	
	
	
	
	
	
}
