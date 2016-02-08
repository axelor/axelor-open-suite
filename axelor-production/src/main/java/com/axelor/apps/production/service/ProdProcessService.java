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
