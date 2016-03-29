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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TrackingNumberRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.LocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InventoryService {

	@Inject
	private InventoryLineService inventoryLineService;

	@Inject
	private SequenceService sequenceService;

	@Inject
	private StockConfigService stockConfigService;

	@Inject
	private ProductRepository productRepo;
	
	@Inject
	private InventoryRepository inventoryRepo;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Inventory createInventoryFromWizard(LocalDate date, String description, Location location, boolean excludeOutOfStock,
			boolean includeObsolete, ProductFamily productFamily, ProductCategory productCategory) throws Exception {

		Inventory inventory = inventoryRepo.save(this.createInventory(date, description, location, excludeOutOfStock, includeObsolete, productFamily, productCategory));

		this.fillInventoryLineList(inventory);

		return inventory;
	}



	public Inventory createInventory(LocalDate date, String description, Location location, boolean excludeOutOfStock, boolean includeObsolete,
			ProductFamily productFamily, ProductCategory productCategory) throws AxelorException  {

		if(location == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_1),IException.CONFIGURATION_ERROR);
		}

		Inventory inventory = new Inventory();

		inventory.setInventorySeq(this.getInventorySequence(location.getCompany()));

		inventory.setDateT(date.toDateTimeAtStartOfDay());

		inventory.setDescription(description);

		inventory.setFormatSelect(IAdministration.PDF);

		inventory.setLocation(location);

		inventory.setExcludeOutOfStock(excludeOutOfStock);

		inventory.setIncludeObsolete(includeObsolete);

		inventory.setProductCategory(productCategory);

		inventory.setProductFamily(productFamily);

		inventory.setStatusSelect(InventoryRepository.STATUS_DRAFT);

		return inventory;
	}


	public String getInventorySequence(Company company) throws AxelorException   {

		String ref = sequenceService.getSequenceNumber(IAdministration.INVENTORY, company);
		if (ref == null)
			throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_2)+" "+company.getName(),
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
				throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_3), IException.CONFIGURATION_ERROR);

			String code = line[1].replace("\"", "");
			String trackingNumberSeq = line[3].replace("\"", "");

			Integer realQty = 0;
			try {
				realQty = Integer.valueOf(line[6].replace("\"", ""));
			}catch(NumberFormatException e) {
				throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_3), IException.CONFIGURATION_ERROR);
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
					throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_3), IException.CONFIGURATION_ERROR);
				}

				InventoryLine inventoryLine = new InventoryLine();
				List<Product> productList = productRepo.all().filter("self.code = :code").bind("code", code).fetch();
				if (productList != null && !productList.isEmpty()){
					if (productList.size() > 1){
						throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_12)+" "+code, IException.CONFIGURATION_ERROR);
					}
				}
				Product product = productList.get(0);
				if (product == null || !product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE))
					throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_4)+" "+code, IException.CONFIGURATION_ERROR);
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

		inventoryRepo.save(inventory);
	}


	public List<String[]> getDatas(String filePath, char separator) throws AxelorException  {

		List<String[]> data = null;
		try {
			data = CsvTool.cSVFileReader(filePath, separator);
			
		} catch(Exception e) {
			throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_5), IException.CONFIGURATION_ERROR);
		}

		if (data == null || data.isEmpty())  {
			throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_3), IException.CONFIGURATION_ERROR);
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
			return Beans.get(TrackingNumberRepository.class).findBySeq(sequence);
		}

		return null;

	}

	public StockMove generateStockMove(Inventory inventory) throws AxelorException {

		Location toLocation = inventory.getLocation();
		Company company = toLocation.getCompany();
		StockMoveService stockMoveService = Beans.get(StockMoveService.class);

		if (company == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVENTORY_6), toLocation.getName()), IException.CONFIGURATION_ERROR);
		}

		String inventorySeq = inventory.getInventorySeq();

		StockMove stockMove = this.createStockMoveHeader(inventory, company, toLocation, inventory.getDateT().toLocalDate(), inventorySeq);

		for (InventoryLine inventoryLine : inventory.getInventoryLineList()) {
			BigDecimal currentQty = inventoryLine.getCurrentQty();
			BigDecimal realQty = inventoryLine.getRealQty();
			Product product = inventoryLine.getProduct();

			if (currentQty.compareTo(realQty) != 0) {
				BigDecimal diff = realQty.subtract(currentQty);

				StockMoveLine stockMoveLine = Beans.get(StockMoveLineService.class).createStockMoveLine(
						product, product.getName(), 
						product.getDescription(), diff,
						product.getCostPrice(),
						product.getUnit(), stockMove, 0,false, BigDecimal.ZERO);
				if (stockMoveLine == null)  {
					throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_7)+" "+inventorySeq, IException.CONFIGURATION_ERROR);
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

		StockMove stockMove = Beans.get(StockMoveService.class).createStockMove(null, null, company, null,
				stockConfigService.getInventoryVirtualLocation(stockConfigService.getStockConfig(company)), toLocation, inventoryDate, inventoryDate, null);

		stockMove.setTypeSelect(StockMoveRepository.TYPE_INTERNAL);
		stockMove.setName(name);

		return stockMove;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Boolean fillInventoryLineList(Inventory inventory) throws AxelorException {

		if(inventory.getLocation() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.INVENTORY_1),IException.CONFIGURATION_ERROR);
		}

		this.initInventoryLines(inventory);

		List<? extends LocationLine> locationLineList = this.getLocationLines(inventory);

		if (locationLineList != null) {
			Boolean succeed = false;
			for (LocationLine locationLine : locationLineList) {
				inventory.addInventoryLineListItem(this.createInventoryLine(inventory, locationLine));
				succeed = true;
			}
			inventoryRepo.save(inventory);
			return succeed;
		}
		return null;
	}


	public List<? extends LocationLine> getLocationLines(Inventory inventory)  {

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

		return Beans.get(LocationLineRepository.class).all().filter(query, params.toArray()).fetch();

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
