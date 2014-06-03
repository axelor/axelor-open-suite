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
package com.axelor.apps.account.service.payment.paymentvoucher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class PaymentVoucherToolService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherToolService.class); 
	
	/**
	 * 
	 * @param paymentVoucher : Une saisie Paiement
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public boolean isDebitToPay(PaymentVoucher paymentVoucher) throws AxelorException  {
		boolean isDebitToPay;
		
		switch(paymentVoucher.getOperationTypeSelect())  {
		case 1:
			isDebitToPay = false;
			break;
		case 2:
			isDebitToPay = true;
			break;
		case 3:
			isDebitToPay = true;
			break;
		case 4:
			isDebitToPay = false;
			break;
		
		default:
			throw new AxelorException(String.format("Type de la saisie paiement absent de la saisie paiement %s", paymentVoucher.getRef()), IException.MISSING_FIELD);
		}	
		
		return isDebitToPay;
	}
	
	
	
	/**
	 * 
	 * @param paymentVoucher : Une saisie Paiement
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public boolean isPurchase(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		boolean isPurchase;
		
		switch(paymentVoucher.getOperationTypeSelect())  {
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
			throw new AxelorException(String.format("Type de la saisie paiement absent de la saisie paiement %s", paymentVoucher.getRef()), IException.MISSING_FIELD);
		}	
		
		return isPurchase;
	}
	
}
