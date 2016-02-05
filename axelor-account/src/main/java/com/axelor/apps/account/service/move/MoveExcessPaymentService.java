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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class MoveExcessPaymentService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveLineRepository moveLineRepository;
	protected MoveToolService moveToolService;

	
	@Inject
	public MoveExcessPaymentService(MoveLineRepository moveLineRepository, MoveToolService moveToolService) {

		this.moveLineRepository = moveLineRepository;
		this.moveToolService = moveToolService;

	}
	
	
	/**
	 * Méthode permettant de récupérer les trop-perçus pour un compte donné (411) et une facture
	 * @param invoice
	 * 			Une facture
	 * @param account
	 * 			Un compte
	 * @return
	 * @throws AxelorException
	 */
	public List<MoveLine> getExcessPayment(Invoice invoice, Account account) throws AxelorException {
		 Company company = invoice.getCompany();

		 List<MoveLine> creditMoveLines =  moveLineRepository.all()
		 .filter("self.move.company = ?1 AND self.move.statusSelect = ?2 AND self.move.ignoreInAccountingOk IN (false,null)" +
		 " AND self.account.reconcileOk = ?3 AND self.credit > 0 and self.amountRemaining > 0" +
		 " AND self.partner = ?4 AND self.account = ?5 ORDER BY self.date ASC",
		 company, MoveRepository.STATUS_VALIDATED, true, invoice.getPartner(), account).fetch();

		 log.debug("Nombre de trop-perçus à imputer sur la facture récupéré : {}", creditMoveLines.size());

		 return creditMoveLines;
	}
		
	
	public List<MoveLine> getAdvancePaymentMoveList(Invoice invoice)  {
		
		List<MoveLine> moveLineList = Lists.newArrayList();
		
		if (invoice.getInvoicePaymentList() != null)  {
			
			for (InvoicePayment invoicePayment : invoice.getInvoicePaymentList())  {
				
				for (MoveLine moveLine : invoicePayment.getMove().getMoveLineList())  {
					
					if (moveLine.getCredit().compareTo(BigDecimal.ZERO) != 0)  {
						moveLineList.add(moveLine);
					}
				}
			}
			
			return moveToolService.orderListByDate(moveLineList);
		}
		
		return moveLineList;
	}
	
	
}