package com.axelor.apps.supplychain.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.service.InventoryService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InventoryController {

	@Inject
	InventoryService inventoryService;
	
	@Inject
	SequenceService sequenceService;
	
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
		
		MetaUser metaUser = MetaUser.findByUser((User) request.getContext().get("__user__"));
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Inventory.rptdesign&__format="+((format == 1) ? "pdf":(format == 2) ? "xls":"pdf")+"&InventoryId="+inventory.getId()+"&Locale="+metaUser.getLanguage()+axelorSettings.get("axelor.report.engine.datasource"));
		
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
		
		if(inventory.getInventorySeq() == null || inventory.getInventorySeq().isEmpty()) {
			String ref = sequenceService.getSequence(IAdministration.INVENTORY, inventory.getLocation().getCompany(),false);
			if (ref == null)
				throw new AxelorException("Aucune séquence configurée pour les inventaires pour la société "+inventory.getLocation().getCompany().getName(),
								IException.CONFIGURATION_ERROR);
			inventory.setInventorySeq(ref);
		}
		inventoryService.importFile(filePath, separator, inventory);
		response.setFlash("File "+filePath+" successfully imported.");
	}
	
	public void generateStockMove(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Inventory inventory = request.getContext().asType(Inventory.class);
		inventoryService.generateStockMove(inventory);
	}
}
