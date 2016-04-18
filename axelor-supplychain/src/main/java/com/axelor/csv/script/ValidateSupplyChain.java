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
package com.axelor.csv.script;

import java.util.Map;

import javax.persistence.Query;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ValidateSupplyChain {
	@Inject
	InventoryService inventoryService;

	@Inject
	PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;

	@Inject
	InvoiceService invoiceService;

	@Inject
	SaleOrderStockService saleOrderStockService;

	@Inject
	SaleOrderLineService saleOrderLineService;

	@Inject
	private StockMoveRepository stockMoveRepo;

	@Inject
	private InventoryRepository inventoryRepo;

	@Inject
	private PurchaseOrderRepository purchaseOrderRepo;

	@Inject
	private SaleOrderRepository saleOrderRepo;

//	@Inject
//	ProductionOrderSaleOrderService productionOrderSaleOrderService;

	public Object validateSupplyChain(Object bean, Map values) {
		String objectQuery = "(SELECT 'inv' as type,id,datet as date from stock_inventory) " +
		"UNION ALL(SELECT 'so' as type,id,confirmation_date as date from sale_sale_order) " +
		"UNION ALL(SELECT 'po' as type,id,order_date as date from purchase_purchase_order) order by date";

		Query query = JPA.em().createNativeQuery(objectQuery);
		for(Object objects : query.getResultList()){
			Object[] object = (Object[]) objects;
			if(object[0].toString().equals("inv"))
				validateInventory(Long.parseLong(object[1].toString()));
			else if(object[0].toString().equals("po"))
				validatePurchaseOrder(Long.parseLong(object[1].toString()));
			else
				validateSaleOrder(Long.parseLong(object[1].toString()));
		}
		return bean;
	}

	@Transactional
	void validateInventory(Long inventoryId){
		try{
			Inventory inventory = inventoryRepo.find(inventoryId);
			StockMove stockMove = inventoryService.generateStockMove(inventory);
			stockMove.setRealDate(inventory.getDateT().toLocalDate());
			stockMoveRepo.save(stockMove);
			inventory.setStatusSelect(3);
			inventoryRepo.save(inventory);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Transactional
	void validatePurchaseOrder(Long poId){
		StockMoveService stockMoveService = Beans.get(StockMoveService.class);
		try{
			PurchaseOrder purchaseOrder = purchaseOrderRepo.find(poId);
			purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);
			if(purchaseOrder.getStatusSelect() == 4 || purchaseOrder.getStatusSelect() == 5 && purchaseOrder.getLocation() == null){
				purchaseOrderServiceSupplychainImpl.createStocksMove(purchaseOrder);
				StockMove stockMove = stockMoveRepo.all().filter("purchaseOrder.id = ?1",purchaseOrder.getId()).fetchOne();
				if(stockMove != null){
					stockMoveService.copyQtyToRealQty(stockMove);
					stockMoveService.realize(stockMove);
					stockMove.setRealDate(purchaseOrder.getDeliveryDate());
				}
				purchaseOrder.setValidationDate(purchaseOrder.getOrderDate());
				purchaseOrder.setValidatedByUser(AuthUtils.getUser());
				purchaseOrder.setSupplierPartner(purchaseOrderServiceSupplychainImpl.validateSupplier(purchaseOrder));
				Invoice invoice = Beans.get(PurchaseOrderInvoiceService.class).generateInvoice(purchaseOrder);
				if(purchaseOrder.getValidationDate()!=null){
					invoice.setInvoiceDate(purchaseOrder.getValidationDate());
				}
				else{
					invoice.setInvoiceDate(new LocalDate());
				}
				invoiceService.compute(invoice);
				invoiceService.validate(invoice);
				invoiceService.ventilate(invoice);
			}
			purchaseOrderRepo.save(purchaseOrder);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Transactional
	void validateSaleOrder(Long soId){
		SaleOrderService saleOrderService = Beans.get(SaleOrderService.class);
		StockMoveService stockMoveService = Beans.get(StockMoveService.class);

		try{
			SaleOrder saleOrder = saleOrderRepo.find(soId);
			for(SaleOrderLine line : saleOrder.getSaleOrderLineList())
				line.setTaxLine(saleOrderLineService.getTaxLine(saleOrder, line));
			saleOrderService.computeSaleOrder(saleOrder);
			if(saleOrder.getStatusSelect() == ISaleOrder.STATUS_ORDER_CONFIRMED){
				//taskSaleOrderService.createTasks(saleOrder); TODO once we will have done the generation of tasks in project module
				saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
				Beans.get(SaleOrderPurchaseService.class).createPurchaseOrders(saleOrder);
//				productionOrderSaleOrderService.generateProductionOrder(saleOrder);
				saleOrder.setClientPartner(saleOrderService.validateCustomer(saleOrder));
				//Generate invoice from sale order
				Invoice invoice = Beans.get(SaleOrderInvoiceService.class).generateInvoice(saleOrder);
				if(saleOrder.getConfirmationDate()!=null){
					invoice.setInvoiceDate(saleOrder.getConfirmationDate());
				}
				else{
					invoice.setInvoiceDate(new LocalDate());
				}
				invoiceService.compute(invoice);
				invoiceService.validate(invoice);
				invoiceService.ventilate(invoice);
				StockMove stockMove = stockMoveRepo.all().filter("saleOrder = ?1",saleOrder).fetchOne();
				if(stockMove != null && stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
					stockMoveService.copyQtyToRealQty(stockMove);
					stockMoveService.validate(stockMove);
					stockMove.setRealDate(saleOrder.getConfirmationDate());
				}
			}
			saleOrderRepo.save(saleOrder);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
