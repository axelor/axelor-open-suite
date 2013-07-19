package com.axelor.apps.supplychain.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.InventoryLine;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InventoryService {
	
	@Inject
	StockMoveService stockMoveService;
	
	@Transactional
	public void importFile(String filePath, char separator, Inventory inventory) throws IOException, AxelorException {
		
		List<InventoryLine> inventoryLineList = inventory.getInventoryLineList();
		
		List<String[]> data = null;
		try {
			data = CsvTool.cSVFileReader(filePath, separator);
		}catch(Exception e) {
			throw new AxelorException("There is currently no such file in the specified folder or the folder may not exists.", IException.CONFIGURATION_ERROR);
		}
		
		if (data == null || data.isEmpty())
			throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);
		
		HashMap<String,InventoryLine> inventoryLineMap = new HashMap<String,InventoryLine>();
		
		for (InventoryLine line : inventoryLineList) {
			if (line.getProduct() != null)
				inventoryLineMap.put(line.getProduct().getCode(), line);
		}
		
		for (String[] line : data) {
			if (line.length < 6)
				throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);
			
			String code = line[1].replace("\"", "");
			
			Integer realQty = 0;
			try {
				realQty = Integer.valueOf(line[4].replace("\"", ""));
			}catch(NumberFormatException e) {
				throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);
			}
			
			String description = line[5].replace("\"", "");
			
			if (inventoryLineMap.containsKey(code)) {
				inventoryLineMap.get(code).setRealQty(new BigDecimal(realQty));
				inventoryLineMap.get(code).setDescription(description);
			}
			else {
				Integer currentQty = 0;
				try {
					currentQty = Integer.valueOf(line[2].replace("\"", ""));
				}catch(NumberFormatException e) {
					throw new AxelorException("An error occurred while importing the file data. Please contact your application administrator to check Traceback logs.", IException.CONFIGURATION_ERROR);
				}
				
				InventoryLine inventoryLine = new InventoryLine();
				Product product = Product.findByCode(code);
				if (product == null || product.getApplicationTypeSelect() != IProduct.PRODUCT_TYPE || !product.getProductTypeSelect().equals(IProduct.STORABLE))
					throw new AxelorException("An error occurred while importing the file data, product not found with code : "+code, IException.CONFIGURATION_ERROR);
				inventoryLine.setProduct(product);
				inventoryLine.setInventory(inventory);
				inventoryLine.setCurrentQty(new BigDecimal(currentQty));
				inventoryLine.setRealQty(new BigDecimal(realQty));
				inventoryLine.setDescription(description);
				inventoryLineList.add(inventoryLine);
			}
		}
		inventory.setInventoryLineList(inventoryLineList);
		
		inventory.save();
	}
	
	public void generateStockMove(Inventory inventory) throws AxelorException {
		
		List<InventoryLine> inventoryLineList = inventory.getInventoryLineList();
		StockMove stockMove = null;
		List<StockMoveLine> stockMoveLineList = null;
		
		LocalDate inventoryDate = inventory.getDateT().toLocalDate();
		
		Location toLocation = inventory.getLocation();
		Company company = toLocation.getCompany();
		Location fromLocation = company.getInventoryVirtualLocation();
		
		for (InventoryLine inventoryLine : inventoryLineList) {
			BigDecimal currentQty = inventoryLine.getCurrentQty();
			BigDecimal realQty = inventoryLine.getRealQty();
			Product product = inventoryLine.getProduct();
			
			if (currentQty.compareTo(realQty) != 0) {
				BigDecimal diff = realQty.subtract(currentQty);
				
				if (toLocation == null || company == null || product == null ) {
					throw new AxelorException("Informations manquantes dans l'inventaire "+inventory.getInventorySeq(), IException.CONFIGURATION_ERROR);
				}
				
				if (stockMove == null) {
					
					stockMove = stockMoveService.createStocksMoves(null, company, null, fromLocation, toLocation, inventoryDate, inventoryDate);
					stockMove.setTypeSelect(IStockMove.INTERNAL);
					stockMove.setName(inventory.getInventorySeq());
				}
				
				StockMoveLine stockMoveLine = stockMoveService.createStockMoveLine(product, diff, product.getUnit(), null, stockMove, 0);
				
				if (stockMoveLine == null)
					throw new AxelorException("Produit incorrect dans la ligne de l'inventaire "+inventory.getInventorySeq(), IException.CONFIGURATION_ERROR);
				
				if (stockMoveLineList == null)
					stockMoveLineList = new ArrayList<StockMoveLine>();
				stockMoveLineList.add(stockMoveLine);
			}
		}
		if (stockMoveLineList != null && stockMove != null) {
			stockMove.setStockMoveLineList(stockMoveLineList);
			stockMoveService.validate(stockMove);
		}
	}
}
