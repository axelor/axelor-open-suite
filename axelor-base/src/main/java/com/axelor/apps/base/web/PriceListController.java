package com.axelor.apps.base.web;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PriceListController {
	
	@Inject 
	private PriceListService priceListService;
	
	public void historizePriceList(ActionRequest request, ActionResponse response){
		PriceList priceList = request.getContext().asType(PriceList.class);
		priceList = priceListService.historizePriceList(priceList);
		response.setReload(true);
	}
}
