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
package com.axelor.csv.script;

import java.util.Map;

import javax.persistence.Query;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.accountorganisation.service.TaskSaleOrderService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderServiceStockImpl;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
//import com.axelor.apps.production.service.ProductionOrderSaleOrderService;

public class ValidateSupplyChain {
	
	@Inject
	InventoryService inventoryService;
	
	@Inject
	PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;
	
	@Inject
	Provider<PurchaseOrderInvoiceService> purchaseOrderInvoiceServiceProvider;
	
	@Inject
	InvoiceService invoiceService;
	
	@Inject
	Provider<SaleOrderService> saleOrderServiceProvider;
	
	@Inject
	SaleOrderServiceStockImpl saleOrderServiceStockImpl;
	
	@Inject
	Provider<SaleOrderInvoiceService> saleOrderInvoiceServiceProvider;
	
	@Inject
	Provider<StockMoveService> stockMoveServiceProvider;
	
	@Inject
	Provider<StockMoveInvoiceService> stockMoveInvoiceServiceProvider;
	
	@Inject
	SaleOrderLineService saleOrderLineService;
	
	@Inject
	TaskSaleOrderService taskSaleOrderService;
	
	@Inject
	Provider<SaleOrderPurchaseService> saleOrderPurchaseServiceProvider;
	
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
		"UNION ALL(SELECT 'so' as type,id,validation_date as date from sale_sale_order) " +
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
		try{
			PurchaseOrder purchaseOrder = purchaseOrderRepo.find(poId);
			purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);
			if(purchaseOrder.getStatusSelect() == 4 || purchaseOrder.getStatusSelect() == 5 && purchaseOrder.getLocation() == null){
				purchaseOrderServiceSupplychainImpl.createStocksMoves(purchaseOrder);
				StockMove stockMove = stockMoveRepo.all().filter("purchaseOrder.id = ?1",purchaseOrder.getId()).fetchOne();
				if(stockMove != null){
					stockMoveServiceProvider.get().copyQtyToRealQty(stockMove);
					stockMoveServiceProvider.get().realize(stockMove);
					stockMove.setRealDate(purchaseOrder.getDeliveryDate());
				}
				purchaseOrder.setValidationDate(purchaseOrder.getOrderDate());
				purchaseOrder.setValidatedByUser(AuthUtils.getUser());
				purchaseOrder.setSupplierPartner(purchaseOrderServiceSupplychainImpl.validateSupplier(purchaseOrder));
				Invoice invoice = purchaseOrderInvoiceServiceProvider.get().generateInvoice(purchaseOrder);
				invoice.setInvoiceDate(purchaseOrder.getValidationDate());
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
		try{
			SaleOrder saleOrder = saleOrderRepo.find(soId);
			for(SaleOrderLine line : saleOrder.getSaleOrderLineList())
				line.setTaxLine(saleOrderLineService.getTaxLine(saleOrder, line));
			saleOrderServiceProvider.get().computeSaleOrder(saleOrder);
			if(saleOrder.getStatusSelect() == 3){
				taskSaleOrderService.createTasks(saleOrder);
				saleOrderServiceStockImpl.createStocksMovesFromSaleOrder(saleOrder);
				saleOrderPurchaseServiceProvider.get().createPurchaseOrders(saleOrder);
//				productionOrderSaleOrderService.generateProductionOrder(saleOrder);
				saleOrder.setClientPartner(saleOrderServiceProvider.get().validateCustomer(saleOrder));
				if(saleOrder.getInvoicingTypeSelect() == 1 || saleOrder.getInvoicingTypeSelect() == 5){
					Invoice invoice = saleOrderInvoiceServiceProvider.get().generatePerOrderInvoice(saleOrder);
					invoice.setInvoiceDate(saleOrder.getValidationDate());
					invoiceService.compute(invoice);
					invoiceService.validate(invoice);
					invoiceService.ventilate(invoice);
				}
				StockMove stockMove = stockMoveRepo.all().filter("saleOrder = ?1",saleOrder).fetchOne();
				if(stockMove != null && stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
					stockMoveServiceProvider.get().copyQtyToRealQty(stockMove);
					stockMoveServiceProvider.get().validate(stockMove);
					stockMove.setRealDate(saleOrder.getValidationDate());
					if(saleOrder.getInvoicingTypeSelect() == 4){
						Invoice invoice = stockMoveInvoiceServiceProvider.get().createInvoiceFromSaleOrder(stockMove, saleOrder);
						invoice.setInvoiceDate(saleOrder.getValidationDate());
						invoiceService.compute(invoice);
						invoiceService.validate(invoice);
						invoiceService.ventilate(invoice);
					}
				}
			}
			saleOrderRepo.save(saleOrder);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
