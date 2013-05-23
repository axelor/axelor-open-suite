package com.axelor.apps.base.service.formula.generator.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.db.FormulaControl;
import com.axelor.apps.base.service.formula.generator.AbstractConditionGenerator;

public class ConditionTaxGenerator extends AbstractConditionGenerator {
	
	@Inject
	public ConditionTaxGenerator() { super(); }
	
	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{
				"com.axelor.apps.account.db.Invoice"
		});
		bind.put("klassName","ConditionTax");
		bind.put("runnableParameters", new String[]{
				"Invoice"
		});
		
		Map<String,String> conditions = new HashMap<String, String>();
		Map<String, List<String>> exceptionConditions = new HashMap<String, List<String>>();
		for (Tax tax : Tax.all().fetch()){
			
			if (tax.getCondition() != null && !tax.getCondition().isEmpty()){
				conditions.put(tax.getCode().toLowerCase(), tax.getCondition());
				List<String> exceptions = new ArrayList<String>();
				exceptions.add(String.format("if( invoice.contractLine ) { msg += \"(Contrat : ${invoice.contractLine.contractLineId}, Taxe : %s)\"}", tax.getName()));
				exceptions.add(String.format("else { msg += \"(Taxe : %s)\"}", tax.getName()));
				for (FormulaControl control : tax.getFormulaControlList()){
					exceptions.add(String.format("if( %s ) { msg += \"\\n%s\"}", control.getControl(), control.getMsg()));
				}
				exceptionConditions.put(tax.getCode().toLowerCase(), exceptions);
			}
			
		}

		bind.put("conditions",conditions);
		bind.put("exceptionConditions", exceptionConditions);

		return bind;
	}

}
