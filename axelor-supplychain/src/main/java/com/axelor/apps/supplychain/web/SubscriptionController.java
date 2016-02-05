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
package com.axelor.apps.supplychain.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SubscriptionService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SubscriptionController {

	@Inject
	protected SubscriptionService subscriptionService;

	@Inject
	protected SaleOrderLineRepository saleOrderLineRepo;

	@Inject
	protected GeneralService generalService;

	public void generateSubscriptions(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

		if(saleOrderLine.getId()!=null && saleOrderLine.getId()>0){
			saleOrderLine=Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
		}

		saleOrderLine = subscriptionService.generateSubscriptions(saleOrderLine);
		response.setValue("subscriptionList", saleOrderLine.getSubscriptionList());
	}
	
	@Transactional
	public void generateAllSubscriptions(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		saleOrder  = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
		
		subscriptionService.generateAllSubscriptions(saleOrder);
		
		response.setReload(true);
	}

	public void generateInvoice(ActionRequest request, ActionResponse response)  throws AxelorException{

		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

		saleOrderLine = saleOrderLineRepo.find(saleOrderLine.getId());

		if (saleOrderLine != null){

			Invoice invoice = Beans.get(SaleOrderInvoiceService.class).generateSubcriptionInvoiceForSaleOrderLine(saleOrderLine);

			if(invoice == null){
				throw new AxelorException(I18n.get("No Subscription to Invoice"), IException.CONFIGURATION_ERROR);
			}
			response.setCanClose(true);
			response.setView(ActionView
		            .define(I18n.get("Invoice Generated"))
		            .model(Invoice.class.getName())
		            .add("form", "invoice-form")
		            .add("grid", "invoice-grid")
		            .context("_showRecord",String.valueOf(invoice.getId()))
		            .map());
		}
	}

	public void generateInvoiceForAllSubscriptions(ActionRequest request, ActionResponse response)  throws AxelorException{

		Query q = JPA.em().createQuery("SELECT DISTINCT saleOrderLine.saleOrder.id FROM Subscription WHERE invoicingDate <= ?1 AND invoiced = false ", Long.class);
		q.setParameter(1, generalService.getTodayDate());
		List<Long> saleOrderIdList = q.getResultList();
		if(saleOrderIdList != null){
			SaleOrder saleOrder = null;
			List<Long> invoiceIdList = new ArrayList<Long>();
			for(Long saleOrderId : saleOrderIdList){
				saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
				try {
					Invoice invoice = Beans.get(SaleOrderInvoiceService.class).generateSubcriptionInvoiceForSaleOrder(saleOrder);
					invoiceIdList.add(invoice.getId());
				} catch (AxelorException e) {
					TraceBackService.trace(e);
				} finally{
					if (invoiceIdList.size()%10 == 0){
						JPA.clear();
					}
				}
			}

			if (!invoiceIdList.isEmpty()){

				response.setReload(true);

				response.setView(ActionView
			            .define(I18n.get("Invoices Generated"))
			            .model(Invoice.class.getName())
			            .add("grid", "invoice-grid")
			            .add("form", "invoice-form")
			            .domain("self.id IN (" + Joiner.on(",").join(invoiceIdList) + ")")
			            .map());
			}
		}
	}

	public void generateInvoiceForSelectedSubscriptions(ActionRequest request, ActionResponse response)  throws AxelorException{

		List<Integer> listSelectedSubscriptionsTemp = (List<Integer>) request.getContext().get("_ids");
		List<Long> listSelectedSubscriptions = new ArrayList<Long>();
		for (Integer intId : listSelectedSubscriptionsTemp) {
			listSelectedSubscriptions.add(intId.longValue());
		}


		Query q = JPA.em().createQuery("SELECT DISTINCT new List(saleOrderLine.saleOrder.id as saleOrderId, id as subscriptionId) FROM Subscription WHERE id IN (:subscriptionIds)", List.class);
		q.setParameter("subscriptionIds", listSelectedSubscriptions);
		List<List<Long>> soIdsubscriptionIdList = q.getResultList();

		if(listSelectedSubscriptions != null){
			Map<Long, List<Long>> subscriptionsBySaleOrderMap = new HashMap<Long, List<Long>>();
			for(List<Long> soIdsubscriptionId : soIdsubscriptionIdList){
				if (subscriptionsBySaleOrderMap.containsKey(soIdsubscriptionId.get(0))){
					subscriptionsBySaleOrderMap.get(soIdsubscriptionId.get(0)).add(soIdsubscriptionId.get(1));
				}else{
					List<Long> subscriptionIdList = new ArrayList<Long>();
					subscriptionIdList.add(soIdsubscriptionId.get(1));
					subscriptionsBySaleOrderMap.put(soIdsubscriptionId.get(0), subscriptionIdList);
				}
			}

			List<Long> invoiceIdList = new ArrayList<Long>();
			for(Map.Entry<Long, List<Long>> entry : subscriptionsBySaleOrderMap.entrySet()){
				try {
					Invoice invoice = Beans.get(SaleOrderInvoiceService.class).generateSubcriptionInvoiceForSaleOrderAndListSubscrip(entry.getKey(), entry.getValue());
					invoiceIdList.add(invoice.getId());
				} catch (AxelorException e) {
					TraceBackService.trace(e);
				} finally{
					if (invoiceIdList.size()%10 == 0){
						JPA.clear();
					}
				}
			}

			if (!invoiceIdList.isEmpty()){

				response.setView(ActionView
			            .define(I18n.get("Invoices Generated"))
			            .model(Invoice.class.getName())
			            .add("grid", "invoice-grid")
			            .add("form", "invoice-form")
			            .domain("self.id IN (" + Joiner.on(",").join(invoiceIdList) + ")")
			            .map());
			}
		}
	}
}
