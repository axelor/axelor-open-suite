package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttribute;
import com.axelor.apps.base.db.ProductVariantValue;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.google.inject.Inject;

public class ProductVariantService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductVariantService.class); 

	@Inject
	private SequenceService sequenceService;
	
	private String exceptionMsg;
	
	@Inject
	public ProductVariantService() {

		this.exceptionMsg = GeneralService.getExceptionAccountingMsg();
		
	}
	
	
	public ProductVariant createProductVariant(ProductVariantAttribute productVariantAttribute1, ProductVariantAttribute productVariantAttribute2, 
			ProductVariantAttribute productVariantAttribute3, ProductVariantAttribute productVariantAttribute4, ProductVariantValue productVariantValue1, 
			ProductVariantValue productVariantValue2, ProductVariantValue productVariantValue3, ProductVariantValue productVariantValue4, boolean usedForStock)  {
		
		ProductVariant productVariant = new ProductVariant();
		productVariant.setProductVariantAttribute1(productVariantAttribute1);
		productVariant.setProductVariantAttribute2(productVariantAttribute2);
		productVariant.setProductVariantAttribute3(productVariantAttribute3);
		productVariant.setProductVariantAttribute4(productVariantAttribute4);
		
		productVariant.setProductVariantValue1(productVariantValue1);
		productVariant.setProductVariantValue2(productVariantValue2);
		productVariant.setProductVariantValue3(productVariantValue3);
		productVariant.setProductVariantValue4(productVariantValue4);
		
		productVariant.setUsedforStock(usedForStock);
		
		return productVariant;
	}
	
	
	public ProductVariantValue createProductVariantValue(ProductVariantAttribute productVariantAttribute, String code, String name, BigDecimal priceExtra)  {
		
		ProductVariantValue productVariantValue = new ProductVariantValue();
		productVariantValue.setCode(code);
		productVariantValue.setName(name);
		productVariantValue.setPriceExtra(priceExtra);
		productVariantValue.setProductVariantAttribute(productVariantAttribute);
		
		return productVariantValue;
	}
	
	
	public ProductVariantAttribute createProductVariantAttribute(String name)  {
		
		ProductVariantAttribute productVariantAttribute = new ProductVariantAttribute();
		productVariantAttribute.setName(name);
		productVariantAttribute.setProductVariantValueList(new ArrayList<ProductVariantValue>());
	
		return productVariantAttribute;
	}
	
	
	public boolean equalsName(ProductVariant productVariant1, ProductVariant productVariant2)  {
		
		if(productVariant1 != null && productVariant2 != null && productVariant1.getName().equals(productVariant2.getName()))  {
			return true;
		}
		
		return false;
		
	}
	
	
	public boolean equals(ProductVariant productVariant1, ProductVariant productVariant2)  {
		
		if(productVariant1 != null && productVariant2 != null 
				&& productVariant1.getProductVariantAttribute1().equals(productVariant2.getProductVariantAttribute1())
				&& productVariant1.getProductVariantAttribute2().equals(productVariant2.getProductVariantAttribute2())
				&& productVariant1.getProductVariantAttribute3().equals(productVariant2.getProductVariantAttribute3())
				&& productVariant1.getProductVariantAttribute4().equals(productVariant2.getProductVariantAttribute4())
				&& productVariant1.getProductVariantValue1().equals(productVariant2.getProductVariantValue1())
				&& productVariant1.getProductVariantValue2().equals(productVariant2.getProductVariantValue2())
				&& productVariant1.getProductVariantValue3().equals(productVariant2.getProductVariantValue3())
				&& productVariant1.getProductVariantValue4().equals(productVariant2.getProductVariantValue4()))  
		{
			return true;
		}
		
		return false;
	}
	
	
	public ProductVariant getProductVariant(ProductVariantAttribute productVariantAttribute1, ProductVariantAttribute productVariantAttribute2, 
			ProductVariantAttribute productVariantAttribute3, ProductVariantAttribute productVariantAttribute4,	ProductVariantValue productVariantValue1, 
			ProductVariantValue productVariantValue2, ProductVariantValue productVariantValue3, ProductVariantValue productVariantValue4, boolean usedForStock)  {
		
		return  ProductVariant.all().filter("self.productVariantAttribute1 = ?1 AND self.productVariantAttribute2 = ?2 AND self.productVariantAttribute3 = ?3 AND " +
				"self.productVariantAttribute4 = ?4 AND self.productVariantValue1 = ?5 AND self.productVariantValue2 = ?6 AND self.productVariantValue3 = ?7 AND " +
				"self.productVariantValue4 = ?8 AND self.usedForStock = 'true'", productVariantAttribute1, productVariantAttribute2, productVariantAttribute3, 
				productVariantAttribute4, productVariantValue1, productVariantValue2, productVariantValue3, productVariantValue4, usedForStock).fetchOne();
		
	}
	
	
	public ProductVariant copyProductVariant(ProductVariant productVariant, boolean usedForStock)  {
		
		return this.createProductVariant(
				productVariant.getProductVariantAttribute1(), 
				productVariant.getProductVariantAttribute2(), 
				productVariant.getProductVariantAttribute3(), 
				productVariant.getProductVariantAttribute4(), 
				productVariant.getProductVariantValue1(), 
				productVariant.getProductVariantValue2(), 
				productVariant.getProductVariantValue3(), 
				productVariant.getProductVariantValue4(), 
				usedForStock);
		
	}

	
	public ProductVariant getStockProductVariant(ProductVariant productVariant)  {
		
		ProductVariant stockProductVariant = this.getProductVariant(
				productVariant.getProductVariantAttribute1(), 
				productVariant.getProductVariantAttribute2(), 
				productVariant.getProductVariantAttribute3(), 
				productVariant.getProductVariantAttribute4(), 
				productVariant.getProductVariantValue1(), 
				productVariant.getProductVariantValue2(), 
				productVariant.getProductVariantValue3(), 
				productVariant.getProductVariantValue4(), 
				true);
				
		if(stockProductVariant == null)  {
			stockProductVariant = this.copyProductVariant(stockProductVariant, true);
		}
		
		return stockProductVariant;
		
	} 
	
	
}
