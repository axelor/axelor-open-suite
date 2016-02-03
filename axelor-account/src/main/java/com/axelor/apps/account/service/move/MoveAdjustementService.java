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
package com.axelor.apps.account.service.move;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveAdjustementService {

	protected MoveLineService moveLineService;
	protected MoveCreateService moveCreateService;
	protected MoveValidateService moveValidateService;
	protected MoveRepository moveRepository;
	protected AccountConfigService accountConfigService;
	protected LocalDate today;

	@Inject
	public MoveAdjustementService(GeneralService generalService, MoveLineService moveLineService, MoveCreateService moveCreateService, MoveValidateService moveValidateService, 
			MoveToolService moveToolService, MoveDueService moveDueService, MoveRepository moveRepository) {

		this.moveLineService = moveLineService;
		this.moveCreateService = moveCreateService;
		this.moveValidateService = moveValidateService;
		this.moveRepository = moveRepository;
		
		today = generalService.getTodayDate();

	}
	
	
	/**
	 * Creating move of passage in gap regulation (on debit)
	 * @param debitMoveLine
	 * @return
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createAdjustmentDebitMove(MoveLine debitMoveLine) throws AxelorException  {

		Partner partner = debitMoveLine.getPartner();
		Account account = debitMoveLine.getAccount();
		Move debitMove = debitMoveLine.getMove();
		Company company = debitMove.getCompany();
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		BigDecimal debitAmountRemaining = debitMoveLine.getAmountRemaining();

		Journal miscOperationJournal = accountConfigService.getMiscOperationJournal(accountConfig);

		Move adjustmentMove = moveCreateService.createMove(miscOperationJournal, company, null, partner, null);

		// Création de la ligne au crédit
		MoveLine creditAdjustmentMoveLine = moveLineService.createMoveLine(adjustmentMove, partner, account, debitAmountRemaining, false, today, 1, null);

		// Création de la ligne au debit
		MoveLine debitAdjustmentMoveLine = moveLineService.createMoveLine(
				adjustmentMove, partner, accountConfigService.getCashPositionVariationAccount(accountConfig), debitAmountRemaining, true, today, 2, null);

		adjustmentMove.addMoveLineListItem(creditAdjustmentMoveLine);
		adjustmentMove.addMoveLineListItem(debitAdjustmentMoveLine);

		moveValidateService.validateMove(adjustmentMove);
		moveRepository.save(adjustmentMove);


	}


	/**
	 * Creating move of passage in gap regulation (on credit)
	 * @param creditMoveLine
	 * @return
	 * @throws AxelorException
	 */
	public MoveLine createAdjustmentCreditMove(MoveLine creditMoveLine) throws AxelorException  {

		Partner partner = creditMoveLine.getPartner();
		Account account = creditMoveLine.getAccount();
		Move creditMove = creditMoveLine.getMove();
		Company company = creditMove.getCompany();
		BigDecimal creditAmountRemaining = creditMoveLine.getAmountRemaining();
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		Journal miscOperationJournal = accountConfigService.getMiscOperationJournal(accountConfig);

		Move adjustmentMove = moveCreateService.createMove(miscOperationJournal, company, null, partner, null);

		// Création de la ligne au crédit
		MoveLine creditAdjustmentMoveLine = moveLineService.createMoveLine(
				adjustmentMove, partner, accountConfigService.getCashPositionVariationAccount(accountConfig), creditAmountRemaining, false, today, 1, null);

		// Création de la ligne au débit
		MoveLine debitAdjustmentMoveLine = moveLineService.createMoveLine(adjustmentMove, partner, account, creditAmountRemaining, true, today, 2, null);

		adjustmentMove.addMoveLineListItem(creditAdjustmentMoveLine);
		adjustmentMove.addMoveLineListItem(debitAdjustmentMoveLine);
		moveValidateService.validateMove(adjustmentMove);
		moveRepository.save(adjustmentMove);
		
		return debitAdjustmentMoveLine;

	}

	
	
	/**
	 * Méthode permettant de créer une écriture du passage du compte de l'écriture au debit vers le compte de l'écriture au credit.
	 * @param debitMoveLineToReconcile
	 * 			Ecriture au débit
	 * @param creditMoveLineToReconcile
	 * 			Ecriture au crédit
	 * @param amount
	 * 			Montant
	 * @return
	 * 			L'écriture de passage du compte de l'écriture au debit vers le compte de l'écriture au credit.
	 * @throws AxelorException
	 */
	public Move createMoveToPassOnTheOtherAccount(MoveLine debitMoveLineToReconcile, MoveLine creditMoveLineToReconcile, BigDecimal amount) throws AxelorException  {
		
		Partner partnerDebit = debitMoveLineToReconcile.getPartner();
		Partner partnerCredit = creditMoveLineToReconcile.getPartner();
		
		Company company = debitMoveLineToReconcile.getMove().getCompany();
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		Journal journal = accountConfigService.getMiscOperationJournal(accountConfig);

		// Move
		Move move = moveCreateService.createMove(journal, company, null, partnerDebit, null);
		
		MoveLine debitMoveLine = moveLineService.createMoveLine(move, partnerCredit, creditMoveLineToReconcile.getAccount(), 
				amount, true, today, 1, null);
		
		MoveLine creditMoveLine = moveLineService.createMoveLine(move, partnerDebit, debitMoveLineToReconcile.getAccount(), 
				amount, false, today, 2, null);
		
		move.addMoveLineListItem(debitMoveLine);
		move.addMoveLineListItem(creditMoveLine);
				
		moveValidateService.validateMove(move);
		
		return move;
	}


		
}