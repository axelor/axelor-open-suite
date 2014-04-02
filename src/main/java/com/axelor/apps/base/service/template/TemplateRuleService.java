/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service.template;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.db.TemplateRule;
import com.axelor.apps.base.db.TemplateRuleLine;
import com.axelor.db.Model;
import com.axelor.meta.ActionHandler;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.Action;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.Resource;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class TemplateRuleService {
	
	@Inject
	private Injector injector;
	
	@Inject
	private TemplateService ts;
	
	@Inject
	private ActionHandler ac;
	
	public Map<String, Object> getContext(TemplateRule templateRule, Model bean) {
		Template template = this.getTemplate(bean, templateRule);
		
		if(template == null) {
			return null;
		}
		
		return ts.getContext(template, bean);
	}

	public Template getTemplate(Model bean, TemplateRule templateRule) {
		if (templateRule.getTemplateRuleLineList() == null
				|| templateRule.getMetaModel() == null) {
			return null;
		}

		Class<?> klass = this.getTemplateClass(templateRule.getMetaModel());
		if (!klass.isInstance(bean)) {
			throw new IllegalArgumentException("Bean is not an instance of " + klass.getSimpleName());
		}

		List<TemplateRuleLine> lines = _sortRuleLine(templateRule.getTemplateRuleLineList());
		for (TemplateRuleLine line : lines) {
			Boolean isValid = this.runAction(bean, line.getMetaAction(), klass.getName());
			if(isValid) {
				return line.getTemplate();
			}
		}

		return null;
	}

	private Class<?> getTemplateClass(MetaModel metaModel) {
		String model = metaModel.getFullName();

		try {
			return Class.forName(model);
		} catch (NullPointerException e) {
		} catch (ClassNotFoundException e) {
		}
		return null;
	}
	
	/**
	 * Trier une liste de ligne de règle de template
	 * 
	 * @param templateRuleLine
	 */
	private List<TemplateRuleLine> _sortRuleLine(List<TemplateRuleLine> templateRuleLine){
		
		Collections.sort(templateRuleLine, new Comparator<TemplateRuleLine>() {
			
			@Override
			public int compare(TemplateRuleLine o1, TemplateRuleLine o2) {
				return o1.getSequence().compareTo(o2.getSequence());
			}
		});
		
		return templateRuleLine;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Boolean runAction(Model bean, MetaAction metaAction, String klassName) {
		if(metaAction == null) {
			return true;
		}
		
		Action action = MetaStore.getAction(metaAction.getName());
		ActionHandler handler = createHandler(bean, action.getName(), klassName);
		Object result = action.wrap(handler);

		if(result instanceof Map) {
			Map<Object, Object> data = (Map<Object, Object>) result;
			if(data.containsKey("errors") && data.get("errors") != null && !( (Map) data.get("errors") ).isEmpty()) {
				return true;
			}
			else {
				return false;
			}
		}
		return (Boolean) result;
	}
	
	private ActionHandler createHandler(Model bean, String action, String model) {
		
		ActionRequest request = new ActionRequest();
		
		Map<String, Object> map = Maps.newHashMap();
		map.put("context", Resource.toMap(bean));
		request.setData(map);
		request.setModel(model);
		request.setAction(action);

		return ac.forRequest(request);
	}

}
