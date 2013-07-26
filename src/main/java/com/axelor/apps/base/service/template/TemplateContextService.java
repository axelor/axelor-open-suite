package com.axelor.apps.base.service.template;

import java.util.Map;

import com.axelor.apps.base.db.TemplateContext;
import com.axelor.apps.base.db.TemplateContextLine;
import com.axelor.db.Model;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class TemplateContextService {
	
	@Inject
	private TemplateContextLineService tcls;
	
	public Map<String, Object> getContext(TemplateContext templateContext, Model context) {
		Map<String, Object> map = Maps.newHashMap();
		
		if(templateContext.getTemplateContextLine() != null) {
			for (TemplateContextLine line : templateContext.getTemplateContextLine()) {
				Object o = tcls.evaluate(line, context);
				map.put(line.getKey(), o);
			}
		}
		
		return map;
	}

}
