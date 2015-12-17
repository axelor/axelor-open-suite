package com.axelor.csv.script;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ImportProduct {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Inject
	ProductService productService;
	
	@Inject
	MetaFiles metaFiles;
	
	public Object importProduct(Object bean, Map<String,Object> values) {
		
		assert bean instanceof Product;
		
		Product product = (Product) bean;
		
		final Path path = (Path) values.get("__path__");
		String fileName = (String) values.get("picture_fileName");
		if(Strings.isNullOrEmpty((fileName)))  {  return bean;  }
		
	    final File image = path.resolve(fileName).toFile(); 

		try {
			final MetaFile metaFile = metaFiles.upload(image);
			product.setPicture(metaFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bean;
	}
	
	public Object generateVariant(Object bean, Map<String,Object> values) {
		
		assert bean instanceof Product;
		
		Product product = (Product) bean;
		
		LOG.debug("Product : {}, Variant config: {}", product.getCode(), product.getProductVariantConfig());
		
		if(product.getProductVariantConfig() != null){
			productService.generateProductVariants(product);
		}
		
		return bean;
	}

}
