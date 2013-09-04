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

import com.axelor.apps.account.db.VatManagement;
import com.axelor.apps.account.db.VatManagementLine;
import com.axelor.apps.base.db.FormulaControl;
import com.axelor.apps.base.service.formula.generator.AbstractFormulaGenerator;

public class VatFormulaGenerator extends AbstractFormulaGenerator {

	@Inject
	public VatFormulaGenerator() { super(); }

	@Override
	protected Map<String, Object> bind() {
	
		Map<String, Object> bind = new HashMap<String, Object>();
		bind.put("klassImport",new String[]{
				"com.axelor.apps.contract.db.Amendment",
				"com.axelor.apps.sale.db.Vat",
				"com.axelor.apps.sale.db.VatManagementLine"
		});
		bind.put("klassName","VatFormula");
		bind.put("computeReturnType","Vat");
		bind.put("computeParameters", new String[]{
				"Amendment",
				"List<VatManagementLine>"
		});
		bind.put("useTransco", false);
		
		Map<String,String> formulas = new HashMap<String, String>();
		Map<String, List<String>> exceptionFormulas = new HashMap<String, List<String>>();
		
		for (VatManagement vatManagement : VatManagement.all().fetch()){
			
			if (vatManagement.getVatManagementLineList() != null){
				formulas.put(vatManagement.getCode().toLowerCase(), decode(vatManagement));
				List<String> exceptionConditions = new ArrayList<String>();
				exceptionConditions.add(String.format("msg += \"(Contrat : ${amendment.contractLine.contractLineId}, Configuration de TVA : %s)\"",vatManagement.getCode()));
				for (FormulaControl control : vatManagement.getFormulaControlList()){
					exceptionConditions.add(String.format("if( %s ) { msg += \"\\n%s\"}", control.getControl(), control.getMsg()));
				}
				exceptionFormulas.put(vatManagement.getCode().toLowerCase(), exceptionConditions);
			}
			
		}

		bind.put("formulas",formulas);
		bind.put("exceptionFormulas", exceptionFormulas);
		bind.put("computeReturnDefaultValue", null);
		bind.put("scale", null);

		return bind;
		
	}
	
	protected String decode(VatManagement vatManagement){
		
		String transformFormula = ""; int i = 0 ;
		for (VatManagementLine vatManagementLine : vatManagement.getVatManagementLineList()){
			if (vatManagementLine.equals(vatManagement.getVatManagementLineList().get(0))){
				transformFormula += String.format("if (%s) { res = listVatManagementLine.get(%d).vat }\n\t\t\t", vatManagementLine.getCondition(), i);
			}
			else {
				transformFormula += String.format("else if (%s) { res = listVatManagementLine.get(%d).vat }\n\t\t\t", vatManagementLine.getCondition(), i);
			}
			i++;
		}
		transformFormula += String.format("else { throw new AxelorException(\"Aucune TVA trouvé pour le contrat ${amendment.contractLine.contractLineId} pour la configuration de TVA %s\", IException.CONFIGURATION_ERROR) }\n", vatManagement.getCode());
		
		return transformFormula;
	}

}
