package com.axelor.csv.script

import java.util.Map;

import com.axelor.apps.account.db.PaymentInvoice
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.PaymentVoucherService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

class ImportPaymentVoucher {
	
	@Inject
	PaymentVoucherService pvs;
	
	Object importPaymentVoucher(Object bean, Map values) {
		assert bean instanceof PaymentVoucher
		try{
			PaymentVoucher paymentVoucher = (PaymentVoucher)bean
			pvs.loadMoveLines(paymentVoucher);
			if(paymentVoucher.state.equals("2"))
				pvs.confirmPaymentVoucher(paymentVoucher, false);
			return paymentVoucher
		}catch(Exception e){
	            e.printStackTrace()
	    }
		return bean
	}
}
