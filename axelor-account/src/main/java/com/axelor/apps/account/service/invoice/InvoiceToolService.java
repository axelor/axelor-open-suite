/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

/**
 * InvoiceService est une classe implémentant l'ensemble des services de
 * facturations.
 * 
 */
public class InvoiceToolService extends InvoiceRepository  {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceToolService.class);
	
	public static LocalDate getDueDate(PaymentCondition paymentCondition, LocalDate invoiceDate)  {
		
		switch (paymentCondition.getTypeSelect()) {
		case PaymentConditionRepository.TYPE_NET:
			
			return invoiceDate.plusDays(paymentCondition.getPaymentTime());
			
		case PaymentConditionRepository.TYPE_END_OF_MONTH_N_DAYS:
					
			return invoiceDate.dayOfMonth().withMaximumValue().plusDays(paymentCondition.getPaymentTime());
					
		case PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH:
			
			return invoiceDate.plusDays(paymentCondition.getPaymentTime()).dayOfMonth().withMaximumValue();
			
		case PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH_AT:
			
			return invoiceDate.plusDays(paymentCondition.getPaymentTime()).dayOfMonth().withMaximumValue().plusDays(paymentCondition.getDaySelect());

		default:
			return invoiceDate;
		}
		
	}
	
	/**
	 * 
	 * @param invoice
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public static boolean isPurchase(Invoice invoice) throws AxelorException  {
		
		boolean isPurchase;
		
		switch(invoice.getOperationTypeSelect())  {
		case 1:
			isPurchase = true;
			break;
		case 2:
			isPurchase = true;
			break;
		case 3:
			isPurchase = false;
			break;
		case 4:
			isPurchase = false;
			break;
		
		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_1), invoice.getInvoiceId()), IException.MISSING_FIELD);
		}	
		
		return isPurchase;
	}
	
}
