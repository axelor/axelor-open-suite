/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveValidateService {

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	protected SequenceService sequenceService;
	protected MoveCustAccountService moveCustAccountService;
	protected MoveRepository moveRepository;

	@Inject
	public MoveValidateService(AppAccountService appAccountService, SequenceService sequenceService, MoveCustAccountService moveCustAccountService, MoveRepository moveRepository) {

		this.sequenceService = sequenceService;
		this.moveCustAccountService = moveCustAccountService;
		this.moveRepository = moveRepository;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(Move move) throws AxelorException  {

	    completeMoveLines(move);

		this.validateMove(move);
		moveRepository.save(move);
	}

	/**
	 * In move lines, fill the dates field and the partner if they are missing,
	 * and fill the counter.
	 * @param move
	 */
	public void completeMoveLines(Move move) {
		LocalDate date = move.getDate();
		Partner partner = move.getPartner();

		int counter = 1;
		for(MoveLine moveLine : move.getMoveLineList())  {
			if (moveLine.getDate() == null) {
				moveLine.setDate(date);
			}

			if(moveLine.getAccount() != null && moveLine.getAccount().getUseForPartnerBalance() && moveLine.getDueDate() == null)  {
				moveLine.setDueDate(date);
			}
			if (partner != null){
				moveLine.setPartner(partner);
			}
			moveLine.setCounter(counter);
			counter++;
		}
	}


	/**
	 * Valider une écriture comptable.
	 *
	 * @param move
	 *
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validateMove(Move move) throws AxelorException {

		this.validateMove(move, true);

	}



	/**
	 * Valider une écriture comptable.
	 *
	 * @param move
	 *
	 * @throws AxelorException
	 */
	public void validateMove(Move move, boolean updateCustomerAccount) throws AxelorException {

		log.debug("Validation de l'écriture comptable {}", move.getReference());
		Journal journal = move.getJournal();
		Company company = move.getCompany();
		if(journal == null)  {
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_2));
		}
		if(company == null)  {
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_3));
		}

		if(move.getPeriod() == null)  {
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_4));
		}

		if (journal.getSequence() == null)  {
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_5), journal.getName());
		}

		if (move.getMoveLineList() == null || move.getMoveLineList().isEmpty()) {
			throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.MOVE_8));
		}

		if (move.getMoveLineList().stream()
				.allMatch(moveLine ->
						moveLine.getDebit().add(moveLine.getCredit())
								.compareTo(BigDecimal.ZERO) == 0
				)) {
			throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.MOVE_8));
		}

		move.setReference(sequenceService.getSequenceNumber(journal.getSequence()));

		if (move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_ADJUSTING) {
			move.setAdjustingMove(true);
		}

		this.validateEquiponderanteMove(move);
		this.fillMoveLines(move);
		moveRepository.save(move);
			
		moveCustAccountService.updateCustomerAccount(move);

		move.setValidationDate(LocalDate.now());

	}



	/**
	 * Procédure permettant de vérifier qu'une écriture est équilibré, et la validé si c'est le cas
	 * @param move
	 * 			Une écriture
	 * @throws AxelorException
	 */
	public void validateEquiponderanteMove(Move move) throws AxelorException {

		log.debug("Validation de l'écriture comptable {}", move.getReference());

		if (move.getMoveLineList() != null){

			BigDecimal totalDebit = BigDecimal.ZERO;
			BigDecimal totalCredit = BigDecimal.ZERO;

			for (MoveLine moveLine : move.getMoveLineList()){

				if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0 && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
					throw new AxelorException(move, TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.MOVE_6), moveLine.getName());
				}

				totalDebit = totalDebit.add(moveLine.getDebit());
				totalCredit = totalCredit.add(moveLine.getCredit());
			}

			if (totalDebit.compareTo(totalCredit) != 0){
				throw new AxelorException(move, TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.MOVE_7), move.getReference(), totalDebit, totalCredit);
			}
			move.setStatusSelect(MoveRepository.STATUS_VALIDATED);
		}
	}

	//Procédure permettant de remplir les champs dans les lignes d'écriture relatifs au compte comptable et au tiers
	@Transactional
	public void fillMoveLines(Move move){
		for (MoveLine moveLine : move.getMoveLineList()) {
			moveLine.setAccountCode(moveLine.getAccount().getCode());
			moveLine.setAccountName(moveLine.getAccount().getName());
			if(move.getPartner() != null){
				moveLine.setPartnerFullName(move.getPartner().getFullName());
				moveLine.setPartnerSeq(move.getPartner().getPartnerSeq());
			}else if(moveLine.getPartner() != null){
				moveLine.setPartnerFullName(moveLine.getPartner().getFullName());
				moveLine.setPartnerSeq(moveLine.getPartner().getPartnerSeq());
			}
		}
	}
	
	public boolean validateMultiple(List<? extends Move> moveList){
		boolean error = false;
		for(Move move: moveList){
			try{
				validate(move);
			}catch (Exception e){
				TraceBackService.trace(e);
				error = true;
			}
		}
		return error;
	}
	
		
}