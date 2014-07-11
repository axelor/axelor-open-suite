/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.purchase.service.PurchaseOrderInvoiceService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.apps.stock.service.StockMoveInvoiceService;
import com.axelor.apps.stock.service.StockMoveService;
//import com.axelor.apps.production.service.ProductionOrderSaleOrderService;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.SaleOrderInvoiceService;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderPurchaseService;
import com.axelor.apps.sale.service.SaleOrderStockMoveService;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ValidateSupplyChain {
	
	@Inject
	InventoryService inventoryService;
	
	@Inject
	PurchaseOrderService purchaseOrderService;
	
	@Inject
	StockMoveService stockMoveSerivce;
	
	@Inject
	PurchaseOrderInvoiceService purchaseOrderInvoiceService;
	
	@Inject
	UserInfoService userInfoSerivce;
	
	@Inject
	InvoiceService invoiceService;
	
	@Inject
	SaleOrderService saleOrderService;
	
	@Inject
	SaleOrderStockMoveService saleOrderStockMoveService;
	
	@Inject
	SaleOrderInvoiceService saleOrderInvoiceService;
	
	@Inject
	StockMoveService stockMoveService;
	
	@Inject
	StockMoveInvoiceService stockMoveInvoiceService;
	
	@Inject
	SaleOrderLineService saleOrderLineService;
	
	@Inject
	TaskSaleOrderService taskSaleOrderService;
	
	@Inject
	SaleOrderPurchaseService saleOrderPurchaseService;
	
//	@Inject
//	ProductionOrderSaleOrderService productionOrderSaleOrderService;
	
	public Object validateSupplyChain(Object bean, Map values) {
		String objectQuery = "(SELECT 'inv' as type,id,datet as date from supplychain_inventory) " +
		"UNION ALL(SELECT 'so' as type,id,validation_date as date from supplychain_sale_order) " +
		"UNION ALL(SELECT 'po' as type,id,order_date as date from supplychain_purchase_order) order by date";

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
			Inventory inventory = Inventory.find(inventoryId);
			StockMove stockMove = inventoryService.generateStockMove(inventory);
			stockMove.setRealDate(inventory.getDateT().toLocalDate());
			stockMove.save();
			inventory.setStatusSelect(3);
			inventory.save();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Transactional
	void validatePurchaseOrder(Long poId){
		try{
			PurchaseOrder purchaseOrder = PurchaseOrder.find(poId);
			purchaseOrderService.computePurchaseOrder(purchaseOrder);
			if(purchaseOrder.getStatusSelect() == 4 || purchaseOrder.getStatusSelect() == 5 && purchaseOrder.getLocation() == null){
				purchaseOrderService.createStocksMoves(purchaseOrder);
				StockMove stockMove = StockMove.all_().filter("purchaseOrder.id = ?1",purchaseOrder.getId()).fetchOne();
				if(stockMove != null){
					stockMoveService.copyQtyToRealQty(stockMove);
					stockMoveService.realize(stockMove);
					stockMove.setRealDate(purchaseOrder.getDeliveryDate());
				}
				purchaseOrder.setValidationDate(purchaseOrder.getOrderDate());
				purchaseOrder.setValidatedByUserInfo(userInfoSerivce.getUserInfo());
				purchaseOrder.setSupplierPartner(purchaseOrderService.validateSupplier(purchaseOrder));
				Invoice invoice = purchaseOrderInvoiceService.generateInvoice(purchaseOrder);
				invoice.setInvoiceDate(purchaseOrder.getValidationDate());
				invoiceService.compute(invoice);
				invoiceService.validate(invoice);
				invoiceService.ventilate(invoice);
			}
			purchaseOrder.save();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Transactional
	void validateSaleOrder(Long soId){
		try{
			SaleOrder saleOrder = SaleOrder.find(soId);
			for(SaleOrderLine line : saleOrder.getSaleOrderLineList())
				line.setTaxLine(saleOrderLineService.getTaxLine(saleOrder, line));
			saleOrderService.computeSaleOrder(saleOrder);
			if(saleOrder.getStatusSelect() == 3){
				taskSaleOrderService.createTasks(saleOrder);
				saleOrderStockMoveService.createStocksMovesFromSaleOrder(saleOrder);
				saleOrderPurchaseService.createPurchaseOrders(saleOrder);
//				productionOrderSaleOrderService.generateProductionOrder(saleOrder);
				saleOrder.setClientPartner(saleOrderService.validateCustomer(saleOrder));
				if(saleOrder.getInvoicingTypeSelect() == 1 || saleOrder.getInvoicingTypeSelect() == 5){
					Invoice invoice = saleOrderInvoiceService.generatePerOrderInvoice(saleOrder);
					invoice.setInvoiceDate(saleOrder.getValidationDate());
					invoiceService.compute(invoice);
					invoiceService.validate(invoice);
					invoiceService.ventilate(invoice);
				}
				StockMove stockMove = StockMove.all_().filter("saleOrder = ?1",saleOrder).fetchOne();
				if(stockMove != null && stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
					stockMoveService.copyQtyToRealQty(stockMove);
					stockMoveService.validate(stockMove);
					stockMove.setRealDate(saleOrder.getValidationDate());
					if(saleOrder.getInvoicingTypeSelect() == 4){
						Invoice invoice = stockMoveInvoiceService.createInvoiceFromSaleOrder(stockMove, saleOrder);
						invoice.setInvoiceDate(saleOrder.getValidationDate());
						invoiceService.compute(invoice);
						invoiceService.validate(invoice);
						invoiceService.ventilate(invoice);
					}
				}
			}
			saleOrder.save();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
