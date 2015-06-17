package com.axelor.apps.business.project.service;

import com.axelor.apps.businessproject.db.BusinessFolder;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleConfig;
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
		SaleConfig saleConfig = user.getActiveCompany().getSaleConfig();
		if(saleConfig != null && saleConfig.getSaleOrderInvoicingTypeSelect() != 0){
			saleOrder.setInvoicingTypeSelect(saleConfig.getSaleOrderInvoicingTypeSelect());
		}
		else{
			saleOrder.setInvoicingTypeSelect(ISaleOrder.INVOICING_TYPE_FREE);
		}
		return saleOrder;
	}
	
	public SaleOrder createSaleOrderFromTemplate(BusinessFolder businessFolder) throws AxelorException{
		SaleOrder saleOrder = saleOrderService.createSaleOrder(businessFolder.getTemplateSaleOrder());
		if(businessFolder.getCustomer() != null){
			saleOrder.setClientPartner(businessFolder.getCustomer());
			saleOrder = saleOrderService.getClientInformations(saleOrder);
		}
		return saleOrder;
	}
}
