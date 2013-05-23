package com.axelor.apps.base.service.formula.generator.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.account.db.CalculationRule;
import com.axelor.apps.base.db.FormulaControl;
import com.axelor.apps.base.service.formula.generator.AbstractFormulaGenerator;

public class CalculationRuleTaxGenerator extends AbstractFormulaGenerator {
	
	@Inject
	public CalculationRuleTaxGenerator() { super(); }
	
	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{
				"com.axelor.apps.pricing.db.PricingList",
				"com.axelor.apps.pricing.db.PricingListVersion",
				"com.axelor.apps.account.db.InvoiceLine"
		});
		bind.put("klassName","CalculationRuleTax");
		bind.put("computeReturnType","BigDecimal");
		bind.put("computeParameters", new String[]{
				"PricingListVersion",
				"InvoiceLine",
				"BigDecimal"
		});
		bind.put("useTransco", true);
		
		Map<String,String> formulas = new HashMap<String, String>();
		Map<String, List<String>> exceptionFormulas = new HashMap<String, List<String>>();
		
		for (CalculationRule calculationRule : CalculationRule.all().fetch()){
			
			if (calculationRule.getCalculationRule() != null && !calculationRule.getCalculationRule().isEmpty()){
								
				formulas.put(calculationRule.getCode().toLowerCase(), decode(calculationRule.getCalculationRule()));
				List<String> exceptionConditions = new ArrayList<String>();
				exceptionConditions.add(String.format("if( invoiceLine?.amendment ) { msg += \"(Avenant: ${invoiceLine.amendment.name}, Version: ${pricingListVersion.fullName}, Assiette: ${bigDecimal}, Règle de calcul: %s)\"}", calculationRule.getName()));
				exceptionConditions.add(String.format("else { msg += \"(Version: ${pricingListVersion.fullName}, Assiette: ${bigDecimal}, Règle de calcul: %s)\"}", calculationRule.getName()));
				for (FormulaControl control : calculationRule.getFormulaControlList()){
					exceptionConditions.add(String.format("if( %s ) { msg += \"\\n%s\"}", control.getControl(), control.getMsg()));
				}
				exceptionFormulas.put(calculationRule.getCode().toLowerCase(), exceptionConditions);
			}
			
		}

		bind.put("formulas",formulas);
		bind.put("exceptionFormulas", exceptionFormulas);
		bind.put("computeReturnDefaultValue", null);
		bind.put("scale", 6);

		return bind;
		
	}

}
