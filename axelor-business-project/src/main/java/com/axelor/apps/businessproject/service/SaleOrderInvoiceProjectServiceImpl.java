/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderInvoiceProjectServiceImpl extends SaleOrderInvoiceServiceImpl{
	
	@Inject
	public GeneralService generalService;
	
	@Inject
	public SaleOrderInvoiceProjectServiceImpl(GeneralService generalService) {
		super(generalService);
	}

	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {

			//Lines of subscription type are invoiced directly from sale order line or from the subscription batch
			
			if (saleOrderLine.getProduct() == null || (saleOrderLine.getProduct() != null && !ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(saleOrderLine.getProduct().getProductTypeSelect()))){
				invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderLine));
				invoiceLineList.get(invoiceLineList.size()-1).setProject(saleOrderLine.getProject());
				saleOrderLine.setInvoiced(true);
			}
		}

		return invoiceLineList;

	}

	@Override
	public List<InvoiceLine> createSubscriptionInvoiceLines(Invoice invoice, List<Subscription> subscriptionList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		Integer sequence = 10;

		for(Subscription subscription : subscriptionList)  {

			invoiceLineList.addAll(this.createSubscriptionInvoiceLine(invoice, subscription, sequence));
			invoiceLineList.get(invoiceLineList.size()-1).setProject(subscription.getSaleOrderLine().getProject());
			subscription.setInvoiced(true);

			sequence += 10;
		}

		return invoiceLineList;

	}

	@Transactional
	public Invoice mergeInvoice(List<Invoice> invoiceList, Company company, Currency currency,
			Partner partner, Partner contactPartner, PriceList priceList,
			PaymentMode paymentMode, PaymentCondition paymentCondition, SaleOrder saleOrder,ProjectTask project)
					throws AxelorException {
		Invoice invoiceMerged = super.mergeInvoice(invoiceList,company,currency,partner,contactPartner,priceList,paymentMode,paymentCondition,saleOrder);
		if (project != null){
			if(!generalService.getGeneral().getProjectTaskInvoiceLines()){
				invoiceMerged.setProject(project);
				for (InvoiceLine invoiceLine : invoiceMerged.getInvoiceLineList()){
					invoiceLine.setProject(project);
				}
			}
		}
		return invoiceMerged;
	}
}
