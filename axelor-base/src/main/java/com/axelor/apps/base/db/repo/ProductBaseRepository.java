package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Product;

public class ProductBaseRepository extends ProductRepository{
	
	@Override
	public Product save(Product product){
		
		product.setFullName("["+product.getCode()+"]"+product.getName());
		
		return super.save(product);
	}
}
