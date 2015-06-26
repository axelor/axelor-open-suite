package com.axelor.apps.business.project.web;

import java.util.HashMap;

import com.axelor.apps.business.project.exception.IExceptionMessage;
import com.axelor.apps.business.project.service.BusinessFolderService;
import com.axelor.apps.businessproject.db.BusinessFolder;
import com.axelor.apps.businessproject.db.repo.BusinessFolderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BusinessFolderController extends BusinessFolderRepository{

	@Inject
	protected BusinessFolderService businessFolderService;

	public void createSaleOrder(ActionRequest request, ActionResponse response) throws AxelorException{
		BusinessFolder businessFolder = this.find(Long.parseLong(request.getContext().get("_idFolder").toString()));
		SaleOrder template = null;
		if(request.getContext().get("_idTemplate") != null){
			template = Beans.get(SaleOrderRepository.class).find(Long.parseLong(request.getContext().get("_idTemplate").toString()));
		}

		SaleOrder saleOrder = null;
		if(template == null){
			saleOrder = businessFolderService.createSaleOrder(businessFolder);
		}
		else{
			saleOrder = businessFolderService.createSaleOrderFromTemplate(businessFolder,template);
		}
		response.setValues(saleOrder);
	}

	public void generateViewSaleOrder(ActionRequest request, ActionResponse response){
		BusinessFolder businessFolder = request.getContext().asType(BusinessFolder.class);
		response.setView(ActionView
	            .define("Sale Order")
	            .model(SaleOrder.class.getName())
	            .add("form", "sale-order-form-business-wizard")
	            .context("_idFolder", businessFolder.getId().toString())
	            .map());
	}

	public void generateViewSaleOrderFromTemplate(ActionRequest request, ActionResponse response) throws AxelorException{
		BusinessFolder businessFolder = request.getContext().asType(BusinessFolder.class);
		HashMap map = (HashMap) request.getContext().get("template");
		SaleOrder template = null;
		if(map != null && map.get("id") != null){
			template = Beans.get(SaleOrderRepository.class).find(Long.parseLong(map.get("id").toString()));
		}

		if(template == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.FOLDER_TEMPLATE)), IException.CONFIGURATION_ERROR);
		}
		response.setView(ActionView
	            .define("Sale Order")
	            .model(SaleOrder.class.getName())
	            .add("form", "sale-order-form-business-wizard")
	            .context("_idFolder", businessFolder.getId().toString())
	            .context("_idTemplate",template.getId().toString())
	            .map());
	}

}
