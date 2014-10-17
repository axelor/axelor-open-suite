/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import java.util.Map;

import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.TemplateService;
import com.axelor.db.Model;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TemplateBaseService extends TemplateService {
	
	@Inject
	private TemplateContextService tcs;
	
	public Map<String, Object> getContext(Template template, Model bean) {
		if(template.getTemplateContext() == null) {
			return null;
		}
		
		return tcs.getContext(tcs.find(find(template.getId()).getTemplateContext().getId()), bean);
	}

	

}
