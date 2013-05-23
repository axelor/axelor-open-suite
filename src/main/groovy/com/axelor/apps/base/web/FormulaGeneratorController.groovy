package com.axelor.apps.base.web

import org.joda.time.DateTime

import com.axelor.apps.base.db.FormulaGenerator
import com.axelor.apps.base.service.formula.FormulaGeneratorService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

class FormulaGeneratorController {
	
	@Inject
	private FormulaGeneratorService generator
		
	def void generate(ActionRequest request, ActionResponse response) {
		
		FormulaGenerator formulaGenerator = request.context as FormulaGenerator
		generator.generate(formulaGenerator)

		response.values = [
			"dateGeneration": new DateTime(),
			"formula": formulaGenerator.formula,
			"formulaLog": formulaGenerator.formulaLog,
			"displayFormula": formulaGenerator.displayFormula,
			"displayFormulaLog": formulaGenerator.displayFormulaLog,
			"qtyFormula": formulaGenerator.qtyFormula,
			"qtyFormulaLog": formulaGenerator.qtyFormulaLog,
			"conditionFormula": formulaGenerator.conditionFormula,
			"conditionFormulaLog": formulaGenerator.conditionFormulaLog,
			"calculationRuleTax": formulaGenerator.calculationRuleTax,
			"calculationRuleTaxLog": formulaGenerator.calculationRuleTaxLog,
			"conditionCalculationRuleTax": formulaGenerator.conditionCalculationRuleTax,
			"conditionCalculationRuleTaxLog": formulaGenerator.conditionCalculationRuleTaxLog,
			"conditionTax": formulaGenerator.conditionTax,
			"conditionTaxLog": formulaGenerator.conditionTaxLog,
			"vatFormula": formulaGenerator.vatFormula,
			"vatFormulaLog": formulaGenerator.vatFormulaLog
		]
		
	}
	
	def void validate(ActionRequest request, ActionResponse response) {
		
		FormulaGenerator formulaGenerator = request.context as FormulaGenerator
		generator.validate(formulaGenerator)

		response.values = [
			"formulaLog": formulaGenerator.formulaLog,
			"displayFormulaLog": formulaGenerator.displayFormulaLog,
			"qtyFormulaLog": formulaGenerator.qtyFormulaLog,
			"conditionFormulaLog": formulaGenerator.conditionFormulaLog,
			"calculationRuleTaxLog": formulaGenerator.calculationRuleTaxLog,
			"conditionCalculationRuleTaxLog": formulaGenerator.conditionCalculationRuleTaxLog,
			"conditionTaxLog": formulaGenerator.conditionTaxLog,
			"vatFormulaLog": formulaGenerator.vatFormulaLog
		]
		
	}
	
}
