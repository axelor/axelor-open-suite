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
import com.google.inject.persist.Transactional;

public class LocationService {
	
	@Transactional
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
