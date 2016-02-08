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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SubscriptionServiceImpl implements SubscriptionService{
	
	@Inject
	private SaleOrderLineService saleOrderLineService;
	
	@Inject
	protected SaleOrderInvoiceServiceImpl saleOrderInvoiceServiceImpl;
	
	@Inject
	private SaleOrderService saleOrderService;

	@Override
	@Transactional
	public SaleOrderLine generateSubscriptions(SaleOrderLine saleOrderLine) throws AxelorException{
		int iterator = 0;

		if(saleOrderLine.getToSubDate() == null){
			throw new AxelorException(I18n.get("Fied Date To is empty because fields periodicity, date from or number of periods are empty"), 1);
		}

		while(iterator != saleOrderLine.getPeriodNumber()){
			Subscription subscription = new Subscription();
			if(saleOrderLine.getSubscripInvTypeSelect() == SaleOrderRepository.SUBSCRIPTION_PERIOD_BEGINNING){
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			}
			else{
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)).minusDays(1));
			}
			subscription.setFromPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			subscription.setToPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)).minusDays(1));
			subscription.setInvoiced(false);
			saleOrderLine.addSubscriptionListItem(subscription);
			iterator++;
		}
		return saleOrderLine;
	}

	@Override
	@Transactional
	public SaleOrderLine generateSubscriptions(SaleOrderLine saleOrderLineIt,SaleOrder saleOrder) throws AxelorException{
		int iterator = 0;
		int oldLine = 0;
		if(saleOrderLineIt.getSubscriptionList() != null && !saleOrderLineIt.getSubscriptionList().isEmpty()){
			oldLine = saleOrderLineIt.getSubscriptionList().size();
		}
		BigDecimal oldExTaxTotal = saleOrderLineIt.getExTaxTotal();
		BigDecimal oldInTaxTotal = saleOrderLineIt.getInTaxTotal();
		if(oldLine != 0){
			oldExTaxTotal = oldExTaxTotal.divide(new BigDecimal(oldLine), 2 ,RoundingMode.HALF_UP);
			oldInTaxTotal = oldInTaxTotal.divide(new BigDecimal(oldLine), 2 ,RoundingMode.HALF_UP);
		}
		if(saleOrder.getToSubDate() == null){
			throw new AxelorException(I18n.get("Field Date To is empty because fields periodicity, date from or number of periods are empty"), 1);
		}

		List<Subscription> subscriptionItList = new ArrayList<Subscription>(saleOrderLineIt.getSubscriptionList());
		for (Subscription subscription : subscriptionItList) {
			if(!subscription.getInvoiced()){
				saleOrderLineIt.removeSubscriptionListItem(subscription);
			}
		}

		while(iterator != saleOrder.getPeriodNumber()){
			Subscription subscription = new Subscription();
			if(saleOrder.getSubscripInvTypeSelect() == SaleOrderRepository.SUBSCRIPTION_PERIOD_BEGINNING){
				subscription.setInvoicingDate(saleOrder.getFromSubDate().plusMonths(saleOrder.getPeriodicity()*iterator));
			}
			else{
				subscription.setInvoicingDate(saleOrder.getFromSubDate().plusMonths(saleOrder.getPeriodicity()*(iterator+1)).minusDays(1));
			}
			subscription.setFromPeriodDate(saleOrder.getFromSubDate().plusMonths(saleOrder.getPeriodicity()*iterator));
			subscription.setToPeriodDate(saleOrder.getFromSubDate().plusMonths(saleOrder.getPeriodicity()*(iterator+1)).minusDays(1));
			subscription.setInvoiced(false);
			saleOrderLineIt.addSubscriptionListItem(subscription);
			iterator++;
		}
		BigDecimal totalLines = new BigDecimal(saleOrderLineIt.getSubscriptionList().size());
		
		if(totalLines.compareTo(BigDecimal.ZERO) != 0){
			saleOrderLineIt.setExTaxTotal(oldExTaxTotal.multiply(totalLines));
			saleOrderLineIt.setInTaxTotal(oldInTaxTotal.multiply(totalLines));
		}
		saleOrderLineIt.setCompanyExTaxTotal(saleOrderLineService.getAmountInCompanyCurrency(saleOrderLineIt.getExTaxTotal(), saleOrder));
		saleOrderLineIt.setCompanyInTaxTotal(saleOrderLineService.getAmountInCompanyCurrency(saleOrderLineIt.getInTaxTotal(), saleOrder));
		

		Beans.get(SaleOrderLineRepository.class).save(saleOrderLineIt);

		return saleOrderLineIt;
	}
	
	@Transactional
	public void generateAllSubscriptions(SaleOrder saleOrder) throws AxelorException{
		for (SaleOrderLine saleOrderLineIt : saleOrder.getSaleOrderLineList()) {
			if(saleOrderLineIt.getProduct().getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE)){
				this.generateSubscriptions(saleOrderLineIt,saleOrder);
			}
		}
		saleOrder = saleOrderService.computeSaleOrder(saleOrder);
		Beans.get(SaleOrderRepository.class).save(saleOrder);
	}
}
