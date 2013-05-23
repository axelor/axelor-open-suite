package com.axelor.apps.base.service.formula.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axelor.apps.account.db.MatrixStructure;

public abstract class AbstractFormulaGenerator extends AbstractGenerator {
	
	protected AbstractFormulaGenerator(){
		
		this.template = "templates/formula.template";
		
	}
	
	/**
	 * Transformation de la formule de prix unitaire d'une composante en une fonction
	 * groovy.
	 *
	 * @param formula
	 *
	 * @return
	 */
	protected String decode(String formula){

		final List<String> listString = new ArrayList<String>();

		String transformFormula = formula;

		Pattern pattern = Pattern.compile("\\$((\\w+)\\.(\\w+))");
		Matcher matcher = pattern.matcher(formula);

		while (matcher.find()){
			
			String element = matcher.group(0);
			
			MatrixStructure matrix = MatrixStructure.all().filter("name = ?1", matcher.group(2)).fetchOne();
			if (matrix != null){

				int formulaCode = 0;
				String parameterCode = "";
				String valueCode = matcher.group(3);
				
				if (matrix.getValueCode1() != null && matrix.getValueCode1().equals(valueCode)) { formulaCode = 1; parameterCode = matrix.getValueName1(); }
				else if (matrix.getValueCode2() != null && matrix.getValueCode2().equals(valueCode)) { formulaCode = 2; parameterCode = matrix.getValueName2(); }
				else if (matrix.getValueCode3() != null && matrix.getValueCode3().equals(valueCode)) { formulaCode = 3; parameterCode = matrix.getValueName3(); }
				else if (matrix.getValueCode4() != null && matrix.getValueCode4().equals(valueCode)) { formulaCode = 4; parameterCode = matrix.getValueName4(); }
				else if (matrix.getValueCode5() != null && matrix.getValueCode5().equals(valueCode)) { formulaCode = 5; parameterCode = matrix.getValueName5(); }
				
				if (formulaCode != 0 && !listString.contains(element)){
					transformFormula = transformFormula.replace(element,String.format("fs.transco(\"%s\",\"%s\",\"%s\")", matcher.group(1), parameterCode, createQuery(matrix, formulaCode)));
					listString.add(element);
				}
			}
		}
	
		pattern = Pattern.compile("\\$res");
		matcher = pattern.matcher(formula);
		
		if (matcher.find()) { transformFormula = transformFormula.replace(matcher.group(0),"res"); }
		else { transformFormula = String.format("res = %s", transformFormula); }
	
		return transformFormula;

	}
	
	protected String createQuery(MatrixStructure matrix, int exitValue){
		
		String condition = this.createCondition(matrix, "pricing_list_version = ${pricingListVersion.id}");
		
		return String.format("SELECT val%d FROM pricing_pricing_list_line WHERE %s", exitValue, condition);
		
		
	}
	
	protected String createCondition(MatrixStructure matrix, String condition){
		
		if (matrix.getSourceParam1() != null && !matrix.getSourceParam1().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam1(), 1); }
		
		if (matrix.getSourceParam2() != null && !matrix.getSourceParam2().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam2(), 2); }
		
		if (matrix.getSourceParam3() != null && !matrix.getSourceParam3().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam3(), 3); }
		
		if (matrix.getSourceParam4() != null && !matrix.getSourceParam4().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam4(), 4); }
		
		if (matrix.getSourceParam5() != null && !matrix.getSourceParam5().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam5(), 5); }
	
		if (matrix.getSourceParam6() != null && !matrix.getSourceParam6().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam6(), 6); }
	
		if (matrix.getSourceParam7() != null && !matrix.getSourceParam7().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam7(), 7); }
	
		if (matrix.getSourceParam8() != null && !matrix.getSourceParam8().isEmpty()) { condition = this.addCondition(condition, matrix.getSourceParam8(), 8); }
	
		if (matrix.getSourceParam9() != null && !matrix.getSourceParam9().isEmpty()) { condition = this.addBetweenCondition(condition, matrix.getSourceParam9(), matrix.getParam9Incl(), matrix.getParam10Incl()); }
				
		return condition;
	}
	
	protected String addCondition(String condition, String element, int id){
		
		if (condition != null) { condition += " AND "; }
		
		if (element.toLowerCase().equals("null")) { condition += String.format("parameter%d IS NULL", id); }
		else { condition += String.format("parameter%d = ${%s.id}", id, element); }
		
		return condition;
		
	}
	
	protected String addBetweenCondition(String condition, String element, boolean leftInc, boolean rightInc){
		
		if (!condition.isEmpty()) {	condition += " AND "; }
		
		String inf = "<"; if (leftInc) { inf += "="; }
		String sup = ">"; if (rightInc) { sup += "="; }
		
		condition += String.format("parameter9 %s ${%s} AND parameter10 %s ${%s}", inf, element, sup, element);
		
		return condition;
		
	}
	
}
