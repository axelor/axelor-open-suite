package com.axelor.apps.base.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;

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
	
	
}