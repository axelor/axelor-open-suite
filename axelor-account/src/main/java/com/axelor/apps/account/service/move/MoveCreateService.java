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

import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.CashRegister;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class MoveCreateService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected PeriodService periodService;
	protected MoveRepository moveRepository;

	protected LocalDate today;

	@Inject
	public MoveCreateService(GeneralService generalService, PeriodService periodService, MoveRepository moveRepository) {

		this.periodService = periodService;
		this.moveRepository = moveRepository;
		
		today = generalService.getTodayDate();

	}

	
	/**
	 * Créer une écriture comptable à la date du jour impactant la compta.
	 *
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @return
	 * @throws AxelorException
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, PaymentMode paymentMode) throws AxelorException{

		return this.createMove(journal, company, invoice, partner, today, paymentMode);
	}


	/**
	 * Créer une écriture comptable impactant la compta.
	 *
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param dateTime
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @return
	 * @throws AxelorException
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, LocalDate date, PaymentMode paymentMode) throws AxelorException{

		return this.createMove(journal, company, invoice, partner, date, paymentMode, false, false);

	}


	/**
	 * Créer une écriture comptable de toute pièce en passant tous les paramètres qu'il faut.
	 *
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param dateTime
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @return
	 * @throws AxelorException
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, LocalDate date, PaymentMode paymentMode, boolean ignoreInReminderOk,
			boolean ignoreInAccountingOk) throws AxelorException{

		log.debug("Création d'une écriture comptable (journal : {}, société : {}", new Object[]{journal.getName(), company.getName()});

		Move move = new Move();

		move.setJournal(journal);
		move.setCompany(company);

		move.setIgnoreInReminderOk(ignoreInReminderOk);
		move.setIgnoreInAccountingOk(ignoreInAccountingOk);

		Period period = periodService.rightPeriod(date, company);

		move.setPeriod(period);
		move.setDate(date);
		move.setMoveLineList(new ArrayList<MoveLine>());

		if (invoice != null)  {
			move.setInvoice(invoice);
		}
		if (partner != null)  {
			move.setPartner(partner);
			move.setCurrency(partner.getCurrency());
		}
		move.setPaymentMode(paymentMode);

		moveRepository.save(move);
		move.setReference("*"+move.getId());

		return move;

	}


	/**
	 * Créer une écriture comptable de toute pièce spécifique à une saisie paiement.
	 *
	 * @param journal
	 * @param period
	 * @param company
	 * @param invoice
	 * @param partner
	 * @param isReject
	 * 		<code>true = écriture de rejet avec séquence spécifique</code>
	 * @param agency
	 * 		L'agence dans laquelle s'effectue le paiement
	 * @return
	 * @throws AxelorException
	 */
	public Move createMove(Journal journal, Company company, Invoice invoice, Partner partner, LocalDate date, PaymentMode paymentMode, CashRegister cashRegister) throws AxelorException{

		Move move = this.createMove(journal, company, invoice, partner, date, paymentMode);
		move.setCashRegister(cashRegister);
		return move;
	}


}