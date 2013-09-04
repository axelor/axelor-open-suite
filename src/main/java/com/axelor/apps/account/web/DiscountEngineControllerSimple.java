/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.DiscountEngineLine;
import com.axelor.apps.base.service.formula.call.ConditionDiscountCall;
import com.axelor.apps.base.service.formula.call.ConditionDiscountLineCall;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DiscountEngineControllerSimple {

	public void resetDiscountEngine(ActionRequest request, ActionResponse response){
		
		try { 
			ConditionDiscountCall.reset();
			ConditionDiscountLineCall.reset();
		}
	    catch(Exception e){ TraceBackService.trace(response, e); }
	}
	
	/**
	 * Construction dynamique de la condition final
	 * @param request
	 * @param response
	 */
	public void finalCondition(ActionRequest request, ActionResponse response) {

		DiscountEngineLine discountEngineLine = request.getContext().asType(DiscountEngineLine.class);
		
		String finalCondition = "";
		
		if (discountEngineLine.getSingleApplicationOk()) { 
			finalCondition = addCondition(finalCondition, "!discountEngineLine.contractLineSet?.contains(invoiceLine.amendment?.contractLine)"); 
		}
		
		if (discountEngineLine.getAllCategoriesOk() && discountEngineLine.getProductCategorySet() != null){
			finalCondition = addCondition(finalCondition, "discountEngineLine.productCategorySet.contains(invoiceLine.product?.productCategory)");
		}
		else if (discountEngineLine.getProductSet() != null){
			finalCondition = addCondition(finalCondition, "discountEngineLine.productSet.contains(invoiceLine.product)");
		}
				
		if (discountEngineLine.getReferenceDateSelect() != null){
			
			switch (discountEngineLine.getReferenceDateSelect()) {
			case 1:
				finalCondition = addCondition(finalCondition, "DateTool.isProrata(discountEngineLine.fromDate, discountEngineLine.toDate, invoiceLine.fromDate, invoiceLine.toDate)");
				break;
			case 2:
				finalCondition = addCondition(finalCondition, "DateTool.isBetween(discountEngineLine.fromDate, discountEngineLine.toDate, invoiceLine.amendment?.contractLine?.realStartDate)");
				break;
			case 3:
				finalCondition = addCondition(finalCondition, "DateTool.isBetween(discountEngineLine.fromDate, discountEngineLine.toDate, invoiceLine.amendment?.contractLine?.realEndDate)");
				break;
			case 4:
				finalCondition = addCondition(finalCondition, "DateTool.isBetween(discountEngineLine.fromDate, discountEngineLine.toDate, invoiceLine.amendment?.contractLine?.startDate)");
				break;
			case 5:
				finalCondition = addCondition(finalCondition, "DateTool.isBetween(discountEngineLine.fromDate, discountEngineLine.toDate, invoiceLine.amendment?.contractLine?.endDate)");
				break;
			case 6:
				finalCondition = addCondition(finalCondition, "DateTool.isBetween(discountEngineLine.fromDate, discountEngineLine.toDate, invoiceLine.serviceAndFee?.startDate)");
				break;
			case 7:
				finalCondition = addCondition(finalCondition, "DateTool.isBetween(discountEngineLine.fromDate, discountEngineLine.toDate, invoiceLine.serviceAndFee?.endDate)");
				break;

			default:
				break;
			}
		}
		
		if (discountEngineLine.getCondition() != null) { 
			finalCondition = addCondition(finalCondition, "("+discountEngineLine.getCondition()+")");
		}
		response.setValue("finalCondition", finalCondition);		
	 }
	
	public String addCondition(String initialCondition, String newCondition){
		 
		 if (initialCondition != null) {
			 return initialCondition+" && "+newCondition;
		 }
		 else {
			 return newCondition;
		 }
	 }
}
