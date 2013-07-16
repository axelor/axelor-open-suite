package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j
import com.axelor.apps.supplychain.db.Inventory
import com.axelor.apps.supplychain.db.DeliveryOrder
import com.axelor.apps.supplychain.db.DeliveryOrderLine
import com.axelor.apps.supplychain.db.InventoryLine
import com.axelor.apps.supplychain.service.InventoryService
import com.axelor.exception.AxelorException
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.service.TraceBackService
import com.axelor.apps.AxelorSettings
import com.axelor.meta.db.MetaUser
import com.axelor.auth.db.User
import com.axelor.exception.db.IException
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional
import com.axelor.apps.tool.net.URLService

@Slf4j
class InventoryController {
	
	@Inject
	InventoryService inventoryService
	
	@Inject
	SequenceService sequenceService
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void showInventory(ActionRequest request, ActionResponse response) {

		Inventory inventory = request.context as Inventory
		int format = inventory.formatSelect

		StringBuilder url = new StringBuilder()
		AxelorSettings axelorSettings = AxelorSettings.get()
		
		MetaUser metaUser = MetaUser.findByUser(request.context.get("__user__"))
		url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/Inventory.rptdesign&__format="+((format == 1) ? "pdf":(format == 2) ? "xls":"pdf")+"&InventoryId=${inventory.id}&Locale=${metaUser.language}${axelorSettings.get('axelor.report.engine.datasource')}")
		
		log.debug("URL : {}", url)
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
			response.view = [
				"title": "Inventory ${inventory.name}",
				"resource": url,
				"viewType": "html"
			]
		}
		else {
			response.flash = urlNotExist
		}
	}
	
	def void importFile(ActionRequest request, ActionResponse response) {
		
		Inventory inventory = request.context as Inventory
		String filePath = inventory.importFilePath
		char separator = ','
		
		if (!inventory.name) {
			def ref = sequenceService.getSequence(IAdministration.INVENTORY, inventory.location.getCompany(),false)
			if (ref == null)
				throw new AxelorException("Aucune séquence configurée pour les inventaires pour la société "+inventory.location.getCompany().getName(),
								IException.CONFIGURATION_ERROR);
			inventory.setName(ref)
		}
		inventoryService.importFile(filePath, separator, inventory)
		response.flash = "File "+filePath+" successfully imported."
	}
	
	def void generateStockMove(ActionRequest request, ActionResponse response) {
		
		Inventory inventory = request.context as Inventory
		inventoryService.generateStockMove(inventory)
	}
}
