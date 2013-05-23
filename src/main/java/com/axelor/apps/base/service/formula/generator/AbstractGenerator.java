package com.axelor.apps.base.service.formula.generator;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import com.axelor.apps.base.service.formula.loader.Loader;

public abstract class AbstractGenerator {
	
	protected String template;

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}
	
	public String generate() throws CompilationFailedException, ClassNotFoundException, IOException {
				
		URL url = Thread.currentThread().getContextClassLoader().getResource(template);
		
		Map<String, Object> bind = bind();

		SimpleTemplateEngine engine = new SimpleTemplateEngine();
		Template template = engine.createTemplate(url);
		return template.make(bind).toString();
		
	}

	abstract protected Map<String, Object> bind();
	
	public String validate(String code){
		
		try {
			Loader loader = Loader.loader();
			Class<?> klass = loader.parseClass(code);
			klass.newInstance();
			loader.removeClassCache(klass);
		} 
		catch (MultipleCompilationErrorsException e) { return e.getMessage(); } 
		catch (InstantiationException e) { return e.getStackTrace()[0].toString(); }
		catch (IllegalAccessException e) { return e.getStackTrace()[0].toString(); }
		
		return "";
		
	}
	
}
