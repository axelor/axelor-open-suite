/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service.wizard;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Lists;

public class ConvertWizardService {

	private static final Logger LOG = LoggerFactory.getLogger(ConvertWizardService.class);
	
	public Object createObject(Map<String, Object> context, Object obj, Mapper mapper) throws AxelorException  {
		
		if(context != null)  {
			
			final int random = new Random().nextInt();
			for(final Property p : mapper.getProperties()) {
				
				if (p.isVirtual() || p.isPrimary() || p.isVersion()) {
					continue;
				}

				LOG.debug("Property name / Context value  : {} / {}", p.getName());	
				
				Object value = context.get(p.getName());
				
				LOG.debug("Context value : {}", value);	
				
				if(value != null)  {
				
					if (value instanceof String && p.isUnique()) {
						value = ((String) value) + " (" +  random + ")";
					}	
	
					if(value instanceof Map)  {
						LOG.debug("Map");	
						Map map = (Map) value;
						Object id = map.get("id");
						value = JPA.find((Class) p.getTarget(), Long.parseLong(id.toString()));
					} 
					if(value instanceof List)  {
						LOG.debug("List");	
						
						List<Object> valueList = (List<Object>) value;
						List<Object> resultList = Lists.newArrayList();
						
						if(valueList != null)  {
							for(Object object : valueList)  {
								Map map = (Map) object;
								Object id = map.get("id");
								resultList.add(JPA.find((Class) p.getTarget(), Long.parseLong(id.toString())));
							}
						}
						value = resultList;
						
					}
					
					p.set(obj, value);
				}
			}
			
			return obj;
		}
		
		return null;
	}
	
	
}
