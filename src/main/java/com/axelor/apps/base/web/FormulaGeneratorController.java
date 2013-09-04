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
