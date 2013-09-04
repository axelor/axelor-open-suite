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
