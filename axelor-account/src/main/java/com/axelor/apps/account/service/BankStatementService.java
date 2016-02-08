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

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.BankStatement;
import com.axelor.apps.account.db.BankStatementLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.BankStatementRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BankStatementService {

	protected MoveService moveService;
	protected MoveRepository moveRepository;
	protected MoveLineService moveLineService;
	protected BankStatementRepository bankStatementRepository;
	
	@Inject
	public BankStatementService(MoveService moveService, MoveRepository moveRepository, MoveLineService moveLineService, BankStatementRepository bankStatementRepository)  {
		
		this.moveService = moveService;
		this.moveRepository = moveRepository;
		this.moveLineService = moveLineService;
		this.bankStatementRepository = bankStatementRepository;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void compute(BankStatement bankStatement) throws AxelorException  {

		BigDecimal computedBalance = bankStatement.getStartingBalance();

		for(BankStatementLine bankStatementLine : bankStatement.getBankStatementLineList())  {

			computedBalance = computedBalance.add(bankStatementLine.getAmount());

		}

		bankStatement.setComputedBalance(computedBalance);

		bankStatementRepository.save(bankStatement);
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(BankStatement bankStatement) throws AxelorException  {

		this.checkBalance(bankStatement);

		for(BankStatementLine bankStatementLine : bankStatement.getBankStatementLineList())  {

			if(!bankStatementLine.getIsPosted())  {

				if(bankStatementLine.getMoveLine() == null)  {
					this.validate(bankStatementLine);
				}
				else  {
					this.checkAmount(bankStatementLine);
				}
			}
		}

		bankStatement.setStatusSelect(BankStatementRepository.STATUS_VALIDATED);

		bankStatementRepository.save(bankStatement);
	}

	public void checkBalance(BankStatement bankStatement) throws AxelorException  {

		if(bankStatement.getComputedBalance().compareTo(bankStatement.getEndingBalance()) != 0)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_STATEMENT_1),
					GeneralServiceImpl.EXCEPTION), IException.CONFIGURATION_ERROR);
		}

	}


	public void validate(BankStatementLine bankStatementLine) throws AxelorException  {

		BigDecimal amount = bankStatementLine.getAmount();

		//TODO add currency conversion

		if(amount.compareTo(BigDecimal.ZERO) == 0)  {

			return;

		}

		BankStatement bankStatement = bankStatementLine.getBankStatement();

		Partner partner = bankStatementLine.getPartner();

		LocalDate effectDate = bankStatementLine.getEffectDate();

		String name = bankStatementLine.getName();

		Move move = moveService.getMoveCreateService().createMove(bankStatement.getJournal(), bankStatement.getCompany(), null, partner, effectDate, null);

		boolean isNegate = amount.compareTo(BigDecimal.ZERO) < 0;

		MoveLine partnerMoveLine = moveLineService.createMoveLine(move, partner, bankStatementLine.getAccount(), amount,
				isNegate, effectDate, effectDate, 1, name);
		move.addMoveLineListItem(partnerMoveLine);

		move.addMoveLineListItem(
				moveLineService.createMoveLine(move, partner, bankStatement.getCashAccount(), amount,
						!isNegate, effectDate, effectDate, 1, name));

		moveRepository.save(move);

		moveService.getMoveValidateService().validateMove(move);

		bankStatementLine.setMoveLine(partnerMoveLine);

		bankStatementLine.setIsPosted(true);

	}

	public void checkAmount(BankStatementLine bankStatementLine) throws AxelorException  {

		MoveLine moveLine = bankStatementLine.getMoveLine();

		if(bankStatementLine.getAmount().compareTo(BigDecimal.ZERO) == 0 )  {

			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_STATEMENT_3),
					GeneralServiceImpl.EXCEPTION, bankStatementLine.getReference()), IException.CONFIGURATION_ERROR);
		}

		if((bankStatementLine.getAmount().compareTo(BigDecimal.ZERO) > 0  && bankStatementLine.getAmount().compareTo(moveLine.getCredit()) != 0 )
				|| (bankStatementLine.getAmount().compareTo(BigDecimal.ZERO) < 0  && bankStatementLine.getAmount().compareTo(moveLine.getDebit()) != 0 ) )  {

			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_STATEMENT_2),
					GeneralServiceImpl.EXCEPTION, bankStatementLine.getReference()), IException.CONFIGURATION_ERROR);
		}

	}

}
