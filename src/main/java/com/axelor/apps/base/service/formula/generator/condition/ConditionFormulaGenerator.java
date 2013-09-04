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
