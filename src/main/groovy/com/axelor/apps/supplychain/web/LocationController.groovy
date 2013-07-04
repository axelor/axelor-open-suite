package com.axelor.apps.supplychain.web

import com.axelor.apps.supplychain.db.Location
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;


class LocationController {

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
}
