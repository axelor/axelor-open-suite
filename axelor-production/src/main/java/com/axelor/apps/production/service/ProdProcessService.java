package com.axelor.apps.production.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.common.base.Joiner;

public class ProdProcessService {
	
	public void validateProdProcess(ProdProcess prodProcess, BillOfMaterial bom) throws AxelorException{
		Map<Product,BigDecimal> bomMap = new HashMap<Product,BigDecimal>();
		for (BillOfMaterial bomIt : bom.getBillOfMaterialList()) {
			bomMap.put(bomIt.getProduct(), bomIt.getQty());
		}
		for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
			for (ProdProduct prodProduct : prodProcessLine.getToConsumeProdProductList()) {
				if(!bomMap.containsKey(prodProduct.getProduct())){
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROD_PROCESS_USELESS_PRODUCT), prodProduct.getProduct().getName()), IException.CONFIGURATION_ERROR);
				}
				bomMap.put(prodProduct.getProduct(), bomMap.get(prodProduct.getProduct()).subtract(prodProduct.getQty()));
			}
		}
		Set<Product> keyList = bomMap.keySet();
		Map<Product,BigDecimal> copyMap = new HashMap<Product,BigDecimal>();
		List<String> nameProductList = new ArrayList<String>();
		for (Product product : keyList) {
			if(bomMap.get(product).compareTo(BigDecimal.ZERO) > 0){
				copyMap.put(product, bomMap.get(product));
				nameProductList.add(product.getName());
			}
		}
		if(!copyMap.isEmpty()){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROD_PROCESS_MISS_PRODUCT), Joiner.on(",").join(nameProductList)), IException.CONFIGURATION_ERROR);
		}
	}
	
}
