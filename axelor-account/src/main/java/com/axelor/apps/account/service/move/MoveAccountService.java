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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveAccountService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveCustAccountService moveCustAccountService;
	protected MoveRepository moveRepository;
	protected MoveValidateService moveValidateService;

	@Inject
	public MoveAccountService(MoveCustAccountService moveCustAccountService, MoveRepository moveRepository, MoveValidateService moveValidateService) {
		this.moveCustAccountService = moveCustAccountService;
		this.moveRepository = moveRepository;
		this.moveValidateService = moveValidateService;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void account(Move move) throws AxelorException  {

		moveValidateService.completeMoveLines(move);

		this.accountMove(move);
		moveRepository.save(move);
	}


	/**
	 * Comptabiliser une écriture comptable.
	 *
	 * @param move
	 *
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void accountMove(Move move) throws AxelorException {
		this.accountMove(move, true);
	}



	/**
	 * Comptabiliser une écriture comptable.
	 *
	 * @param move
	 *
	 * @throws AxelorException
	 */
	public void accountMove(Move move, boolean updateCustomerAccount) throws AxelorException {

		log.debug("Comptabilisation de l'écriture comptable {}", move.getReference());
		Journal journal = move.getJournal();
		Company company = move.getCompany();
		if (journal == null) {
			throw new AxelorException(move, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_2));
		}
		if (company == null) {
			throw new AxelorException(move, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_3));
		}
		if (move.getPeriod() == null) {
			throw new AxelorException(move, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_4));
		}
		if (journal.getSequence() == null) {
			throw new AxelorException(move, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.MOVE_2), journal.getName());
		}

		this.accountEquiponderanteMove(move);
		this.fillMoveLines(move);
		moveRepository.save(move);

		moveCustAccountService.updateCustomerAccount(move);

		move.setValidationDate(LocalDate.now());

	}



	/**
	 * Procédure permettant de vérifier qu'une écriture est équilibrée, et la comptabiliser si c'est le cas
	 * @param move
	 * 			Une écriture
	 * @throws AxelorException
	 */
	public void accountEquiponderanteMove(Move move) throws AxelorException {

		log.debug("Comptabilisation de l'écriture comptable {}", move.getReference());

		if (move.getMoveLineList() != null){

			BigDecimal totalDebit = BigDecimal.ZERO;
			BigDecimal totalCredit = BigDecimal.ZERO;

			for (MoveLine moveLine : move.getMoveLineList()){

				if(moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0 && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
					throw new AxelorException(move, TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.MOVE_6), moveLine.getName());
				}

				totalDebit = totalDebit.add(moveLine.getDebit());
				totalCredit = totalCredit.add(moveLine.getCredit());
			}

			if (totalDebit.compareTo(totalCredit) != 0) {
				throw new AxelorException(move, TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(IExceptionMessage.MOVE_7),
						move.getReference(), totalDebit, totalCredit);
			}
			move.setStatusSelect(MoveRepository.STATUS_DAYBOOK);
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
	
	public boolean accountMultiple(List<? extends Move> moveList){
		boolean error = false;
		for(Move move: moveList){
			try{
				account(move);
			}catch (Exception e){
				TraceBackService.trace(e);
				error = true;
			}
		}
		return error;
	}
	
		
}