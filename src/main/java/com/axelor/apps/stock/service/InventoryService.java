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
package com.axelor.apps.stock.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.IInventory;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.InventoryLine;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.apps.supplychain.service.config.SupplychainConfigService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InventoryService {
	
	private static final Logger LOG = LoggerFactory.getLogger(InventoryService.class); 

	@Inject
	private InventoryLineService inventoryLineService;
	
	@Inject
	private StockMoveService stockMoveService;

	@Inject
	private StockMoveLineService stockMoveLineService;

	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private SupplychainConfigService supplychainConfigService;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Inventory createInventoryFromWizard(LocalDate date, String description, Location location, boolean excludeOutOfStock, 
			boolean includeObsolete, ProductFamily productFamily, ProductCategory productCategory) throws Exception {
		
		Inventory inventory = this.createInventory(date, description, location, excludeOutOfStock, includeObsolete, productFamily, productCategory).save();
		
		this.fillInventoryLineList(inventory);
		
		return inventory;
	}
	
	
	
	public Inventory createInventory(LocalDate date, String description, Location location, boolean excludeOutOfStock, boolean includeObsolete, 
			ProductFamily productFamily, ProductCategory productCategory) throws AxelorException  {
		
		if(location == null)  {
			throw new AxelorException("Veuillez selectionner un entrepot",IException.CONFIGURATION_ERROR);
		}
		
		Inventory inventory = new Inventory();
		
		inventory.setInventorySeq(this.getInventorySequence(location.getCompany()));
			
		inventory.setDateT(date.toDateTimeAtStartOfDay());
		
		inventory.setDescription(description);
		
		inventory.setFormatSelect(IInventory.FORMAT_PDF);
		
		inventory.setLocation(location);
		
		inventory.setExcludeOutOfStock(excludeOutOfStock);
		
		inventory.setIncludeObsolete(includeObsolete);
		
		inventory.setProductCategory(productCategory);
		
		inventory.setProductFamily(productFamily);
		
		inventory.setStatusSelect(IInventory.STATUS_DRAFT);
		
		return inventory;
	}
	
	
	public String getInventorySequence(Company company) throws AxelorException   {
		
		String ref = sequenceService.getSequenceNumber(IAdministration.INVENTORY, company);
		if (ref == null)
			throw new AxelorException("Aucune séquence configurée pour les inventaires pour la société "+company.getName(),
							IException.CONFIGURATION_ERROR);
		
		return ref;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void importFile(String filePath, char separator, Inventory inventory) throws IOException, AxelorException {

		List<InventoryLine> inventoryLineList = inventory.getInventoryLineList();

		List<String[]> data = this.getDatas(filePath, separator);

		HashMap<String,InventoryLine> inventoryLineMap = this.getInventoryLines(inventory);

		for (String[] line : data) {
			if (line.length < 6)
				throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);

			String code = line[1].replace("\"", "");
			String trackingNumberSeq = line[3].replace("\"", "");

			Integer realQty = 0;
			try {
				realQty = Integer.valueOf(line[6].replace("\"", ""));
			}catch(NumberFormatException e) {
				throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);
			}

			String description = line[7].replace("\"", "");

			if (inventoryLineMap.containsKey(code)) {
				inventoryLineMap.get(code).setRealQty(new BigDecimal(realQty));
				inventoryLineMap.get(code).setDescription(description);
			}
			else {
				Integer currentQty = 0;
				try {
					currentQty = Integer.valueOf(line[4].replace("\"", ""));
				}catch(NumberFormatException e) {
					throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);
				}

				InventoryLine inventoryLine = new InventoryLine();
				Product product = Product.findByCode(code);
				if (product == null || product.getApplicationTypeSelect() != IProduct.APPLICATION_TYPE_PRODUCT || !product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE))
					throw new AxelorException("An error occurred while importing the file data, product not found with code : "+code, IException.CONFIGURATION_ERROR);
				inventoryLine.setProduct(product);
				inventoryLine.setInventory(inventory);
				inventoryLine.setCurrentQty(new BigDecimal(currentQty));
				inventoryLine.setRealQty(new BigDecimal(realQty));
				inventoryLine.setDescription(description);
				inventoryLine.setTrackingNumber(this.getTrackingNumber(trackingNumberSeq));
				inventoryLineList.add(inventoryLine);
			}
		}
		inventory.setInventoryLineList(inventoryLineList);

		inventory.save();
	}


	public List<String[]> getDatas(String filePath, char separator) throws AxelorException  {

		List<String[]> data = null;
		try {
			data = CsvTool.cSVFileReader(filePath, separator);
		} catch(Exception e) {
			throw new AxelorException("There is currently no such file in the specified folder or the folder may not exists.", IException.CONFIGURATION_ERROR);
		}

		if (data == null || data.isEmpty())  {
			throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);
		}

		return data;


	}


	public HashMap<String,InventoryLine> getInventoryLines(Inventory inventory)  {
		HashMap<String,InventoryLine> inventoryLineMap = new HashMap<String,InventoryLine>();

		for (InventoryLine line : inventory.getInventoryLineList()) {
			String key = "";
			if(line.getProduct() != null)  {
				key += line.getProduct().getCode();
			}	
			if(line.getTrackingNumber() != null)  {
				key += line.getTrackingNumber().getTrackingNumberSeq();
			}

			inventoryLineMap.put(key, line);
		}

		return inventoryLineMap;
	}


	public TrackingNumber getTrackingNumber(String sequence)  {

		if(sequence != null && !sequence.isEmpty())  {
			return TrackingNumber.findBySeq(sequence);
		}

		return null;

	}

	public StockMove generateStockMove(Inventory inventory) throws AxelorException {

		Location toLocation = inventory.getLocation();
		Company company = toLocation.getCompany();

		if (company == null) {
			throw new AxelorException(String.format("Société manquante pour l'entrepot {}", toLocation.getName()), IException.CONFIGURATION_ERROR);
		}

		String inventorySeq = inventory.getInventorySeq();

		StockMove stockMove = this.createStockMoveHeader(inventory, company, toLocation, inventory.getDateT().toLocalDate(), inventorySeq);

		for (InventoryLine inventoryLine : inventory.getInventoryLineList()) {
			BigDecimal currentQty = inventoryLine.getCurrentQty();
			BigDecimal realQty = inventoryLine.getRealQty();
			Product product = inventoryLine.getProduct();

			if (currentQty.compareTo(realQty) != 0) {
				BigDecimal diff = realQty.subtract(currentQty);

				StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(product, diff, product.getUnit(), null, stockMove, 0);
				if (stockMoveLine == null)  {
					throw new AxelorException("Produit incorrect dans la ligne de l'inventaire "+inventorySeq, IException.CONFIGURATION_ERROR);
				}

				stockMove.addStockMoveLineListItem(stockMoveLine);
			}
		}
		if (stockMove.getStockMoveLineList() != null) {
			
			stockMoveService.plan(stockMove);
			stockMoveService.copyQtyToRealQty(stockMove);
			stockMoveService.realize(stockMove);
		}
		return stockMove;
	}

	
	public StockMove createStockMoveHeader(Inventory inventory, Company company, Location toLocation, LocalDate inventoryDate, String name) throws AxelorException  {

		StockMove stockMove = stockMoveService.createStockMove(null, null, company, null, 
				supplychainConfigService.getInventoryVirtualLocation(supplychainConfigService.getSupplychainConfig(company)), toLocation, inventoryDate, inventoryDate);
		
		stockMove.setTypeSelect(IStockMove.TYPE_INTERNAL);
		stockMove.setName(name);

		return stockMove;
	}

	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public List<InventoryLine> fillInventoryLineList(Inventory inventory) throws AxelorException {

		if(inventory.getLocation() == null)  {
			throw new AxelorException("Veuillez selectionner un entrepot",IException.CONFIGURATION_ERROR);
		}
		
		this.initInventoryLines(inventory);
		
		List<LocationLine> locationLineList = this.getLocationLines(inventory);
		
		if (locationLineList != null) {
			List<InventoryLine> inventoryLineList = new ArrayList<InventoryLine>();
			
			for (LocationLine locationLine : locationLineList) {
				
				inventoryLineList.add(this.createInventoryLine(inventory, locationLine));
				
			}
			inventory.setInventoryLineList(inventoryLineList);
			inventory.save();
			return inventoryLineList;
		}
		return null;
	}
	
	
	public List<LocationLine> getLocationLines(Inventory inventory)  {
		
		String query = "(self.location = ? OR self.detailsLocation = ?)";
		List<Object> params = new ArrayList<Object>();
		
		params.add(inventory.getLocation());
		params.add(inventory.getLocation());
		
		if (inventory.getExcludeOutOfStock()) {
			query += " and self.currentQty > 0";
		}
		
		if (!inventory.getIncludeObsolete()) {
			query += " and (self.product.endDate > ? or self.product.endDate is null)";
			params.add(inventory.getDateT().toLocalDate());
		}
		
		if (inventory.getProductFamily() != null) {
			query += " and self.product.productFamily = ?";
			params.add(inventory.getProductFamily());
		}
		
		if (inventory.getProductCategory() != null) {
			query += " and self.product.productCategory = ?";
			params.add(inventory.getProductCategory());
		}
		
		return LocationLine.filter(query, params.toArray()).fetch();
		
	}
	
	
	public InventoryLine createInventoryLine(Inventory inventory, LocationLine locationLine)  {
		
		return inventoryLineService.createInventoryLine(
				inventory, 
				locationLine.getProduct(), 
				locationLine.getCurrentQty(), 
				locationLine.getTrackingNumber());
		
	}
	
	
	public void initInventoryLines(Inventory inventory)  {
		
		if (inventory.getInventoryLineList() == null) { inventory.setInventoryLineList(new ArrayList<InventoryLine>()); }
		else  {  inventory.getInventoryLineList().clear();  }
	}
	
}
