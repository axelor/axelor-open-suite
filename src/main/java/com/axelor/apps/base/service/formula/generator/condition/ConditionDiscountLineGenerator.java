package com.axelor.apps.base.service.formula.generator.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.base.service.formula.generator.AbstractConditionGenerator;
import com.axelor.apps.account.db.DiscountEngineLine;

public class ConditionDiscountLineGenerator extends AbstractConditionGenerator {
	
	@Inject
	public ConditionDiscountLineGenerator() { super(); }
	
	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{ 
				"com.axelor.apps.account.db.InvoiceLine",
				"com.axelor.apps.account.db.DiscountEngineLine",
				"com.axelor.apps.tool.date.DateTool"
		});
		bind.put("klassName","ConditionDiscountLine");
		bind.put("runnableParameters", new String[]{ 
				"DiscountEngineLine",
				"InvoiceLine"
		});
		
		Map<String,String> conditions = new HashMap<String, String>();
		Map<String, List<String>> exceptionConditions = new HashMap<String, List<String>>();

		for (DiscountEngineLine discountEngineLine : DiscountEngineLine.all().fetch()){
			if ( discountEngineLine.getCode() != null && !discountEngineLine.getCode().isEmpty() && discountEngineLine.getFinalCondition() != null && !discountEngineLine.getFinalCondition().isEmpty()){
				conditions.put(discountEngineLine.getCode().toLowerCase(), discountEngineLine.getFinalCondition());
				List<String> exceptions = new ArrayList<String>();
				exceptions.add(String.format("msg += \"Problème sur la condition de la remise %s\"", discountEngineLine.getCode()));
				exceptionConditions.put(discountEngineLine.getCode().toLowerCase(), exceptions);
			}
		}

		bind.put("conditions",conditions);
		bind.put("exceptionConditions", exceptionConditions);

		return bind;
	}
	
}
