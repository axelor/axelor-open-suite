/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.InventoryLine;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class LocationService {
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Inventory createInventory(String ref, LocalDate date, String description, Location location, 
			boolean excludeOutOfStock, boolean includeObsolete, ProductFamily productFamily,
			ProductCategory productCategory) throws Exception {
			
		
		Inventory inventory = new Inventory();
		
		inventory.setStatusSelect(1);
		
		if (ref != null)
			inventory.setInventorySeq(ref);
			
		if (date != null)
			inventory.setDateT(date.toDateTimeAtStartOfDay());
		else
			throw new Exception("Invalid date");
		
		
		if (description != null)
			inventory.setDescription(description);
		
		
		if (location != null)
			inventory.setLocation(location);
		else
			throw new Exception("Invalid location");
		
		
		String query = "self.detailLocation = ?";
		List<Object> params = new ArrayList<Object>();
		params.add(location);
		
		if (excludeOutOfStock) {
			query += " and self.currentQty > 0";
		}
		
		if (!includeObsolete) {
			query += " and (self.product.endDate > ? or self.product.endDate is null)";
			params.add(date);
		}
		
		if (productFamily != null) {
			query += " and self.product.productFamily = ?";
			params.add(productFamily);
		}
		
		if (productCategory != null) {
			query += " and self.product.productCategory = ?";
			params.add(productCategory);
		}
		
		List<LocationLine> locationLineList = LocationLine.all().filter(query, params.toArray()).fetch();
		if (locationLineList != null) {
			List<InventoryLine> inventoryLineList = new ArrayList<InventoryLine>();
			
			for (LocationLine locationLine : locationLineList) {
				InventoryLine inventoryLine = new InventoryLine();
				inventoryLine.setProduct(locationLine.getProduct());
				inventoryLine.setCurrentQty(locationLine.getCurrentQty());
				inventoryLine.setInventory(inventory);
				inventoryLine.setTrackingNumber(locationLine.getTrackingNumber());
				inventoryLine.setProductVariant(locationLine.getProductVariant());
				inventoryLineList.add(inventoryLine);
			}
			
			inventory.setInventoryLineList(inventoryLineList);
		}
		inventory.save();
		return inventory;
	}
}
