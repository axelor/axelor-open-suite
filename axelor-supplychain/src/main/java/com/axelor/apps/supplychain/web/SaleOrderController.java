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
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.db.repo.SubscriptionRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.TimetableService;
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

public class SaleOrderController{
	
	@Inject
	private SaleOrderRepository saleOrderRepo;

	@Inject
	protected GeneralService generalService;

	@Inject
	private SaleOrderInvoiceServiceImpl saleOrderInvoiceServiceImpl;
	

	public void createStockMove(ActionRequest request, ActionResponse response) throws AxelorException {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		if(saleOrder.getId() != null) {

			SaleOrderStockService saleOrderStockService = Beans.get(SaleOrderStockService.class);
			StockMove stockMove = saleOrderStockService.createStocksMovesFromSaleOrder(saleOrderRepo.find(saleOrder.getId()));
			
			if(stockMove != null)  {
				response.setView(ActionView
					.define(I18n.get("Stock Move"))
					.model(StockMove.class.getName())
					.add("grid", "stock-move-grid")
					.add("form", "stock-move-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(stockMove.getId())).map());
			}
			else  {
				response.setFlash(I18n.get(IExceptionMessage.SO_NO_DELIVERY_STOCK_MOVE_TO_GENERATE));
			}
		}
	}

	public void getLocation(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		if(saleOrder != null) {

			Location location = Beans.get(SaleOrderStockService.class).getLocation(saleOrder.getCompany());

			if(location != null) {
				response.setValue("location", location);
			}
		}
	}


	public void generatePurchaseOrdersFromSelectedSOLines(ActionRequest request, ActionResponse response) throws AxelorException {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		if(saleOrder.getId() != null) {

			if (request.getContext().get("supplierPartnerSelect") != null){
				Partner partner = JPA.em().find(Partner.class, new Long((Integer)((Map)request.getContext().get("supplierPartnerSelect")).get("id")));
				List<Long> saleOrderLineIdSelected = new ArrayList<Long>();
				String saleOrderLineIdSelectedStr = (String)request.getContext().get("saleOrderLineIdSelected");
				for (String saleOrderId : saleOrderLineIdSelectedStr.split(",")) {
					saleOrderLineIdSelected.add(new Long(saleOrderId));
				}
				List<SaleOrderLine> saleOrderLinesSelected = JPA.all(SaleOrderLine.class).filter("self.id IN (:saleOderLineIdList)").bind("saleOderLineIdList", saleOrderLineIdSelected).fetch();
				PurchaseOrder purchaseOrder = Beans.get(SaleOrderPurchaseService.class).createPurchaseOrder(partner, saleOrderLinesSelected, saleOrderRepo.find(saleOrder.getId()));
				response.setView(ActionView
						.define(I18n.get("Purchase Order"))
						.model(PurchaseOrder.class.getName())
						.add("form", "purchase-order-form")
						.param("forceEdit", "true")
						.context("_showRecord", String.valueOf(purchaseOrder.getId()))
						.map());
				response.setCanClose(true);
			}else{
				Partner supplierPartner = null;
				List<Long> saleOrderLineIdSelected = new ArrayList<Long>();

				//Check if supplier partners of each sale order line are the same. If it is, send the partner id to view to load this partner by default into select
				for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
					if (saleOrderLine.isSelected()){
						if (supplierPartner == null){
							supplierPartner = saleOrderLine.getSupplierPartner();
						}else{
							if (!supplierPartner.equals(saleOrderLine.getSupplierPartner())){
								supplierPartner = null;
								break;
							}
						}
						saleOrderLineIdSelected.add(saleOrderLine.getId());
					}
				}

				if (saleOrderLineIdSelected.isEmpty()){
					response.setFlash(I18n.get(IExceptionMessage.SO_LINE_PURCHASE_AT_LEAST_ONE));
				}else{
					response.setView(ActionView
									.define("SaleOrder")
									.model(SaleOrder.class.getName())
									.add("form", "sale-order-generate-po-select-supplierpartner-form")
									.param("popup", "true")
									.param("show-toolbar", "false")
									.param("show-confirm", "false")
									.param("popup-save", "false")
									.param("forceEdit", "true")
									.context("_showRecord", String.valueOf(saleOrder.getId()))
									.context("supplierPartnerId", ((supplierPartner != null) ? String.valueOf(supplierPartner.getId()) : "NULL"))
									.context("saleOrderLineIdSelected", Joiner.on(",").join(saleOrderLineIdSelected))
									.map());
				}
			}

		}
	}


	public void generateInvoice(ActionRequest request, ActionResponse response)  {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		try {

			saleOrder = saleOrderRepo.find(saleOrder.getId());
			//Check if at least one row is selected. If yes, then invoiced only the selected rows, else invoiced all rows
			List<Long> saleOrderLineIdSelected = new ArrayList<Long>();
			for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
				if (saleOrderLine.isSelected()){
					saleOrderLineIdSelected.add(saleOrderLine.getId());
				}
			}

			Invoice invoice = null;

			if (!saleOrderLineIdSelected.isEmpty()){
				List<SaleOrderLine> saleOrderLinesSelected = JPA.all(SaleOrderLine.class).filter("self.id IN (:saleOderLineIdList)").bind("saleOderLineIdList", saleOrderLineIdSelected).fetch();
				invoice = saleOrderInvoiceServiceImpl.generateInvoice(saleOrder, saleOrderLinesSelected);
			}else{
				invoice = saleOrderInvoiceServiceImpl.generateInvoice(saleOrder);
			}

			if(invoice != null)  {
				response.setReload(true);
				response.setFlash(I18n.get(IExceptionMessage.PO_INVOICE_2));
				response.setView(ActionView
		            .define(I18n.get("Invoice generated"))
		            .model(Invoice.class.getName())
		            .add("form", "invoice-form")
		            .add("grid", "invoice-grid")
		            .context("_showRecord",String.valueOf(invoice.getId()))
		            .map());
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}

	public void getSubscriptionSaleOrdersToInvoice(ActionRequest request, ActionResponse response) throws AxelorException  {

		List<Subscription> subscriptionList = Beans.get(SubscriptionRepository.class).all().filter("self.invoiced = false AND self.invoicingDate <= ?1",generalService.getTodayDate()).fetch();
		List<Long> listId = new ArrayList<Long>();
		for (Subscription subscription : subscriptionList) {
			listId.add(subscription.getSaleOrderLine().getSaleOrder().getId());
		}
		if(listId.isEmpty()){
			throw new AxelorException(I18n.get("No Subscription to Invoice"), IException.CONFIGURATION_ERROR);
		}
		if(listId.size() == 1){
			response.setView(ActionView
		            .define(I18n.get("Subscription Sale Orders"))
		            .model(SaleOrder.class.getName())
		            .add("grid", "sale-order-subscription-grid")
		            .add("form", "sale-order-form")
		            .domain("self.id = "+listId.get(0))
		            .map());
		}
		response.setView(ActionView
	            .define(I18n.get("Subscription Sale Orders"))
	            .model(SaleOrder.class.getName())
	            .add("grid", "sale-order-subscription-grid")
	            .add("form", "sale-order-form")
	            .domain("self.id in ("+Joiner.on(",").join(listId)+")")
	            .map());
	}

	public void invoiceSubscriptions(ActionRequest request, ActionResponse response) throws AxelorException{
		List<Integer> listSelectedSaleOrder = (List<Integer>) request.getContext().get("_ids");
		if(listSelectedSaleOrder != null){
			SaleOrder saleOrder = null;
			List<Long> listInvoiceId = new ArrayList<Long>();
			for (Integer integer : listSelectedSaleOrder) {
				saleOrder = saleOrderRepo.find(integer.longValue());
				Invoice invoice = saleOrderInvoiceServiceImpl.generateSubcriptionInvoiceForSaleOrder(saleOrder);
				if(invoice != null){
					listInvoiceId.add(invoice.getId());
				}
			}
			if(listInvoiceId.isEmpty()){
				throw new AxelorException(I18n.get("No sale order selected or no subscription to invoice"), IException.CONFIGURATION_ERROR);
			}
			response.setReload(true);
			if(listInvoiceId.size() == 1){
				response.setView(ActionView
			            .define(I18n.get("Invoice Generated"))
			            .model(Invoice.class.getName())
			            .add("grid", "invoice-grid")
			            .add("form", "invoice-form")
			            .domain("self.id = "+listInvoiceId.get(0))
			            .map());
			}
			response.setView(ActionView
		            .define(I18n.get("Invoices Generated"))
		            .model(Invoice.class.getName())
		            .add("grid", "invoice-grid")
		            .add("form", "invoice-form")
		            .domain("self.id in ("+Joiner.on(",").join(listInvoiceId)+")")
		            .map());
		}
	}

	public void invoiceSubscriptionsSaleOrder(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		saleOrder = saleOrderRepo.find(saleOrder.getId());
		Invoice invoice = saleOrderInvoiceServiceImpl.generateSubcriptionInvoiceForSaleOrder(saleOrder);
		if(invoice == null){
			throw new AxelorException(I18n.get("No Subscription to Invoice"), IException.CONFIGURATION_ERROR);
		}
		response.setReload(true);
		response.setView(ActionView
	            .define(I18n.get("Invoice Generated"))
	            .model(Invoice.class.getName())
	            .add("form", "invoice-form")
	            .add("grid", "invoice-grid")
	            .context("_showRecord",String.valueOf(invoice.getId()))
	            .map());
	}
	
	public void updateTimetable(ActionRequest request, ActionResponse response){
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		if(saleOrder.getId() != null && saleOrder.getId() > 0){
			saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
		}
		Beans.get(TimetableService.class).updateTimetable(saleOrder);
		response.setValues(saleOrder);
	}
	
	
}
