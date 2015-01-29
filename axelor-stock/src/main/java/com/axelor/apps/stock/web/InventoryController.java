/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductServiceImpl;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.apps.stock.service.LocationLineService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class InventoryController {

	@Inject
	InventoryService inventoryService;
	
	private static final Logger LOG = LoggerFactory.getLogger(InventoryController.class);
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showInventory(ActionRequest request, ActionResponse response) {

		Inventory inventory = request.getContext().asType(Inventory.class);
		int format = inventory.getFormatSelect();

		StringBuilder url = new StringBuilder();

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(
				new ReportSettings(IReport.INVENTORY, ((format == 1) ? "pdf":(format == 2) ? "xls":"pdf"))
				.addParam("Locale", language)
				.addParam("InventoryId", inventory.getId().toString())
				.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", I18n.get("Inventory")+" "+inventory.getInventorySeq());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	public void importFile(ActionRequest request, ActionResponse response) throws IOException, AxelorException {
		
		Inventory inventory = request.getContext().asType(Inventory.class);
		String filePath = inventory.getImportFilePath();
		char separator = ',';
		
		inventoryService.importFile(filePath, separator, inventory);
		response.setFlash(String.format(I18n.get(IExceptionMessage.INVENTORY_8),filePath));
	}
	
	public void generateStockMove(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Inventory inventory = request.getContext().asType(Inventory.class);
		inventoryService.generateStockMove(inventory);
	}
	
	public void fillInventoryLineList(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Inventory inventory = request.getContext().asType(Inventory.class);
		if(inventory != null) {
			List<InventoryLine> inventoryLineList = inventoryService.fillInventoryLineList(inventory);
			if(inventoryLineList == null)  {
				response.setFlash(I18n.get(IExceptionMessage.INVENTORY_9));
			}
			else {
				if(inventoryLineList.size() > 0) {
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

  