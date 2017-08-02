/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;

public class InvoiceServiceSupplychainImpl extends InvoiceServiceImpl {

	@Inject
	public InvoiceServiceSupplychainImpl(ValidateFactory validateFactory, VentilateFactory ventilateFactory, CancelFactory cancelFactory,
							  AlarmEngineService<Invoice> alarmEngineService, InvoiceRepository invoiceRepo, AppAccountService appAccountService) {
		super(validateFactory, ventilateFactory, cancelFactory,
				alarmEngineService, invoiceRepo, appAccountService);
	}


	@Override
	public String createAdvancePaymentInvoiceSetDomain(Invoice invoice) {
		SaleOrder saleOrder = invoice.getSaleOrder();
	    if (saleOrder == null) {
	    	return super.createAdvancePaymentInvoiceSetDomain(invoice);
		}

		return "self.operationSubTypeSelect = "
				+ InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE
				+" AND self.saleOrder.id = " + saleOrder.getId();
    }
}
