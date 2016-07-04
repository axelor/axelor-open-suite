package com.axelor.studio.test;

import groovy.lang.GroovyShell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.Test;

public class TestFormula {
	
//	@Test
	public void testFormula(){
		
		Pattern COUNT = Pattern.compile("\\$COUNT\\{(.[^\\}]*)");
		String formula = "$COUNT{inventoryLines, inventoryLines.product != null} + 1";
		
		Matcher matcher = COUNT.matcher(formula);
		
		while(matcher.find()){
			String replace = matcher.group();
			System.out.println("replace match: " + replace);
			String field = matcher.group(1);
			System.out.println("field match:" + field);
			
		}
	}
	
//	@Test
	public void  testReportFileName(){
		
		
		String fileName = "$customer.name$-$TODAY$";
		Pattern tag = Pattern.compile("\\$(.[^\\$]*)");
		
		Matcher matcher = tag.matcher(fileName);
		while(matcher.find()){
			String replace = matcher.group();
			System.out.println("replace match: " + replace);
			String field = matcher.group(1);
			System.out.println("field match:" + field);
		}
	}
	
	@Test
	public void testRegex(){
//		String expr = "sum(lines;$a+3:3sdf == 3)";
//		System.out.println(expr.matches("sum\\(([^;^:]+;[^;^:]+(:[^:^;]+)?)\\)"));
//				+ "[\\:[[$]?[a-zA-Z0-9]+[\\s\\.\\+\\-\\*\\%\\=]+]+]?)\\)"));
		
		String expr = "seq(23)";
		System.out.println(expr.matches("seq\\(([\\d]+(:[^:]+)?(:[^:]+)?)\\)"));
	}

//	@Test
	public void testGroovy() {
		try {
			new GroovyShell().parse("a.b(c)");
		} catch(MultipleCompilationErrorsException cfe) {
			cfe.printStackTrace();
		}
		
	}

}
