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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.workflow.ventilate.VentilateState;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class VentilateStateSupplyChain extends VentilateState {

	@Inject
	private SaleOrderInvoiceService saleOrderInvoiceService;

	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;

	@Override
	public void process( ) throws AxelorException {

		super.process();
		if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE){
			//Update amount invoiced on SaleOrder
			if (invoice.getSaleOrder() == null){
				//Get all different saleOrders from invoice
				SaleOrder currentSaleOrder = null;
				List<SaleOrder> saleOrderList = new ArrayList<SaleOrder>();
				for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
					if (invoiceLine.getSaleOrderLine() != null){
						if (currentSaleOrder == null
								|| !currentSaleOrder.equals(invoiceLine.getSaleOrderLine().getSaleOrder())){
							saleOrderList.add(invoiceLine.getSaleOrderLine().getSaleOrder());
							currentSaleOrder = invoiceLine.getSaleOrderLine().getSaleOrder();
						}
						//Update invoiced amount on sale order line
						BigDecimal invoicedAmountToAdd = invoiceLine.getExTaxTotal();
						if (!invoice.getCurrency().equals(invoiceLine.getSaleOrderLine().getSaleOrder().getCurrency())){
							//If the sale order currency is different from the invoice currency, use company currency to calculate a rate. This rate will be applied to sale order line
							BigDecimal currentCompanyInvoicedAmount = invoiceLine.getCompanyExTaxTotal();
							BigDecimal rate = currentCompanyInvoicedAmount.divide(invoiceLine.getSaleOrderLine().getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
							invoicedAmountToAdd = rate.multiply(invoiceLine.getSaleOrderLine().getExTaxTotal());
						}
						invoiceLine.getSaleOrderLine().setAmountInvoiced(invoiceLine.getSaleOrderLine().getAmountInvoiced().add(invoicedAmountToAdd));
						JPA.save(invoiceLine.getSaleOrderLine());
					}
				}

				for (SaleOrder saleOrder : saleOrderList) {
					saleOrder.setAmountInvoiced(saleOrderInvoiceService.getAmountInvoiced(saleOrder, invoice.getId(), true));
					JPA.save(saleOrder);
				}

			}else{
				invoice.getSaleOrder().setAmountInvoiced(saleOrderInvoiceService.getAmountInvoiced(invoice.getSaleOrder(), invoice.getId(), true));
			}
		}else if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE){
			//Update amount invoiced on PurchaseOrder
			if (invoice.getPurchaseOrder() == null){
				//Get all different saleOrders from invoice
				PurchaseOrder currentPurchaseOrder = null;
				List<PurchaseOrder> purchaseOrderList = new ArrayList<PurchaseOrder>();
				for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
					if (currentPurchaseOrder == null
							|| !currentPurchaseOrder.equals(invoiceLine.getPurchaseOrderLine().getPurchaseOrder())){
						purchaseOrderList.add(invoiceLine.getPurchaseOrderLine().getPurchaseOrder());
						currentPurchaseOrder = invoiceLine.getPurchaseOrderLine().getPurchaseOrder();
					}
					//Update invoiced amount on purchase order line
					BigDecimal invoicedAmountToAdd = invoiceLine.getExTaxTotal();
					if (!invoice.getCurrency().equals(invoiceLine.getPurchaseOrderLine().getPurchaseOrder().getCurrency())){
						//If the purchase order currency is different from the invoice currency, use company currency to calculate a rate. This rate will be applied to purchase order line
						BigDecimal currentCompanyInvoicedAmount = invoiceLine.getCompanyExTaxTotal();
						BigDecimal rate = currentCompanyInvoicedAmount.divide(invoiceLine.getPurchaseOrderLine().getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
						invoicedAmountToAdd = rate.multiply(invoiceLine.getPurchaseOrderLine().getExTaxTotal());
					}
					invoiceLine.getPurchaseOrderLine().setAmountInvoiced(invoiceLine.getPurchaseOrderLine().getAmountInvoiced().add(invoicedAmountToAdd));
					JPA.save(invoiceLine.getPurchaseOrderLine());
				}

				for (PurchaseOrder purchaseOrder : purchaseOrderList) {
					purchaseOrder.setAmountInvoiced(purchaseOrderInvoiceService.getAmountInvoiced(purchaseOrder, invoice.getId(), true));
					JPA.save(purchaseOrder);
				}

			}else{
				invoice.getPurchaseOrder().setAmountInvoiced(purchaseOrderInvoiceService.getAmountInvoiced(invoice.getPurchaseOrder(), invoice.getId(), true));
			}
		}
	}
}