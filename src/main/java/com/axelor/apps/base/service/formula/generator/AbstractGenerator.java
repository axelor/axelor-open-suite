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
