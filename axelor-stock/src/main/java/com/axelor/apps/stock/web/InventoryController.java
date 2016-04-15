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
package com.axelor.apps.stock.web;

import java.io.IOException;
import java.math.BigDecimal;

import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.report.IReport;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.apps.stock.service.LocationLineService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InventoryController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	InventoryService inventoryService;
	
	@Inject
	InventoryRepository inventoryRepo;
	
	private static final Logger LOG = LoggerFactory.getLogger(InventoryController.class);
	
	private static final String PATH = AppSettings.get().get("file.upload.dir");
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void showInventory(ActionRequest request, ActionResponse response) throws AxelorException {

		Inventory inventory = request.getContext().asType(Inventory.class);

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		String name = I18n.get("Inventory")+" "+inventory.getInventorySeq();
		
		String fileLink = ReportFactory.createReport(IReport.INVENTORY, name+"-${date}")
				.addParam("InventoryId", inventory.getId())
				.addParam("Locale", language)
				.addFormat(inventory.getFormatSelect())
				.generate()
				.getFileLink();

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
	}
	
	public void importFile(ActionRequest request, ActionResponse response) throws IOException, AxelorException {
		
		Inventory inventory = inventoryRepo.find( request.getContext().asType(Inventory.class).getId() );
		MetaFile importFile = inventory.getImportFile();
		char separator = ',';
		
		inventoryService.importFile(PATH + System.getProperty("file.separator") + importFile.getFilePath() , separator, inventory);
		response.setFlash(String.format(I18n.get(IExceptionMessage.INVENTORY_8),importFile.getFilePath()));
	}
	
	public void generateStockMove(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Inventory inventory = request.getContext().asType(Inventory.class);
		inventoryService.generateStockMove(inventory);
	}
	
	public void fillInventoryLineList(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Long inventoryId  = (Long) request.getContext().get("id");
		if(inventoryId != null) {
			Inventory inventory = inventoryRepo.find(inventoryId);
			Boolean succeed = inventoryService.fillInventoryLineList(inventory);
			if(succeed == null)  {
				response.setFlash(I18n.get(IExceptionMessage.INVENTORY_9));
			}
			else {
				if(succeed) {
					response.setFlash(I18n.get(IExceptionMessage.INVENTORY_10));
				}
				else  {
					response.setFlash(I18n.get(IExceptionMessage.INVENTORY_11));
				}
			}
		}
		response.setReload(true);
	}
	
	
	public void setInventorySequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Inventory inventory = request.getContext().asType(Inventory.class);
		
		if(inventory.getInventorySeq() ==  null) {
			
			Location location = inventory.getLocation();
			
			response.setValue("inventorySeq", inventoryService.getInventorySequence(location.getCompany()));
		}
	}
	
	public BigDecimal getCurrentQty(Location location, Product product){
		
		if(location != null && product != null){
			LocationLine locationLine = Beans.get(LocationLineService.class).getLocationLine(location, product);
			if(locationLine != null ){
				LOG.debug("Current qty found: {}",locationLine.getCurrentQty());
				return locationLine.getCurrentQty();
			}
		}
		return BigDecimal.ZERO;
	}
	
	
}

  