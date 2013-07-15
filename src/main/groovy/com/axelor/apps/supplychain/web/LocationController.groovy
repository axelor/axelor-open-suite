package com.axelor.apps.supplychain.web

import com.axelor.apps.base.db.ProductCategory
import com.axelor.apps.base.db.ProductFamily
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.Inventory
import com.axelor.apps.supplychain.db.Location
import com.axelor.apps.supplychain.db.LocationLine
import com.axelor.apps.supplychain.service.LocationService
import com.axelor.exception.AxelorException
import com.axelor.exception.service.TraceBackService
import com.axelor.exception.db.IException
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.rpc.Context
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.joda.time.LocalDate


class LocationController {
	
	@Inject
	LocationService locationService
	
	@Inject
	SequenceService sequenceService;

	void checkIsDefaultLocation(ActionRequest request, ActionResponse response){
		
		Location location = request.context as Location
		
		if(location && location.getIsDefaultLocation() && location.getCompany() && location.getTypeSelect()) {
			
			Location findLocation = Location.all().filter("company = ? and typeSelect = ? and isDefaultLocation = ?", location.getCompany(),location.getTypeSelect(),location.getIsDefaultLocation()).fetchOne();
			
			if(findLocation) {
				response.flash = "Il existe déjà un entrepot par défaut, veuillez d'abord désactiver l'entrepot ${findLocation.name}"
				response.values = ["isDefaultLocation" : false]
			}
		}
	}
	
	void createInventory(ActionRequest request, ActionResponse response) {
		Context context = request.context
		LocalDate date = new LocalDate(context.inventoryDate)
		String description = request.context.get("description")
		int locationId = context._location.id
		Location location = Location.find(locationId)
		boolean excludeOutOfStock = context.excludeOutOfStock
		boolean includeObsolete = context.includeObsolete
		
		
		if (location == null)
			throw new AxelorException("Emplacement invalide",
							IException.CONFIGURATION_ERROR);
		
		ProductFamily productFamily = null
		if (context.productFamily)
			productFamily = ProductFamily.find(context.productFamily.id)
			
		ProductCategory productCategory = null
		if (context.productCategory)
			productCategory = ProductCategory.find(context.productCategory.id)
			
		def ref = sequenceService.getSequence(IAdministration.INVENTORY, location.getCompany(),false)
		if (ref == null)
			throw new AxelorException("Aucune séquence configurée pour les inventaires pour la société "+location.getCompany().getName(),
							IException.CONFIGURATION_ERROR);
		
		Inventory inventory = locationService.createInventory(ref, date, description, location, excludeOutOfStock,
										includeObsolete, productFamily, productCategory)
		
		response.values = [
			"inventoryId" : inventory.id
		]
	}
}
