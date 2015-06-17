package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.db.repo.SubscriptionRepository;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SubscriptionServiceImpl extends SubscriptionRepository implements SubscriptionService{

	@Inject
	protected SaleOrderInvoiceServiceImpl saleOrderInvoiceServiceImpl;

	@Override
	@Transactional
	public SaleOrderLine generateSubscriptions(SaleOrderLine saleOrderLine) throws AxelorException{
		int iterator = 0;

		if(saleOrderLine.getToSubDate() == null){
			throw new AxelorException(I18n.get("Fied Date To is empty because fields periodicity, date from or number of periods are empty"), 1);
		}

		while(iterator != saleOrderLine.getPeriodNumber()){
			Subscription subscription = new Subscription();
			if(saleOrderLine.getInvoicingTypeSelect() == 1){
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			}
			else{
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)).minusDays(1));
			}
			subscription.setFromPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			subscription.setToPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)).minusDays(1));
			subscription.setQuantity(saleOrderLine.getQty());
			if(saleOrderLine.getPeriodicity() == saleOrderLine.getProduct().getPeriodicitySelect()){
				subscription.setUnitPrice(saleOrderLine.getPrice().multiply(new BigDecimal(saleOrderLine.getPeriodicity())));
			}
			else{
				subscription.setUnitPrice(saleOrderLine.getPrice().multiply(new BigDecimal(saleOrderLine.getPeriodicity()).divide(new BigDecimal(saleOrderLine.getProduct().getPeriodicitySelect()))));
			}
			subscription.setInvoiced(false);
			saleOrderLine.addSubscriptionListItem(subscription);
			iterator++;
		}
		return saleOrderLine;
	}

	@Override
	@Transactional
	public SaleOrderLine generateSubscriptions(SaleOrderLine saleOrderLineIt,SaleOrderLine saleOrderLine) throws AxelorException{
		int iterator = 0;

		if(saleOrderLine.getToSubDate() == null){
			throw new AxelorException(I18n.get("Fied Date To is empty because fields periodicity, date from or number of periods are empty"), 1);
		}

		for (Subscription subscription : saleOrderLineIt.getSubscriptionList()) {
			if(!subscription.getInvoiced()){
				subscription.setSaleOrderLine(null);
			}
		}

		while(iterator != saleOrderLine.getPeriodNumber()){
			Subscription subscription = new Subscription();
			if(saleOrderLine.getInvoicingTypeSelect() == 1){
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			}
			else{
				subscription.setInvoicingDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)).minusDays(1));
			}
			subscription.setFromPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*iterator));
			subscription.setToPeriodDate(saleOrderLine.getFromSubDate().plusMonths(saleOrderLine.getPeriodicity()*(iterator+1)).minusDays(1));
			subscription.setQuantity(saleOrderLine.getQty());
			if(saleOrderLine.getPeriodicity() == saleOrderLine.getProduct().getPeriodicitySelect()){
				subscription.setUnitPrice(saleOrderLine.getPrice().multiply(new BigDecimal(saleOrderLine.getPeriodicity())));
			}
			else{
				subscription.setUnitPrice(saleOrderLine.getPrice().multiply(new BigDecimal(saleOrderLine.getPeriodicity()).divide(new BigDecimal(saleOrderLine.getProduct().getPeriodicitySelect()))));
			}
			subscription.setInvoiced(false);
			saleOrderLineIt.addSubscriptionListItem(subscription);
			iterator++;
		}

		Beans.get(SaleOrderLineRepository.class).save(saleOrderLineIt);

		return saleOrderLine;
	}
}
