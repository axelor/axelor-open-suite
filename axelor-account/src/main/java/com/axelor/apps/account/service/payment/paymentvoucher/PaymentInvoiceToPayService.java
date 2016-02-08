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
package com.axelor.apps.account.service.payment.paymentvoucher;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentVoucher;

public class PaymentInvoiceToPayService {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	/**
	 * Generic method for creating invoice to pay lines (2nd O2M in the view)
	 * @param pv
	 * @param seq
	 * @return
	 */
	public PaymentInvoiceToPay createPaymentInvoiceToPay(PaymentVoucher paymentVoucher, int seq, Invoice invoice, MoveLine moveLine, 
			BigDecimal totalAmount, BigDecimal remainingAmount, BigDecimal amountToPay){
		
		log.debug("In  createPaymentInvoiceToPay....");
		
		if (paymentVoucher != null && moveLine != null){
			PaymentInvoiceToPay piToPay= new PaymentInvoiceToPay();
			piToPay.setSequence(seq);
			piToPay.setMoveLine(moveLine);
			piToPay.setTotalAmount(totalAmount);
			piToPay.setRemainingAmount(remainingAmount);
			piToPay.setAmountToPay(amountToPay);
			piToPay.setPaymentVoucher(paymentVoucher);
			
			log.debug("End createPaymentInvoiceToPay IF.");
			
			return piToPay;
		}
		else{
			log.debug("End createPaymentInvoiceToPay ELSE.");
			return null;
		}
	}
	
}
