package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j
import com.axelor.apps.supplychain.db.Inventory
import com.axelor.apps.supplychain.db.DeliveryOrder
import com.axelor.apps.supplychain.db.DeliveryOrderLine
import com.axelor.exception.service.TraceBackService
import com.axelor.apps.AxelorSettings
import com.axelor.meta.db.MetaUser
import com.axelor.auth.db.User
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.axelor.apps.tool.net.URLService

@Slf4j
class InventoryController {
	
	
	
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
	
	
}
