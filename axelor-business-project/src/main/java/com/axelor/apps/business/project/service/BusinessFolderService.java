package com.axelor.apps.business.project.service;

import com.axelor.apps.businessproject.db.BusinessFolder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BusinessFolderService {

	@Inject
	protected SaleOrderServiceSupplychainImpl saleOrderService;

	public SaleOrder createSaleOrder(BusinessFolder businessFolder) throws AxelorException{

		User user = AuthUtils.getUser();
		SaleOrder saleOrder = saleOrderService.createSaleOrder(user.getActiveCompany());
		saleOrder.setClientPartner(businessFolder.getCustomer());
		saleOrder = saleOrderService.getClientInformations(saleOrder);
		return saleOrder;
	}

	public SaleOrder createSaleOrderFromTemplate(BusinessFolder businessFolder,SaleOrder template) throws AxelorException{
		SaleOrder saleOrder = saleOrderService.createSaleOrder(template);
		if(businessFolder.getCustomer() != null){
			saleOrder.setClientPartner(businessFolder.getCustomer());
			saleOrder = saleOrderService.getClientInformations(saleOrder);
		}
		return saleOrder;
	}
}
