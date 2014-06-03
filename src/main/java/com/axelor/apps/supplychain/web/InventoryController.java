/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.InventoryLine;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.report.IReport;
import com.axelor.apps.supplychain.service.InventoryService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
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
			mapView.put("title", "Inventory "+inventory.getInventorySeq());
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
		response.setFlash("File "+filePath+" successfully imported.");
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
				response.setFlash("Il n'y a aucun produit contenu dans l'emplacement de stock.");
			}
			else {
				if(inventoryLineList.size() > 0) {
					response.setFlash("La liste des lignes d'inventaire a été rempli.");
				}
				else  {
					response.setFlash("Aucune lignes d'inventaire n'a été créée.");
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
}
  