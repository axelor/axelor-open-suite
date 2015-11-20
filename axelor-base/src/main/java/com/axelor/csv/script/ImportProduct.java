package com.axelor.csv.script;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductService;
import com.google.inject.Inject;

public class ImportProduct {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Inject
	ProductService productService;
	
	public Object importProduct(Object bean, Map values) {
		
		assert bean instanceof Product;
		
		Product product = (Product) bean;
		
		LOG.debug("Product : {}, Variant config: {}", product.getCode(), product.getProductVariantConfig());
		
		if(product.getProductVariantConfig() != null){
			productService.generateProductVariants(product);
		}
		
		return bean;
	}

}
