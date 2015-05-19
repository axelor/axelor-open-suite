package com.axelor.apps.supplychain.web;

import java.util.Map;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.db.CustomerCreditLine;
import com.axelor.apps.supplychain.service.CustomerCreditLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class CustomerCreditLineController {
	
	@Inject
	protected CustomerCreditLineService customerCreditLineService;
	
	@Inject
	protected PartnerRepository partnerRepo;
	
	public void computeUsedCredit(ActionRequest request, ActionResponse response)  {
		CustomerCreditLine customerCreditLine = request.getContext().asType(CustomerCreditLine.class);
		customerCreditLine = customerCreditLineService.computeUsedCredit(customerCreditLine);
		response.setValues(customerCreditLine);
	}
	
	public void generateLines(ActionRequest request, ActionResponse response)  {
		Partner partner = request.getContext().asType(Partner.class);
		partner = customerCreditLineService.generateLines(partnerRepo.find(partner.getId()));
		response.setValues(partner);
	}
	
	public void updateLinesFromPartner(ActionRequest request, ActionResponse response)  {
		Partner partnerView = request.getContext().asType(Partner.class);
		Partner partner = partnerRepo.find(partnerView.getId());
		Map<String,Object> map = customerCreditLineService.updateLines(partner);
		response.setValues(map);
	}
	
	public void updateLinesFromSaleOrder(ActionRequest request, ActionResponse response)  {
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		if(saleOrder.getClientPartner() != null){
			Partner partner = saleOrder.getClientPartner();
			Map<String,Object> map = customerCreditLineService.updateLinesFromOrder(partnerRepo.find(partner.getId()),saleOrder);
			response.setValues(map);
		}
	}
	
}
