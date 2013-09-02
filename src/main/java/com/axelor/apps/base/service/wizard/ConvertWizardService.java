package com.axelor.apps.base.service.wizard;

import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;

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
						Map map = (Map) value;
						Object id = map.get("id");
						value = JPA.find((Class) p.getTarget(), Long.parseLong(id.toString()));
					} 
					p.set(obj, value);
				}
			}
			
			return obj;
		}
		
		return null;
	}
	
	
}
