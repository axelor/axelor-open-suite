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
package com.axelor.csv.script;

import java.util.Map;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
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
			if(paymentVoucher.getStatusSelect() == PaymentVoucherRepository.STATUS_CONFIRMED)
				paymentVoucherConfirmService.confirmPaymentVoucher(paymentVoucher);
			return paymentVoucher;
		}catch(Exception e){
	            e.printStackTrace();
	    }
		return bean;
	}
}
