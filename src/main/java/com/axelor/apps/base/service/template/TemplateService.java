/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
