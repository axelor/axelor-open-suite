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
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.invoice.workflow.cancel.CancelState;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class CancelStateSupplyChain extends CancelState {

	protected SequenceService sequenceService;
	protected MoveService moveService;

	@Inject
	private SaleOrderInvoiceService saleOrderInvoiceService;

	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;

	@Override
	public void process() throws AxelorException {

		super.process();

		//Update amount remaining to invoiced on SaleOrder
		if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE){
			if (invoice.getSaleOrder() == null){
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
					saleOrder.setAmountRemainingToBeInvoiced(saleOrderInvoiceService.getAmountRemainingToBeInvoiced(saleOrder, invoice.getId(), false));
					JPA.save(saleOrder);
				}

			}else{
				invoice.getSaleOrder().setAmountRemainingToBeInvoiced(saleOrderInvoiceService.getAmountRemainingToBeInvoiced(invoice.getSaleOrder(), invoice.getId(), false));
			}
		}else if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE){
			if (invoice.getPurchaseOrder() == null){
				//Get all different purchaseOrders from invoice
				PurchaseOrder currentPurchaseOrder = null;
				List<PurchaseOrder> purchaseOrderList = new ArrayList<PurchaseOrder>();
				for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
					if (currentPurchaseOrder == null
							|| !currentPurchaseOrder.equals(invoiceLine.getPurchaseOrderLine().getPurchaseOrder())){
						purchaseOrderList.add(invoiceLine.getPurchaseOrderLine().getPurchaseOrder());
						currentPurchaseOrder = invoiceLine.getPurchaseOrderLine().getPurchaseOrder();
					}
				}

				for (PurchaseOrder purchaseOrder : purchaseOrderList) {
					purchaseOrder.setAmountRemainingToBeInvoiced(purchaseOrderInvoiceService.getAmountRemainingToBeInvoiced(purchaseOrder, invoice.getId(), false));
					JPA.save(purchaseOrder);
				}

			}else{
				invoice.getPurchaseOrder().setAmountRemainingToBeInvoiced(purchaseOrderInvoiceService.getAmountRemainingToBeInvoiced(invoice.getPurchaseOrder(), invoice.getId(), false));
			}
		}

	}
}