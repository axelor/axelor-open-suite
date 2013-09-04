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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.FormulaControl;
import com.axelor.apps.base.service.formula.generator.AbstractConditionGenerator;
import com.axelor.apps.base.service.formula.loader.Loader;

public class ConditionCalculationRuleTaxGenerator extends AbstractConditionGenerator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionCalculationRuleTaxGenerator.class);
	
	@Inject
	public ConditionCalculationRuleTaxGenerator() { super(); }
	
	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{
				"com.axelor.apps.account.db.InvoiceLine"
		});
		bind.put("klassName","ConditionCalculationRuleTax");
		bind.put("runnableParameters", new String[]{
				"InvoiceLine"
		});
		
		Map<String,String> conditions = new HashMap<String, String>();
		Map<String, List<String>> exceptionConditions = new HashMap<String, List<String>>();
		for (TaxLine taxLine : TaxLine.all().fetch()){
				
			if (taxLine.getCondition() != null && !taxLine.getCondition().isEmpty()){
				conditions.put(taxLine.getCode().toLowerCase(), taxLine.getCondition());
				List<String> exceptions = new ArrayList<String>();
				exceptions.add(String.format("if( invoiceLine.amendment ) { msg += \"(Contrat : ${invoiceLine.amendment.name}, Taxe : %s)\"}", taxLine.getTax().getName()));
				exceptions.add(String.format("else { msg += \"(Taxe : %s)\"}", taxLine.getTax().getName()));
				for (FormulaControl control : taxLine.getFormulaControlList()){
					exceptions.add(String.format("if( %s ) { msg += \"\\n%s\"}", control.getControl(), control.getMsg()));
				}
				exceptionConditions.put(taxLine.getCode().toLowerCase(), exceptions);
			}
			
		}

		bind.put("conditions",conditions);
		bind.put("exceptionConditions", exceptionConditions);

		return bind;
	}

	@Override
	public String validate(String code) {
		
		String res = "";
		
		try {
			Loader.loaderCondition1Lvl(code);
		} catch (Exception e) {
			res = e.getMessage();
			LOG.error(e.getMessage());
		} 

		return res;
	}

}
