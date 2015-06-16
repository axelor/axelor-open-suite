package com.axelor.apps.business.project.web;

import com.axelor.apps.business.project.service.BusinessFolderService;
import com.axelor.apps.businessproject.db.BusinessFolder;
import com.axelor.apps.businessproject.db.repo.BusinessFolderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BusinessFolderController extends BusinessFolderRepository{
	
	@Inject
	protected BusinessFolderService businessFolderService;
	
	public void createSaleOrder(ActionRequest request, ActionResponse response) throws AxelorException{
		BusinessFolder businessFolder = this.find(Long.parseLong(request.getContext().get("_idFolder").toString()));
		SaleOrder saleOrder = null;
		if(businessFolder.getTemplate() == null){
			saleOrder = businessFolderService.createSaleOrder(businessFolder);
		}
		else{
			saleOrder = businessFolderService.createSaleOrderFromTemplate(businessFolder);
		}
		response.setValues(saleOrder);
	}
	
	
	public void generateViewSaleOrder(ActionRequest request, ActionResponse response){
		BusinessFolder businessFolder = request.getContext().asType(BusinessFolder.class);
		businessFolder = this.find(businessFolder.getId());
		response.setView(ActionView
	            .define("Sale Order")
	            .model(SaleOrder.class.getName())
	            .add("form", "sale-order-form-business-wizard")
	            .context("_idFolder", businessFolder.getId().toString())
	            .map());
	}
}
