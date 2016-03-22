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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveServiceSupplychainImpl extends StockMoveServiceImpl  {

	private static final Logger LOG = LoggerFactory.getLogger(StockMoveServiceSupplychainImpl.class);
	
	@Inject
	GeneralService generalService;
	
	@Override
	public BigDecimal compute(StockMove stockMove){
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
			if((stockMove.getSaleOrder() != null && stockMove.getSaleOrder().getInAti()) || (stockMove.getPurchaseOrder() != null && stockMove.getPurchaseOrder().getInAti())){
				for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
					exTaxTotal = exTaxTotal.add(stockMoveLine.getRealQty().multiply(stockMoveLine.getUnitPriceTaxed()));
				}
			}
			else{
				for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
					exTaxTotal = exTaxTotal.add(stockMoveLine.getRealQty().multiply(stockMoveLine.getUnitPriceUntaxed()));
				}
			}
		}
		return exTaxTotal.setScale(2, RoundingMode.HALF_UP);
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	@Override
	public String realize(StockMove stockMove) throws AxelorException  {
		LOG.debug("Réalisation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		String newStockSeq = super.realize(stockMove);
		General general = generalService.getGeneral();
		if (stockMove.getSaleOrder() != null){
			//Update linked saleOrder delivery state depending on BackOrder's existence
			SaleOrder saleOrder = stockMove.getSaleOrder();
			if (newStockSeq != null){
				saleOrder.setDeliveryState(SaleOrderRepository.STATE_PARTIALLY_DELIVERED);
			}else{
				saleOrder.setDeliveryState(SaleOrderRepository.STATE_DELIVERED);
				if (general.getTerminateSaleOrderOnDelivery()){
					Beans.get(SaleOrderServiceImpl.class).finishSaleOrder(saleOrder);
				}
			}

			Beans.get(SaleOrderRepository.class).save(saleOrder);
		}else if (stockMove.getPurchaseOrder() != null){
			//Update linked purchaseOrder receipt state depending on BackOrder's existence
			PurchaseOrder purchaseOrder = stockMove.getPurchaseOrder();
			if (newStockSeq != null){
				purchaseOrder.setReceiptState(IPurchaseOrder.STATE_PARTIALLY_RECEIVED);
			}else{
				purchaseOrder.setReceiptState(IPurchaseOrder.STATE_RECEIVED);
				if (general.getTerminatePurchaseOrderOnReceipt()){
					Beans.get(PurchaseOrderServiceImpl.class).finishPurchaseOrder(purchaseOrder);
				}
			}

			Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);
		}

		return newStockSeq;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeProductWeightedAveragePrice(StockMove stockMove){
		
	}

}
