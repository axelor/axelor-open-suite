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
package com.axelor.apps.base.service.formula.generator.formula;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.axelor.apps.base.service.formula.generator.AbstractFormulaGenerator;

public class QtyFormulaGenerator extends AbstractFormulaGenerator {
	
	@Inject
	public QtyFormulaGenerator() { super(); }

	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{
				"com.axelor.apps.contract.db.*",
				"com.axelor.apps.event.db.*",
				"com.axelor.apps.pricing.db.*"
		});
		bind.put("klassName","QtyFormula");
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
//			if (constituent.getQtyFormula() != null && !constituent.getQtyFormula().isEmpty()){
//								
//				formulas.put(constituent.getCode().toLowerCase(), decode(constituent.getQtyFormula()));
//				List<String> exceptionConditions = new ArrayList<String>();
//				exceptionConditions.add(String.format("if( consumptionEventLine ) { msg += \"(Contrat : ${amendment.contractLine.contractLineId}, Macro-évènement : ${consumptionEventLine.consumptionEvent.name}, Composante : %s)\"}", constituent.getName()));
//				exceptionConditions.add(String.format("else { msg += \"(Contrat : ${amendment.contractLine.contractLineId}, Composante : %s)\"}", constituent.getName()));
//				for (FormulaControl control : constituent.getFormulaControlList()){
//					exceptionConditions.add(String.format("if( %s ) { msg += \"\\n%s\"}", control.getControl(), control.getMsg()));
//				}
//				exceptionFormulas.put(constituent.getCode().toLowerCase(), exceptionConditions);
//			}
//			
//		}

		bind.put("formulas",formulas);
		bind.put("exceptionFormulas", exceptionFormulas);
		bind.put("computeReturnDefaultValue", "BigDecimal.ONE");
		bind.put("scale", 2);

		return bind;
		
	}
	
}
