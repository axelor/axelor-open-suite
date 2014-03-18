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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProductVariant;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttr;
import com.axelor.apps.base.db.ProductVariantConfig;
import com.axelor.apps.base.db.ProductVariantValue;
import com.axelor.exception.AxelorException;
import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductService.class);
		
	@Inject
	private ProductVariantService productVariantService;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateProductPrice(Product product)  {
		
		this.updateSalePrice(product);
		
		product.save();
		
	}
	
	
	/**
	 * Retourne le prix d'un produit à une date t.
	 * 
	 * @param product
	 * @param date
	 * @return
	 */
	public BigDecimal getPrice(Product product, boolean isPurchase){
		
		if(isPurchase)  {  return product.getPurchasePrice();  }
		else  {  return product.getSalePrice();  }

	}
	
	
	public void updateSalePrice(Product product)  {
		
		BigDecimal managePriceCoef = product.getManagPriceCoef();
		
		if(product.getCostPrice() != null)  {
			
			if(product.getProductVariant() != null)  {
				
				product.setCostPrice(product.getCostPrice().add(this.getProductExtraPrice(product.getProductVariant(), IProductVariant.APPLICATION_COST_PRICE)));
				
			}
			
		}
		
		if(product.getCostPrice() != null && managePriceCoef != null)  {
			
			product.setSalePrice((product.getCostPrice().multiply(managePriceCoef)).setScale(5, BigDecimal.ROUND_HALF_UP));
			
			if(product.getProductVariant() != null)  {
				
				product.setSalePrice(product.getSalePrice().add(this.getProductExtraPrice(product.getProductVariant(), IProductVariant.APPLICATION_SALE_PRICE)));
				
			}
		}
		
		if(product.getProductVariantConfig() != null)  {
			
			this.updateSalePriceOfVariant(product);
			
		}
	}
	
	
	public void updateSalePriceOfVariant(Product product)  {
		
		List<Product> productVariantList = Product.filter("self.parentProduct = ?1", product).fetch();
		
		for(Product productVariant : productVariantList)  {
			
			productVariant.setCostPrice(product.getCostPrice());
			productVariant.setSalePrice(product.getSalePrice());
			productVariant.setManagPriceCoef(product.getManagPriceCoef());
			
			this.updateSalePrice(productVariant);
			
		}
		
		
	}
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generateProductVariants(Product productModel)  {
		
		List<ProductVariant> productVariantList = this.getProductVariantList(productModel.getProductVariantConfig());
		
		for(ProductVariant productVariant : productVariantList)  {
			
			productVariant.save();
			
			this.createProduct(productModel, productVariant).save();
			
		}
		
	}
	
	
	public Product createProduct(Product productModel, ProductVariant productVariant)  {
		
		Product product = new Product(
				productModel.getName()+" ("+productVariant.getName()+")",
				productModel.getCode()+"-"+productVariant.getId(),
				productModel.getDescription()+"<br>"+productVariant.getName(), 
				productModel.getInternalDescription()+"<br>"+productVariant.getName(),
				productModel.getPicture(), 
				productModel.getProductCategory(), 
				productModel.getProductFamily(), 
				productModel.getUnit(), 
				productModel.getApplicationTypeSelect(), 
				productModel.getSaleSupplySelect(), 
				productModel.getProductTypeSelect(), 
				productModel.getProcurementMethodSelect(), 
				productModel.getIsRawMaterial(), 
				productModel.getSaleCurrency(), 
				productModel.getPurchaseCurrency(), 
				productModel.getStartDate(), 
				productModel.getEndDate(), 
				productModel.getInvoiceLineType());
		
		productModel.setIsModel(true);
		
		product.setIsModel(false);
		product.setParentProduct(productModel);
		product.setProductVariant(productVariant);
		
		this.updateSalePrice(product);
		
		return product;
	}
	
	
	/**
	 * 
	 * @param productVariant
	 * @param applicationPriceSelect
	 * 		- 1 : Sale price
	 * 		- 2 : Cost price
	 * @return
	 */
	public BigDecimal getProductExtraPrice(ProductVariant productVariant, int applicationPriceSelect)  {
		
		BigDecimal extraPrice = BigDecimal.ZERO;
		
		ProductVariantValue productVariantValue1 = productVariant.getProductVariantValue1();
		ProductVariantValue productVariantValue2 = productVariant.getProductVariantValue2();
		ProductVariantValue productVariantValue3 = productVariant.getProductVariantValue3();
		ProductVariantValue productVariantValue4 = productVariant.getProductVariantValue4();
		
		
		if(productVariantValue1 != null && productVariantValue1.getApplicationPriceSelect() == applicationPriceSelect)  {
			
			extraPrice = extraPrice.add(productVariantValue1.getPriceExtra());
			
		}
		
		if(productVariantValue2 != null)  {
			
			extraPrice = extraPrice.add(productVariantValue2.getPriceExtra());
			
		}

		if(productVariantValue3 != null)  {
			
			extraPrice = extraPrice.add(productVariantValue3.getPriceExtra());
			
		}
		
		if(productVariantValue4 != null)  {
			
			extraPrice = extraPrice.add(productVariantValue4.getPriceExtra());
			
		}
		
		return extraPrice;
		
	}
	
	
	
	public List<ProductVariant> getProductVariantList(ProductVariantConfig productVariantConfig)  {
		
		List<ProductVariant> productVariantList = Lists.newArrayList();
		
		if(productVariantConfig.getProductVariantAttr1() != null && productVariantConfig.getProductVariantValue1Set() != null)  {
			
			for(ProductVariantValue productVariantValue1 : productVariantConfig.getProductVariantValue1Set())  {
				
				productVariantList.addAll(this.getProductVariantList(productVariantConfig, productVariantValue1));
				
			}
		}
		
		return productVariantList;
	}
	
	
	public List<ProductVariant> getProductVariantList(ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1)  {
		
		List<ProductVariant> productVariantList = Lists.newArrayList();
		
		if(productVariantConfig.getProductVariantAttr2() != null && productVariantConfig.getProductVariantValue2Set() != null)  {
			
			for(ProductVariantValue productVariantValue2 : productVariantConfig.getProductVariantValue2Set())  {

				productVariantList.addAll(this.getProductVariantList(productVariantConfig, productVariantValue1, productVariantValue2));
				
			}
		}
		
		else  {
			
			productVariantList.add( 
					this.createProductVariant(productVariantConfig, productVariantValue1, null, null, 	null));
		}
		
		return productVariantList;
		
	}
	
	
	public List<ProductVariant> getProductVariantList(ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1, ProductVariantValue productVariantValue2)  {
		
		List<ProductVariant> productVariantList = Lists.newArrayList();
		
		if(productVariantConfig.getProductVariantAttr3() != null && productVariantConfig.getProductVariantValue3Set() != null)  {
			
			for(ProductVariantValue productVariantValue3 : productVariantConfig.getProductVariantValue3Set())  {
				
				productVariantList.addAll(this.getProductVariantList(productVariantConfig, productVariantValue1, productVariantValue2, productVariantValue3));
			}
		}
		
		else  {
			
			productVariantList.add(
					this.createProductVariant(productVariantConfig, productVariantValue1, productVariantValue2, null, 	null));
		}
		
		return productVariantList;
		
	}
	
	
	public List<ProductVariant> getProductVariantList(ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1, ProductVariantValue productVariantValue2,
			ProductVariantValue productVariantValue3)  {
		
		List<ProductVariant> productVariantList = Lists.newArrayList();
		
		if(productVariantConfig.getProductVariantAttr4() != null && productVariantConfig.getProductVariantValue4Set() != null)  {
			
			for(ProductVariantValue productVariantValue4 : productVariantConfig.getProductVariantValue4Set())  {
				
				productVariantList.add(
						this.createProductVariant(productVariantConfig, productVariantValue1, productVariantValue2, productVariantValue3, 	productVariantValue4));
			}
		}
		
		else  {
			
			productVariantList.add(
					this.createProductVariant(productVariantConfig, productVariantValue1, productVariantValue2, productVariantValue3, 	null));
		}
		
		return productVariantList;
		
	}
	
	
	public ProductVariant createProductVariant(ProductVariantConfig productVariantConfig, ProductVariantValue productVariantValue1, ProductVariantValue productVariantValue2,
			ProductVariantValue productVariantValue3, ProductVariantValue productVariantValue4)  {
		
		ProductVariantAttr productVariantAttr1 = null, productVariantAttr2 = null, productVariantAttr3 = null, productVariantAttr4 = null;
		if(productVariantValue1 != null)  {
			productVariantAttr1 = productVariantConfig.getProductVariantAttr1();
		}
		if(productVariantValue2 != null)  {
			productVariantAttr2 = productVariantConfig.getProductVariantAttr2();
		}
		if(productVariantValue3 != null)  {
			productVariantAttr3 = productVariantConfig.getProductVariantAttr3();
		}
		if(productVariantValue4 != null)  {
			productVariantAttr4 = productVariantConfig.getProductVariantAttr4();
		}
		
		return productVariantService.createProductVariant(
				productVariantAttr1, 
				productVariantAttr2, 
				productVariantAttr3, 
				productVariantAttr4, 
				productVariantValue1, 
				productVariantValue2, 
				productVariantValue3, 
				productVariantValue4, 
				false);
		
	}
 	
	
	
	
}