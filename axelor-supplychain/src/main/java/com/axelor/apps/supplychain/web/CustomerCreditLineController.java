/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.web;

import java.util.Map;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.db.CustomerCreditLine;
import com.axelor.apps.supplychain.service.CustomerCreditLineService;
import com.axelor.exception.AxelorException;
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
	
	public void generateLines(ActionRequest request, ActionResponse response) throws AxelorException  {
		Partner partner = request.getContext().asType(Partner.class);
		partner.setCustomerCreditLineList(customerCreditLineService.generateLines(partner).getCustomerCreditLineList());
		response.setValues(partner);
	}
	
	public void updateLinesFromPartner(ActionRequest request, ActionResponse response) throws AxelorException  {
		Partner partnerView = request.getContext().asType(Partner.class);
		if(partnerView.getId() != null && partnerView.getId() > 0){
			Partner partner = partnerRepo.find(partnerView.getId());
			customerCreditLineService.updateLines(partner);
			response.setValue("customerCreditLineList", partner.getCustomerCreditLineList());
		}
	}
	
	public void updateLinesFromSaleOrder(ActionRequest request, ActionResponse response) throws AxelorException  {
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		if(saleOrder.getClientPartner() != null){
			Partner partner = saleOrder.getClientPartner();
			Map<String,Object> map = customerCreditLineService.updateLinesFromOrder(partnerRepo.find(partner.getId()),saleOrder);
			response.setValues(map);
		}
	}
	
}
