package com.axelor.apps.base.service.formula.generator.formula;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.base.service.formula.generator.AbstractFormulaGenerator;

public class PriceFormulaGenerator extends AbstractFormulaGenerator {
	
	@Inject
	public PriceFormulaGenerator() { super(); }

	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{
				"com.axelor.apps.contract.db.*",
				"com.axelor.apps.event.db.*",
				"com.axelor.apps.pricing.db.*"
		});
		bind.put("klassName","Formula");
		bind.put("computeReturnType","BigDecimal");
		bind.put("computeParameters", new String[]{
				"PricingListVersion",
				"Amendment",
				"Parameter",
				"ConsumptionEventLine"
		});
		bind.put("useTransco", true);
		
		Map<String,String> formulas = new HashMap<String, String>();
		Map<String, List<String>> exceptionFormulas = new HashMap<String, List<String>>();
		
//		for (Constituent constituent : Constituent.all().fetch()){
//			
//			if (constituent.getFormula() != null && !constituent.getFormula().isEmpty()){
//								
//				formulas.put(constituent.getCode().toLowerCase(), decode(constituent.getFormula()));
//				List<String> exceptionConditions = new ArrayList<String>();
//				exceptionConditions.add(String.format("if( consumptionEventLine ) { msg += \"(Contrat : ${amendment.contractLine.contractLineId}, Macro-évènement : ${consumptionEventLine.consumptionEvent.name}, Composante : %s)\"}",constituent.getName()));
//				exceptionConditions.add(String.format("else { msg += \"(Contrat : ${amendment.contractLine.contractLineId}, Composante : %s)\"}",constituent.getName()));
//				for (FormulaControl control : constituent.getFormulaControlList()){
//					exceptionConditions.add(String.format("if( %s ) { msg += \"\\n%s\"}", control.getControl(), control.getMsg()));
//				}
//				exceptionFormulas.put(constituent.getCode().toLowerCase(), exceptionConditions);
//			}
//			
//		}

		bind.put("formulas",formulas);
		bind.put("exceptionFormulas", exceptionFormulas);
		bind.put("computeReturnDefaultValue", null);
		bind.put("scale", 6);

		return bind;
		
	}
	
}
