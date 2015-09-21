package com.axelor.apps.production.db.repo;

import java.awt.Font;
import java.io.File;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;

import com.axelor.app.AppSettings;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;

public class OperationOrderManagementRepository extends OperationOrderRepository{
	@Override
	public OperationOrder save(OperationOrder entity){
		
		if(entity.getBarCode() == null){
			entity = super.save(entity);
			try{
				Barcode barcode  = BarcodeFactory.createEAN13(String.format("%012d", entity.getId()));
				barcode.setBarHeight(86);
			    barcode.setBarWidth(1);
			    barcode.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

			    File imgFile = new File(AppSettings.get().get("file.upload.dir")+String.format("/barCode%d.png",entity.getId()));

			    BarcodeImageHandler.savePNG(barcode, imgFile);
			    
			    entity.setBarCode(Beans.get(MetaFiles.class).upload(imgFile));
			}
			catch(Exception e){
				
			}
		}
		
		return super.save(entity);
	}
}
