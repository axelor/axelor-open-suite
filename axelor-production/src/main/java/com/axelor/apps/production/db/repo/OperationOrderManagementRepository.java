/*
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
package com.axelor.apps.production.db.repo;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.ValidationException;

import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;


public class OperationOrderManagementRepository extends OperationOrderRepository {
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	protected BarcodeGeneratorService barcodeGeneratorService;
	
	@Inject
	protected AppProductionService appProductionService;
	
	@Override
	public OperationOrder save(OperationOrder entity){
		
		if(entity.getBarCode() == null) {
			entity = super.save(entity);
			try {
				String stringId=entity.getId().toString();
				boolean addPadding=true;
				InputStream inStream = barcodeGeneratorService.createBarCode(stringId,
						appProductionService.getAppProduction().getBarcodeTypeConfig(), addPadding);
				if (inStream != null) {
			    	MetaFile barcodeFile =  metaFiles.upload(inStream, String.format("OppOrderBarcode%d.png", entity.getId()));
			    	entity.setBarCode(barcodeFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch (AxelorException e) {
				throw new ValidationException(e.getMessage());
			}
		}
		
		return super.save(entity);
	}
}
