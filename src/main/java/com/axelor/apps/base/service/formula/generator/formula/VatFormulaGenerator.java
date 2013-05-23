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
