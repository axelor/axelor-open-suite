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
