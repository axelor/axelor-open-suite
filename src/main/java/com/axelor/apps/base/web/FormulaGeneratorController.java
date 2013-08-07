package com.axelor.apps.base.web;

import org.joda.time.DateTime;

import com.axelor.apps.base.db.FormulaGenerator;
import com.axelor.apps.base.service.formula.FormulaGeneratorService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class FormulaGeneratorController {
	
	@Inject
	private FormulaGeneratorService generator;
	
	public void generate(ActionRequest request, ActionResponse response) {
		
		FormulaGenerator formulaGenerator = request.getContext().asType(FormulaGenerator.class);
		generator.generate(formulaGenerator);

		response.setValue("dateGeneration", new DateTime());
		response.setValue("formula", formulaGenerator.getFormula());
		response.setValue("formulaLog", formulaGenerator.getFormulaLog());
		response.setValue("displayFormula", formulaGenerator.getDisplayFormula());
		response.setValue("displayFormulaLog", formulaGenerator.getDisplayFormulaLog());
		response.setValue("qtyFormula", formulaGenerator.getQtyFormula());
		response.setValue("qtyFormulaLog", formulaGenerator.getQtyFormulaLog());
		response.setValue("conditionFormula", formulaGenerator.getConditionFormula());
		response.setValue("conditionFormulaLog", formulaGenerator.getConditionFormulaLog());
		response.setValue("calculationRuleTax", formulaGenerator.getCalculationRuleTax());
		response.setValue("calculationRuleTaxLog", formulaGenerator.getCalculationRuleTaxLog());
		response.setValue("conditionCalculationRuleTax", formulaGenerator.getConditionCalculationRuleTax());
		response.setValue("conditionCalculationRuleTaxLog", formulaGenerator.getConditionCalculationRuleTaxLog());
		response.setValue("conditionTax", formulaGenerator.getConditionTax());
		response.setValue("conditionTaxLog", formulaGenerator.getConditionTaxLog());
		response.setValue("vatFormula", formulaGenerator.getVatFormula());
		response.setValue("vatFormulaLog", formulaGenerator.getVatFormulaLog());		
	}
	
	public void validate(ActionRequest request, ActionResponse response) {
		
		FormulaGenerator formulaGenerator = request.getContext().asType(FormulaGenerator.class);
		generator.validate(formulaGenerator);

		response.setValue("formulaLog", formulaGenerator.getFormulaLog());
		response.setValue("displayFormulaLog", formulaGenerator.getDisplayFormulaLog());
		response.setValue("qtyFormulaLog", formulaGenerator.getQtyFormulaLog());
		response.setValue("conditionFormulaLog", formulaGenerator.getConditionFormulaLog());
		response.setValue("calculationRuleTaxLog", formulaGenerator.getCalculationRuleTaxLog());
		response.setValue("conditionCalculationRuleTaxLog", formulaGenerator.getConditionCalculationRuleTaxLog());
		response.setValue("conditionTaxLog", formulaGenerator.getConditionTaxLog());
		response.setValue("vatFormulaLog", formulaGenerator.getVatFormulaLog());		
	}
}
