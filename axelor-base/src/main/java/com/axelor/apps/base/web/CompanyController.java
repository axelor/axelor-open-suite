package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CompanyController {

	/**
	 * @see com.axelor.apps.base.service.CompanyService#checkMultiBanks
	 * 
	 * @param request
	 * @param response
	 */
	public void checkMultiBanks(ActionRequest request, ActionResponse response) {
		Company company = request.getContext().asType(Company.class);
		Beans.get(CompanyService.class).checkMultiBanks(company);
	}

}
