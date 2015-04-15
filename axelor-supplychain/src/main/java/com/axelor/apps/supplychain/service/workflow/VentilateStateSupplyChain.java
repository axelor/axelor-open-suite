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
package com.axelor.apps.supplychain.service.workflow;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.workflow.ventilate.VentilateState;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class VentilateStateSupplyChain extends VentilateState {

	@Inject
	private SaleOrderInvoiceService saleOrderInvoiceService;

	@Override
	public void process( ) throws AxelorException {

		super.process();
		//Update amount remaining to invoiced on SaleOrder
		if (GeneralService.getGeneral().getManageAmountInvoiceByLine()){
			//Get all different saleOrders from invoice
			SaleOrder currentSaleOrder = null;
			List<SaleOrder> saleOrderList = new ArrayList<SaleOrder>();
			for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
				if (currentSaleOrder == null
						|| !currentSaleOrder.equals(invoiceLine.getSaleOrderLine().getSaleOrder())){
					saleOrderList.add(invoiceLine.getSaleOrderLine().getSaleOrder());
					currentSaleOrder = invoiceLine.getSaleOrderLine().getSaleOrder();
				}
			}

			for (SaleOrder saleOrder : saleOrderList) {
				saleOrder.setAmountRemainingToBeInvoiced(saleOrderInvoiceService.getAmountRemainingToBeInvoiced(saleOrder, invoice.getId(), true));
				JPA.save(saleOrder);
			}

		}else{
			invoice.getSaleOrder().setAmountRemainingToBeInvoiced(saleOrderInvoiceService.getAmountRemainingToBeInvoiced(invoice.getSaleOrder(), invoice.getId(), true));
		}
	}
}