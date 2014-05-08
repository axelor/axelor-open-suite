package com.axelor.csv.script;

import java.util.Map;

import com.axelor.apps.account.db.IPaymentVoucher;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherLoadService;
import com.google.inject.Inject;

public class ImportPaymentVoucher {
	
	@Inject
	PaymentVoucherLoadService paymentVoucherLoadService;
	
	@Inject
	PaymentVoucherConfirmService paymentVoucherConfirmService;
	
	
	public Object importPaymentVoucher(Object bean, Map values) {
		assert bean instanceof PaymentVoucher;
		try{
			PaymentVoucher paymentVoucher = (PaymentVoucher)bean;
			paymentVoucherLoadService.loadMoveLines(paymentVoucher);
			if(paymentVoucher.getStateSelect() == IPaymentVoucher.STATE_CONFIRMED)
				paymentVoucherConfirmService.confirmPaymentVoucher(paymentVoucher, false);
			return paymentVoucher;
		}catch(Exception e){
	            e.printStackTrace();
	    }
		return bean;
	}
}
