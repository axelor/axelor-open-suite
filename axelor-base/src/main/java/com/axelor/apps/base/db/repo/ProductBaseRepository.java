/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db.repo;

import java.io.IOException;
import java.io.InputStream;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;


public class ProductBaseRepository extends ProductRepository{
	
	@Inject
	private MetaFiles metaFiles;

	@Inject
	protected GeneralService generalService;

	
	@Override
	public Product save(Product product){
		
		product.setFullName("["+product.getCode()+"]"+product.getName());
		
		product = super.save(product);
		
		if((product.getBarCode() == null)&&
           (generalService.getGeneral().getActivateBarCodeGeneration())) {
			    	
			try {
				InputStream inStream = BarcodeGeneratorService.createBarCode(product.getId());
				if (inStream != null) {
			    	MetaFile barcodeFile =  metaFiles.upload(inStream, String.format("ProductBarCode%d.png", product.getId()));
					product.setBarCode(barcodeFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
		}
		
		return super.save(product);
	}
	
	@Override
	public Product copy(Product product, boolean deep) {
		
		Product copy = super.copy(product, deep);
		copy.setBarCode(null);
		
		return copy;
		
	}
}
