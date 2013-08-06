package com.axelor.apps.supplychain.web;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.service.LocationService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class LocationController {

	@Inject
	LocationService locationService;
	
	@Inject
	SequenceService sequenceService;
	
	public void checkIsDefaultLocation(ActionRequest request, ActionResponse response){
		
		Location location = request.getContext().asType(Location.class);
		
		if(location != null && location.getIsDefaultLocation() && location.getCompany() != null && location.getTypeSelect() != null) {
			
			Location findLocation = Location.all().filter("company = ? and typeSelect = ? and isDefaultLocation = ?", location.getCompany(),location.getTypeSelect(),location.getIsDefaultLocation()).fetchOne();
			
			if(findLocation != null) {
				response.setFlash("Il existe déjà un entrepot par défaut, veuillez d'abord désactiver l'entrepot "+findLocation.getName());
				response.setValue("isDefaultLocation", false);
			}
		}
	}
	
	public void createInventory(ActionRequest request, ActionResponse response) throws Exception {
		Context context = request.getContext();
		LocalDate date = new LocalDate(context.get("inventoryDate"));
		String description = (String) context.get("description");
		Location contextLocation = (Location) context.get("_location");
		Location location = null;
		if(contextLocation != null) {
			location = Location.find(contextLocation.getId());
		}
		boolean excludeOutOfStock = (Boolean) context.get("excludeOutOfStock");
		boolean includeObsolete = (Boolean) context.get("includeObsolete");
		
		if (location == null)
			throw new AxelorException("Emplacement invalide",
							IException.CONFIGURATION_ERROR);
		
		ProductFamily productFamily = null;
		ProductFamily contextProductFamily = (ProductFamily) context.get("productFamily");
		if (contextProductFamily != null) {
			productFamily = ProductFamily.find(contextProductFamily.getId());
		}
		
		ProductCategory productCategory = null;
		ProductCategory contextProductCategory = (ProductCategory) context.get("productCategory");
		if (contextProductCategory != null) {
			productCategory = ProductCategory.find(contextProductCategory.getId());
		}	
		String ref = sequenceService.getSequence(IAdministration.INVENTORY, location.getCompany(),false);
		if (ref == null)
			throw new AxelorException("Aucune séquence configurée pour les inventaires pour la société "+location.getCompany().getName(),
							IException.CONFIGURATION_ERROR);
		
		Inventory inventory = locationService.createInventory(ref, date, description, location, excludeOutOfStock,
										includeObsolete, productFamily, productCategory);
		response.setValue("inventoryId", inventory.getId());
	}
}
