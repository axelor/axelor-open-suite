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
package com.axelor.apps.supplychain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class StockMoveServiceSupplychainImpl extends StockMoveServiceImpl  {

	private static final Logger LOG = LoggerFactory.getLogger(StockMoveServiceSupplychainImpl.class);

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	@Override
	public String realize(StockMove stockMove) throws AxelorException  {
		LOG.debug("Réalisation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		String newStockSeq = super.realize(stockMove);

		if (stockMove.getSaleOrder() != null){
			//Update linked saleOrder delivery state depending on BackOrder's existence
			SaleOrder saleOrder = stockMove.getSaleOrder();
			if (newStockSeq != null){
				saleOrder.setDeliveryState(SaleOrderRepository.STATE_PARTIALLY_DELIVERED);
			}else{
				saleOrder.setDeliveryState(SaleOrderRepository.STATE_DELIVERED);
			}

			JPA.save(saleOrder);
		}

		return newStockSeq;
	}


}
