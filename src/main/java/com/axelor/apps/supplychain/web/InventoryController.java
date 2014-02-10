/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.InventoryLine;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.service.InventoryService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaUser;
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
		AxelorSettings axelorSettings = AxelorSettings.get();

		MetaUser metaUser = MetaUser.findByUser(AuthUtils.getUser());
		String language = metaUser != null? (metaUser.getLanguage() == null || metaUser.getLanguage().equals(""))? "en" : metaUser.getLanguage() : "en"; 

		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Inventory.rptdesign&__format="+((format == 1) ? "pdf":(format == 2) ? "xls":"pdf")+"&InventoryId="+inventory.getId()+"&Locale="+language+axelorSettings.get("axelor.report.engine.datasource"));
		
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
  