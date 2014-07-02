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
package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;

public class PriceListService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PriceListService.class); 

	
	public PriceListLine getPriceListLine(Product product, BigDecimal qty, PriceList priceList)  {
		
		PriceListLine priceListLine = null;
		
		if(product != null && priceList != null)  {
			priceListLine = PriceListLine.filter("self.product = ?1 AND self.minQty <= ?2 ORDER BY self.minQty DESC", product, qty).fetchOne();
			
			if(priceListLine == null && product.getProductCategory() != null)  {
				priceListLine = PriceListLine.filter("self.productCategory = ?1 AND self.minQty <= ?2 ORDER BY self.minQty DESC", product.getProductCategory(), qty).fetchOne();
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
	
	
	public BigDecimal getUnitPriceDiscounted(PriceList priceList, BigDecimal unitPrice)  {
		
		BigDecimal discountPercent = priceList.getGeneralDiscount(); 
		
		return unitPrice.multiply(
				BigDecimal.ONE.subtract(
						discountPercent.divide(new BigDecimal(100))));
		
	}
	
	
	public BigDecimal computeDiscount(BigDecimal unitPrice, int discountTypeSelect, BigDecimal discountAmount)  {
		
		if(discountTypeSelect == IPriceListLine.AMOUNT_TYPE_FIXED)  {
			return  unitPrice.add(discountAmount);
		}
		else if(discountTypeSelect == IPriceListLine.AMOUNT_TYPE_PERCENT)  {
			return unitPrice.multiply(
					BigDecimal.ONE.add(
							discountAmount.divide(new BigDecimal(100))));
		}
		
		return unitPrice;
	}
	
	
	public Map<String, Object>  getDiscounts(PriceList priceList, PriceListLine priceListLine, BigDecimal price)  {
		
		Map<String, Object> discounts = new HashMap<String, Object>();
		
		if(priceListLine != null)  {
			if(priceList.getIsDisplayed())  {
				discounts.put("discountAmount", this.getDiscountAmount(priceListLine, price));
				discounts.put("discountTypeSelect", this.getDiscountTypeSelect(priceListLine));
			}
			else  {
				discounts.put("price", this.getUnitPriceDiscounted(priceListLine, price));
			}
		}
		else  {
			if(priceList.getIsDisplayed())  {
				discounts.put("discountAmount", priceList.getGeneralDiscount());
				discounts.put("discountTypeSelect", IPriceListLine.AMOUNT_TYPE_PERCENT);
			}
			else  {
				discounts.put("price", this.getUnitPriceDiscounted(priceList, price));
			}
		}
		
		return discounts;
	}
}
