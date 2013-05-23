package com.axelor.apps.base.service.formula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.FormulaGenerator;
import com.axelor.apps.base.service.formula.generator.condition.ConditionCalculationRuleTaxGenerator;
import com.axelor.apps.base.service.formula.generator.condition.ConditionFormulaGenerator;
import com.axelor.apps.base.service.formula.generator.condition.ConditionTaxGenerator;
import com.axelor.apps.base.service.formula.generator.formula.CalculationRuleTaxGenerator;
import com.axelor.apps.base.service.formula.generator.formula.DisplayPriceFormulaGenerator;
import com.axelor.apps.base.service.formula.generator.formula.PriceFormulaGenerator;
import com.axelor.apps.base.service.formula.generator.formula.QtyFormulaGenerator;
import com.axelor.apps.base.service.formula.generator.formula.VatFormulaGenerator;
import com.google.inject.Inject;

public class FormulaGeneratorService {
	
	private static final Logger LOG = LoggerFactory.getLogger(FormulaGeneratorService.class);

	@Inject
	private PriceFormulaGenerator priceFormulaGenerator; 
	@Inject
	private DisplayPriceFormulaGenerator displayPriceFormulaGenerator;
	@Inject
	private QtyFormulaGenerator qtyFormulaGenerator;
	@Inject
	private ConditionFormulaGenerator conditionFormulaGenerator;
	@Inject
	private CalculationRuleTaxGenerator calculationRuleTaxGenerator;
	@Inject
	private ConditionCalculationRuleTaxGenerator conditionCalculationRuleTaxGenerator;
	@Inject
	private ConditionTaxGenerator conditionTaxGenerator;
	@Inject
	private VatFormulaGenerator vatFormulaGenerator;

	public void generate(FormulaGenerator formulaGenerator){
		
		LOG.info("START FORMULA GENERATION");
		
		try {
			
			formulaGenerator.setFormula(priceFormulaGenerator.generate());
			formulaGenerator.setDisplayFormula(displayPriceFormulaGenerator.generate());
			formulaGenerator.setQtyFormula(qtyFormulaGenerator.generate());
			formulaGenerator.setConditionFormula(conditionFormulaGenerator.generate());
			formulaGenerator.setCalculationRuleTax(calculationRuleTaxGenerator.generate());
			formulaGenerator.setConditionCalculationRuleTax(conditionCalculationRuleTaxGenerator.generate());
			formulaGenerator.setConditionTax(conditionTaxGenerator.generate());
			formulaGenerator.setVatFormula(vatFormulaGenerator.generate());
			
			validate(formulaGenerator);
			
		} catch (Exception e) { LOG.error(e.getMessage()); }
		
		LOG.info("END FORMULA GENERATION");
		
	}

	public void validate(FormulaGenerator formulaGenerator){
		
		LOG.info("START FORMULA VALIDATION");
		
		formulaGenerator.setFormulaLog(priceFormulaGenerator.validate(formulaGenerator.getFormula()));
		formulaGenerator.setDisplayFormulaLog(displayPriceFormulaGenerator.validate(formulaGenerator.getDisplayFormula()));
		formulaGenerator.setQtyFormulaLog(qtyFormulaGenerator.validate(formulaGenerator.getQtyFormula()));
		formulaGenerator.setConditionFormulaLog(conditionFormulaGenerator.validate(formulaGenerator.getConditionFormula()));
		formulaGenerator.setCalculationRuleTaxLog(calculationRuleTaxGenerator.validate(formulaGenerator.getCalculationRuleTax()));
		formulaGenerator.setConditionCalculationRuleTaxLog(conditionCalculationRuleTaxGenerator.validate(formulaGenerator.getConditionCalculationRuleTax()));
		formulaGenerator.setConditionTaxLog(conditionTaxGenerator.validate(formulaGenerator.getConditionTax()));
		formulaGenerator.setVatFormulaLog(vatFormulaGenerator.validate(formulaGenerator.getVatFormula()));
		
		LOG.info("END FORMULA VALIDATION");
		
	}
	
}
