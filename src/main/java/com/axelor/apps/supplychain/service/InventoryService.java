package com.axelor.apps.supplychain.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.InventoryLine;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.persist.Transactional;

public class InventoryService {
	
	@Transactional
	public Inventory importFile(String filePath, char separator, Inventory inventory) throws IOException, AxelorException {
		
		List<InventoryLine> inventoryLineList = inventory.getInventoryLineList();
		
		List<String[]> data = CsvTool.cSVFileReader(filePath, separator);
		
		if (data == null || data.isEmpty())
			throw new AxelorException("L'importation du fichier "+filePath+" a échouée", IException.CONFIGURATION_ERROR);
		
		HashMap<String,InventoryLine> inventoryLineMap = new HashMap<String,InventoryLine>();
		
		for (InventoryLine line : inventoryLineList) {
			if (line.getProduct() != null)
				inventoryLineMap.put(line.getProduct().getCode(), line);
		}
		
		for (String[] line : data) {
			if (line.length < 6)
				throw new AxelorException("Données importées invalides", IException.CONFIGURATION_ERROR);
			String code = line[1].replace("\"", "");
			Integer realQty = Integer.valueOf(line[4].replace("\"", ""));
			String description = line[5].replace("\"", "");
			
			if (inventoryLineMap.containsKey(code)) {
				inventoryLineMap.get(code).setRealQty(new BigDecimal(realQty));
				inventoryLineMap.get(code).setDescription(description);
			}
			else {
				Integer currentQty = Integer.valueOf(line[2].replace("\"", ""));
				InventoryLine inventoryLine = new InventoryLine();
				Product product = Product.findByCode(code);
				if (product == null)
					throw new AxelorException("Produit "+code+" inconnu", IException.CONFIGURATION_ERROR);
				inventoryLine.setProduct(product);
				inventoryLine.setInventory(inventory);
				inventoryLine.setCurrentQty(new BigDecimal(currentQty));
				inventoryLine.setRealQty(new BigDecimal(realQty));
				inventoryLine.setDescription(description);
				inventoryLineList.add(inventoryLine);
			}
		}
		inventory.setInventoryLineList(inventoryLineList);
		
		return inventory;
	}
}
