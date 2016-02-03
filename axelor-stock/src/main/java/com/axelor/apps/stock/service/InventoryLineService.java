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
package com.axelor.apps.stock.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;

public class InventoryLineService {

	public InventoryLine createInventoryLine(Inventory inventory, Product product, BigDecimal currentQty, TrackingNumber trackingNumber)  {
		
		InventoryLine inventoryLine = new InventoryLine();
		inventoryLine.setInventory(inventory);
		inventoryLine.setProduct(product);
		inventoryLine.setCurrentQty(currentQty);
		inventoryLine.setTrackingNumber(trackingNumber);
		
		return inventoryLine;
		
	}

	
}
