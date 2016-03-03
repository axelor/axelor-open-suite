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
package com.axelor.apps.supplychain.service.invoice.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public abstract class InvoiceGeneratorSupplyChain extends InvoiceGenerator {

	@Inject
	protected GeneralService generalService;

	protected SaleOrder saleOrder;

	protected PurchaseOrder purchaseOrder;

	protected InvoiceGeneratorSupplyChain(SaleOrder saleOrder) throws AxelorException {

		super(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, saleOrder.getCompany(), saleOrder.getPaymentCondition(), saleOrder.getPaymentMode(), saleOrder.getMainInvoicingAddress(), 
				saleOrder.getClientPartner(), saleOrder.getContactPartner(), saleOrder.getCurrency(), saleOrder.getPriceList(), saleOrder.getSaleOrderSeq(), 
				saleOrder.getExternalReference(), saleOrder.getInAti(), saleOrder.getCompanyBankDetails());
		this.saleOrder = saleOrder;
		
	}

	protected InvoiceGeneratorSupplyChain(PurchaseOrder purchaseOrder) throws AxelorException {

		super(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE, purchaseOrder.getCompany(), purchaseOrder.getPaymentCondition(), purchaseOrder.getPaymentMode(), null, 
				purchaseOrder.getSupplierPartner(), purchaseOrder.getContactPartner(), purchaseOrder.getCurrency(), purchaseOrder.getPriceList(), purchaseOrder.getPurchaseOrderSeq(),  
				purchaseOrder.getExternalReference(), purchaseOrder.getInAti(), purchaseOrder.getCompanyBankDetails());
		this.purchaseOrder = purchaseOrder;
		
	}

	/**
	 * PaymentCondition, Paymentmode, MainInvoicingAddress, Currency récupérés du tiers
	 * @param operationType
	 * @param company
	 * @param partner
	 * @param contactPartner
	 * @throws AxelorException
	 */
	protected InvoiceGeneratorSupplyChain(StockMove stockMove, int invoiceOperationType) throws AxelorException {

		super(invoiceOperationType, stockMove.getCompany(), stockMove.getPartner(), null, null, stockMove.getStockMoveSeq(), stockMove.getTrackingNumber(), null);
		
	}


	@Override
	protected Invoice createInvoiceHeader() throws AxelorException  {

		Invoice invoice = super.createInvoiceHeader();

		if (!Beans.get(GeneralService.class).getGeneral().getManageInvoicedAmountByLine())  {
			if(saleOrder != null)  {
				invoice.setSaleOrder(saleOrder);
				
			}  else  {
				invoice.setPurchaseOrder(purchaseOrder);
			}
		}
		
		return invoice;
	}

}
