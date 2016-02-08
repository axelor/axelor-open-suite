/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.awt.Font;
import java.io.File;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Product;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;

public class ProductBaseRepository extends ProductRepository{
	
	@Override
	public Product save(Product product){
		
		product.setFullName("["+product.getCode()+"]"+product.getName());
		
		product = super.save(product);
		
		if(product.getBarCode() == null){
			try{
				Barcode barcode  = BarcodeFactory.createEAN13(String.format("%012d", product.getId()));
				barcode.setBarHeight(50);
			    barcode.setBarWidth(1);
			    barcode.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
			    
			    File uploadDir = new File(AppSettings.get().get("file.upload.dir"));
			    
			    if(uploadDir.exists()){
			    
			    	File imgFile = new File(uploadDir, String.format("/ProductBarCode%d.png", product.getId()));
			    
			    	BarcodeImageHandler.savePNG(barcode, imgFile);

			    	product.setBarCode(Beans.get(MetaFiles.class).upload(imgFile));
			    }
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return super.save(product);
	}
}
