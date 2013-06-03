package com.axelor.apps.account.service.payment;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentVoucher;

public class PaymentInvoiceToPayService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentInvoiceToPayService.class); 
	
	/**
	 * Generic method for creating invoice to pay lines (2nd O2M in the view)
	 * @param pv
	 * @param seq
	 * @return
	 */
	public PaymentInvoiceToPay createPaymentInvoiceToPay(PaymentVoucher pv,int seq,Invoice invoice,MoveLine ml,BigDecimal totalAmount,BigDecimal remainingAmount,BigDecimal amountToPay){
		
		LOG.debug("In  createPaymentInvoiceToPay....");
		
		if (pv != null && ml != null){
			PaymentInvoiceToPay piToPay= new PaymentInvoiceToPay();
			piToPay.setPaymentScheduleLine(pv.getScheduleToPay());
			piToPay.setSequence(seq);
			piToPay.setMoveLine(ml);
			piToPay.setTotalAmount(totalAmount);
			piToPay.setRemainingAmount(remainingAmount);
			piToPay.setAmountToPay(amountToPay);
			piToPay.setPaymentVoucher(pv);
			
			LOG.debug("End createPaymentInvoiceToPay IF.");
			
			return piToPay;
		}
		else{
			LOG.debug("End createPaymentInvoiceToPay ELSE.");
			return null;
		}
	}
	
}
