package com.axelor.apps.base.service.formula.generator.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.base.service.formula.generator.AbstractConditionGenerator;
import com.axelor.apps.account.db.DiscountEngine;

public class ConditionDiscountGenerator extends AbstractConditionGenerator {
	
	@Inject
	public ConditionDiscountGenerator() { super(); }
	
	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{ "com.axelor.apps.account.db.Invoice" });
		bind.put("klassName","ConditionDiscount");
		bind.put("runnableParameters", new String[]{ "Invoice" });
		
		Map<String,String> conditions = new HashMap<String, String>();
		Map<String, List<String>> exceptionConditions = new HashMap<String, List<String>>();
		
		for (DiscountEngine discountEngine : DiscountEngine.all().filter("self.activeOk = ?1", true).fetch()){
			if (discountEngine.getCondition() != null && !discountEngine.getCondition().isEmpty()){
				conditions.put(discountEngine.getCode().toLowerCase(), discountEngine.getCondition());
				List<String> exceptions = new ArrayList<String>();
				exceptions.add(String.format("msg += \"Problème sur la condition de la remise %s\"", discountEngine.getCode()));
				exceptionConditions.put(discountEngine.getCode().toLowerCase(), exceptions);
			}
		}

		bind.put("conditions",conditions);
		bind.put("exceptionConditions", exceptionConditions);

		return bind;
	}
	
}
