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
package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.supplychain.db.SalesOrderLine;

public class PriceListService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PriceListService.class); 

	
	private PriceListLine getPriceListLine(Product product, List<PriceListLine> priceLineLineList)  {
	
		if(priceLineLineList != null)  {
			for(PriceListLine priceListLine : priceLineLineList)  {
				
				if(priceListLine.getProduct() != null && priceListLine.getProduct().equals(product))  {
					return priceListLine;
				}
			}
		}
		
		return null;
	}
	
	
	private PriceListLine getPriceListLine(ProductCategory productCategory, List<PriceListLine> priceLineLineList)  {
		
		if(priceLineLineList != null && productCategory != null)  {
			for(PriceListLine priceListLine : priceLineLineList)  {
				
				if(priceListLine.getProductCategory() != null && priceListLine.getProductCategory().equals(productCategory))  {
					return priceListLine;
				}
			}
		}
		
		return null;
	}
	
	
	public PriceListLine getPriceListLine(Product product, PriceList priceList)  {
		
		PriceListLine priceListLine = null;
		
		if(product != null && priceList != null)  {
			priceListLine = this.getPriceListLine(product, priceList.getPriceListLineList());
			
			if(priceListLine == null)  {
				priceListLine = this.getPriceListLine(product.getProductCategory(), priceList.getPriceListLineList());
			}
		}
		
		return priceListLine;
	}
	
	
	public int getDiscountTypeSelect(PriceListLine priceListLine)  {
		
		return priceListLine.getAmountTypeSelect();
		
	}
	
	
	public BigDecimal getDiscountAmount(PriceListLine priceListLine, BigDecimal unitPrice)  {
		
		switch (priceListLine.getTypeSelect()) {
			case IPriceListLine.TYPE_ADDITIONNAL:
				
				return priceListLine.getAmount();
				
			case IPriceListLine.TYPE_DISCOUNT:
				
				return priceListLine.getAmount().negate();
				
			case IPriceListLine.TYPE_REPLACE:
		
				return priceListLine.getAmount().subtract(unitPrice);
	
			default:
				return BigDecimal.ZERO;
		}
	}
	
	
	
	
	
	public BigDecimal getUnitPriceDiscounted(PriceListLine priceListLine, BigDecimal unitPrice)  {
		
		switch (priceListLine.getTypeSelect()) {
			case IPriceListLine.TYPE_ADDITIONNAL:
				
				if(priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED)  {
					return unitPrice.add(priceListLine.getAmount());
				}
				else if(priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_PERCENT)  {
					return unitPrice.multiply(
							BigDecimal.ONE.add(
									priceListLine.getAmount().divide(new BigDecimal(100))));
				}
				
			case IPriceListLine.TYPE_DISCOUNT:
				
				if(priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED)  {
					return unitPrice.subtract(priceListLine.getAmount());
				}
				else if(priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_PERCENT)  {
					return unitPrice.multiply(
							BigDecimal.ONE.subtract(
									priceListLine.getAmount().divide(new BigDecimal(100))));
				}
				
			case IPriceListLine.TYPE_REPLACE:
		
				return priceListLine.getAmount();
	
			default:
				return unitPrice;
		}
	}
}
