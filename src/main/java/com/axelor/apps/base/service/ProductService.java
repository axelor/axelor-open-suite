/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;

public class ProductService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductService.class);
		
	/**
	 * Retourne le prix d'un produit à une date t.
	 * 
	 * @param product
	 * @param date
	 * @return
	 */
	public BigDecimal getPrice(Product product, boolean isPurchase){
		
		BigDecimal price = BigDecimal.ZERO;
		
		if(isPurchase)  {  return product.getPurchasePrice();  }
		else  {  return product.getSalePrice();  }

	}
	
	
	public void updateSalePrice(Product product) throws AxelorException  {
		
		BigDecimal costPrice = product.getCostPrice();
		BigDecimal managePriceCoef = product.getManagPriceCoef();
		
		if(costPrice != null && managePriceCoef != null)  {
			
			product.setSalePrice((costPrice.multiply(managePriceCoef)).setScale(5, BigDecimal.ROUND_HALF_UP));
		}
	}
	
	
	
}