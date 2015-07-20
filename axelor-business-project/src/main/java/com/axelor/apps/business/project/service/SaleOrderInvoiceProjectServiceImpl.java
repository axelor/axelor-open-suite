package com.axelor.apps.business.project.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.db.repo.SubscriptionRepository;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderInvoiceProjectServiceImpl extends SaleOrderInvoiceServiceImpl{

	@Inject
	public SaleOrderInvoiceProjectServiceImpl(GeneralService generalService) {
		super(generalService);
	}

	@Inject
	private SaleOrderLineRepository saleOrderLineRepo;

	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {

			//Lines of subscription type are invoiced directly from sale order line or from the subscription batch
			if (!ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(saleOrderLine.getProduct().getProductTypeSelect())){
				invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderLine));
				invoiceLineList.get(invoiceLineList.size()-1).setProject(saleOrderLine.getProject());
				saleOrderLine.setInvoiced(true);
			}
		}

		return invoiceLineList;

	}

	@Override
	public List<InvoiceLine> createInvoiceLinesSubscription(Invoice invoice, List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderLine));
			invoiceLineList.get(invoiceLineList.size()-1).setProject(saleOrderLine.getProject());
			saleOrderLine.setInvoiced(true);
		}

		return invoiceLineList;

	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubcriptionsForSaleOrder(SaleOrder saleOrder) throws AxelorException{
		List<Subscription> subscriptionList = Beans.get(SubscriptionRepository.class).all().filter("self.invoicingDate <= ?1 AND self.saleOrderLine.saleOrder.id = ?2 AND self.invoiced = false",generalService.getTodayDate(),saleOrder.getId()).fetch();
		if(subscriptionList != null && !subscriptionList.isEmpty()){
			List<SaleOrderLine> saleOrderLineList = new ArrayList<SaleOrderLine>();
			for (Subscription subscription : subscriptionList) {
				SaleOrderLine saleOrderLine = saleOrderLineRepo.copy(subscription.getSaleOrderLine(), false);
				if(saleOrderLine.getDescription() != null){
					saleOrderLine.setDescription(subscription.getFromPeriodDate().toString()+I18n.get(" to ") + subscription.getToPeriodDate().toString() + saleOrderLine.getDescription());
				}
				else{
					saleOrderLine.setDescription(subscription.getFromPeriodDate().toString()+I18n.get(" to ") + subscription.getToPeriodDate().toString());
				}
				saleOrderLineList.add(saleOrderLine);

				subscription.setInvoiced(true);

				Beans.get(SubscriptionRepository.class).save(subscription);
			}
			Invoice invoice =  this.generateSubscriptionInvoice(subscriptionList.get(0), saleOrderLineList,saleOrder);
			invoice.setProject(saleOrder.getProject());
			Beans.get(InvoiceRepository.class).save(invoice);
			return invoice;
		}
		return null;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubcriptionsForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException{
		List<Subscription> subscriptionList = Beans.get(SubscriptionRepository.class).all().filter("self.invoicingDate <= ?1 AND self.saleOrderLine.id = ?2 AND self.invoiced = false",generalService.getTodayDate(),saleOrderLine.getId()).fetch();
		if(subscriptionList != null && !subscriptionList.isEmpty()){
			List<SaleOrderLine> saleOrderLineList = new ArrayList<SaleOrderLine>();
			for (Subscription subscription : subscriptionList) {
				saleOrderLineRepo.copy(saleOrderLine, false);
				if(saleOrderLine.getDescription() != null){
					saleOrderLine.setDescription(subscription.getFromPeriodDate().toString()+I18n.get(" to ") + subscription.getToPeriodDate().toString() + saleOrderLine.getDescription());
				}
				else{
					saleOrderLine.setDescription(subscription.getFromPeriodDate().toString()+I18n.get(" to ") + subscription.getToPeriodDate().toString());
				}

				saleOrderLineList.add(saleOrderLine);

				subscription.setInvoiced(true);

				Beans.get(SubscriptionRepository.class).save(subscription);
			}
			Invoice invoice = this.generateSubscriptionInvoice(subscriptionList.get(0), saleOrderLineList,saleOrderLine.getSaleOrder());
			invoice.setProject(saleOrderLine.getProject());
			Beans.get(InvoiceRepository.class).save(invoice);
			return invoice;
		}
		return null;
	}
}
