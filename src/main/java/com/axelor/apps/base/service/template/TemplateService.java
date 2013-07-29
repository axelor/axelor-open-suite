package com.axelor.apps.base.service.template;

import java.util.Map;

import com.axelor.apps.base.db.Template;
import com.axelor.db.Model;
import com.google.inject.Inject;

public class TemplateService {
	
	@Inject
	private TemplateContextService tcs;
	
	public Map<String, Object> getContext(Template template, Model context) {
		if(template.getTemplateContext() == null) {
			return null;
		}
		
		return tcs.getContext(template.getTemplateContext(), context);
	}

}
