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
package com.axelor.apps.supplychain.service.workflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.workflow.ventilate.VentilateState;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class VentilateStateSupplyChain extends VentilateState {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );

	@Inject
	private SaleOrderInvoiceService saleOrderInvoiceService;

	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
	
	@Inject
	protected SaleOrderRepository saleOrderRepository;
	
	@Inject 
	protected PurchaseOrderRepository purchaseOrderRepository;

	
	@Override
	public void process() throws AxelorException {

		super.process();
		
		if (InvoiceToolService.isPurchase(invoice))  {
			
			//Update amount invoiced on PurchaseOrder
			this.purchaseOrderProcess();
			
		}  else  {
			
			//Update amount remaining to invoiced on SaleOrder
			this.saleOrderProcess();
			
		}

	}
	
	public void saleOrderProcess() throws AxelorException  {
		
		SaleOrder invoiceSaleOrder = invoice.getSaleOrder();
		
		if (invoiceSaleOrder != null)  {
			
			log.debug("Update the invoiced amount of the sale order : {}", invoiceSaleOrder.getSaleOrderSeq());
			invoiceSaleOrder.setAmountInvoiced(saleOrderInvoiceService.getInvoicedAmount(invoiceSaleOrder, invoice.getId(), false));

		}  else  {
			
			//Get all different saleOrders from invoice
			List<SaleOrder> saleOrderList = Lists.newArrayList();
			
			for (InvoiceLine invoiceLine : invoice.getInvoiceLineList())  {
				
				SaleOrder saleOrder = this.saleOrderLineProcess(invoiceLine);
				
				if (saleOrder != null && !saleOrderList.contains(saleOrder))  {   saleOrderList.add(saleOrder);  }

			}

			for (SaleOrder saleOrder : saleOrderList) {
				log.debug("Update the invoiced amount of the sale order : {}", saleOrder.getSaleOrderSeq());
				saleOrder.setAmountInvoiced(saleOrderInvoiceService.getInvoicedAmount(saleOrder, invoice.getId(), false));
				saleOrderRepository.save(saleOrder);
			}
		}
		
	}
	
	
	public SaleOrder saleOrderLineProcess(InvoiceLine invoiceLine) throws AxelorException  {
		
		SaleOrderLine saleOrderLine = invoiceLine.getSaleOrderLine();
		
		if (saleOrderLine == null)  {  return null;  }
		
		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		
		//Update invoiced amount on sale order line
		BigDecimal invoicedAmountToAdd = invoiceLine.getExTaxTotal();
		
		// If is it a refund invoice, so we negate the amount invoiced
		if(InvoiceToolService.isRefund(invoiceLine.getInvoice()))  {  invoicedAmountToAdd = invoicedAmountToAdd.negate();  }
		
		if (!invoice.getCurrency().equals(saleOrder.getCurrency()) && saleOrderLine.getCompanyExTaxTotal().compareTo(BigDecimal.ZERO) != 0)  {
			//If the sale order currency is different from the invoice currency, use company currency to calculate a rate. This rate will be applied to sale order line
			BigDecimal currentCompanyInvoicedAmount = invoiceLine.getCompanyExTaxTotal();
			BigDecimal rate = currentCompanyInvoicedAmount.divide(saleOrderLine.getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
			invoicedAmountToAdd = rate.multiply(saleOrderLine.getExTaxTotal());
		}
		
		saleOrderLine.setAmountInvoiced(saleOrderLine.getAmountInvoiced().add(invoicedAmountToAdd));
		
		return saleOrder;
		
	}
	
	
	public void purchaseOrderProcess() throws AxelorException  {
		
		PurchaseOrder invoicePurchaseOrder = invoice.getPurchaseOrder();
		
		if (invoicePurchaseOrder != null)  {
			
			log.debug("Update the invoiced amount of the purchase order : {}", invoicePurchaseOrder.getPurchaseOrderSeq());
			invoicePurchaseOrder.setAmountInvoiced(purchaseOrderInvoiceService.getInvoicedAmount(invoicePurchaseOrder, invoice.getId(), false));

		}  else  {
			
			//Get all different purchaseOrders from invoice

			List<PurchaseOrder> purchaseOrderList = Lists.newArrayList();;
			
			for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
				
				PurchaseOrder purchaseOrder = this.purchaseOrderLineProcess(invoiceLine);
				
				if(purchaseOrder != null && !purchaseOrderList.contains(purchaseOrder))  {  purchaseOrderList.add(purchaseOrder);  }
				
			}
			
			for (PurchaseOrder purchaseOrder : purchaseOrderList)  {
				log.debug("Update the invoiced amount of the purchase order : {}", purchaseOrder.getPurchaseOrderSeq());
				purchaseOrder.setAmountInvoiced(purchaseOrderInvoiceService.getInvoicedAmount(purchaseOrder, invoice.getId(), false));
				purchaseOrderRepository.save(purchaseOrder);
			}
		}
	}

	
	public PurchaseOrder purchaseOrderLineProcess(InvoiceLine invoiceLine) throws AxelorException  {
		
		PurchaseOrderLine purchaseOrderLine = invoiceLine.getPurchaseOrderLine();
		
		if(purchaseOrderLine == null)  {  return null;  }
		
		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		
		BigDecimal invoicedAmountToAdd = invoiceLine.getExTaxTotal();
		
		// If is it a refund invoice, so we negate the amount invoiced
		if(InvoiceToolService.isRefund(invoiceLine.getInvoice()))  {  invoicedAmountToAdd = invoicedAmountToAdd.negate();  }
		
		//Update invoiced amount on purchase order line
		if (!invoice.getCurrency().equals(purchaseOrder.getCurrency()) && purchaseOrderLine.getCompanyExTaxTotal().compareTo(BigDecimal.ZERO) != 0){
			//If the purchase order currency is different from the invoice currency, use company currency to calculate a rate. This rate will be applied to purchase order line
			BigDecimal currentCompanyInvoicedAmount = invoiceLine.getCompanyExTaxTotal();
			BigDecimal rate = currentCompanyInvoicedAmount.divide(purchaseOrderLine.getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
			invoicedAmountToAdd = rate.multiply(purchaseOrderLine.getExTaxTotal());
		}
		
		purchaseOrderLine.setAmountInvoiced(purchaseOrderLine.getAmountInvoiced().add(invoicedAmountToAdd));
		
		return purchaseOrder;
		
	}
	
	
}