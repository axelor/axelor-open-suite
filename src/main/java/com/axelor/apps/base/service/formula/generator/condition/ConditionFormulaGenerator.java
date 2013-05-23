package com.axelor.apps.base.service.formula.generator.condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.base.service.formula.generator.AbstractConditionGenerator;

public class ConditionFormulaGenerator extends AbstractConditionGenerator {
	
	@Inject
	public ConditionFormulaGenerator() { super(); }
	
	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{
				"com.axelor.apps.contract.db.Amendment",
				"com.axelor.apps.event.db.ConsumptionEventLine"
		});
		bind.put("klassName","ConditionFormula");
		bind.put("runnableParameters", new String[]{
				"Amendment",
				"ConsumptionEventLine"
		});
		
		Map<String,String> conditions = new HashMap<String, String>();
		Map<String, List<String>> exceptionConditions = new HashMap<String, List<String>>();
//		for (PricingStructure pricingStructure : PricingStructure.all().fetch()){
//			
//			for (PricingStructureLine pricingStructureLine : pricingStructure.getStructureLineList()){
//				
//				if (pricingStructureLine.getCondition() != null && !pricingStructureLine.getCondition().isEmpty()){
//					conditions.put(pricingStructureLine.getCode().toLowerCase(), pricingStructureLine.getCondition());
//					List<String> exceptions = new ArrayList<String>();
//					exceptions.add(String.format("if( consumptionEventLine ) { msg += \"(Contrat : ${amendment.contractLine.contractLineId}, Macro-évènement : ${consumptionEventLine.consumptionEvent.name}, Structure tarifaire : %s)\"}", pricingStructureLine.getName()));
//					exceptions.add(String.format("else { msg += \"(Contrat : ${amendment.contractLine.contractLineId}, Structure tarifaire : %s)\"}", pricingStructureLine.getName()));
//					for (FormulaControl control : pricingStructure.getFormulaControlList()){
//						exceptions.add(String.format("if( %s ) { msg += \"\\n%s\"}", control.getControl(), control.getMsg()));
//					}
//					exceptionConditions.put(pricingStructureLine.getCode().toLowerCase(), exceptions);
//				}
//				
//				
//			}
//			
//		}

		bind.put("conditions",conditions);
		bind.put("exceptionConditions", exceptionConditions);

		return bind;
	}
	
}
