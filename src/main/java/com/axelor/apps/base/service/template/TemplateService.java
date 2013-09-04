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
package com.axelor.apps.base.service.template;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.db.TemplateContext;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.db.MetaModel;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class TemplateService {
	
	@Inject
	private TemplateContextService tcs;
	
	public Map<String, Object> getContext(Template template, Model bean) {
		if(template.getTemplateContext() == null) {
			return null;
		}
		
		return tcs.getContext(TemplateContext.find(Template.find(template.getId()).getTemplateContext().getId()), bean);
	}

	public void checkTargetReceptor(Template template) throws AxelorException {
		String target = template.getTarget();
		MetaModel metaModel = template.getMetaModel();
		
		if(Strings.isNullOrEmpty(target)) {
			return;
		}
		if(metaModel == null) {
			throw new AxelorException("Model empty. Please configure a model.", IException.MISSING_FIELD);
		}
		
		try {
			this.validTarget(target, metaModel);
		}
		catch(Exception ex) {
			throw new AxelorException("Your target receptor is not valid. Please check it.", IException.INCONSISTENCY);
		}
	}

	private void validTarget(String target, MetaModel metaModel) throws ClassNotFoundException {
		Iterator<String> iter = Splitter.on(".").split(target).iterator();
		Property p = Mapper.of(Class.forName(metaModel.getFullName())).getProperty(iter.next());
		while(iter.hasNext() && p != null) {
			p = Mapper.of(p.getTarget()).getProperty(iter.next());
		}
		
		if(p == null) {
			throw new IllegalArgumentException();
		}
	}

	public String processSubject(Template template, Model bean, String beanName, Map<String, Object> context) {
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		
		maker.setTemplate(template.getSubject());
		maker.setContext(bean, context, beanName);
		String result = maker.make();
		
		return result;
	}

	public String processContent(Template template, Model bean, String beanName, Map<String, Object> context) {
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		
		maker.setTemplate(template.getContent());
		maker.setContext(bean, context, beanName);
		String result = maker.make();
		
		return result;
	}

}
