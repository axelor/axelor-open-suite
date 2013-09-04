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
