package com.axelor.studio.service.data.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.studio.service.data.importer.DataReader;
import com.google.common.collect.Sets;

public class FormatValidator {
	
	private final Logger log = LoggerFactory.getLogger(FormatValidator.class);
	
	private static Set<String> RULES = Sets.newHashSet("Search","Unique");
	
	private StringBuilder logger = new StringBuilder();
	
	public String validate(List<Class<?>> classes, DataReader reader) {
		
		logger = new StringBuilder();
		
		for (Class<?> klass : classes) {
			log.debug("Validating klass: {}", klass.getName());
			String[] row = reader.read(klass.getSimpleName(), 0);
			if (row != null) {
				validateFields(klass, row);
			}

			if (row == null){
				logger.append(String.format(I18n.get("\n Object: %s, Invalid format"), klass.getName()));
			}
		}
		
		return logger.toString();
	}
	
	
	private void validateFields(Class<?> klass, String[] row) {
		
		Mapper mapper = Mapper.of(klass);
		
		List<String> invalidFields = new ArrayList<String>();
		List<String> invalidRules = new ArrayList<String>();
		for (String name : row) {
			String[] column = name.split("\\(");
			log.debug("Validating field: {}", name);
			String field[] = column[0].split("\\.");
			if (!checkProperty(mapper.getProperty(field[0]), field)) {
				invalidFields.add(name);
			}
			if (column.length > 1) {
			    String[] rules = column[1].replaceAll("\\)", "").split(",");
			    if (!RULES.containsAll(Arrays.asList(rules))) {
			    	invalidRules.add(name);
			    }
			}
		}
		
		if (!invalidFields.isEmpty()) {
			logger.append(String.format(I18n.get("\n Object: %s, Invalid fields: %s"), klass.getName(), invalidFields));
		}
		
		if (!invalidRules.isEmpty()) {
			logger.append(String.format(I18n.get("\n Object: %s, Invalid rules: %s"), klass.getName(), invalidRules));
		}
		
	}

	private boolean checkProperty(Property property, String[] field) {
		
		if (property == null) {
			log.debug("No property found");
			return false;
		}
		
		if (field.length > 1) {
			if (property.getTarget() == null) {
				log.debug("Property '{}' is not a reference", property.getName());
				return false;
			}
			String[] subFields = Arrays.copyOfRange(field, 1, field.length);
			Property subProperty =  Mapper.of(property.getTarget()).getProperty(subFields[0]);
			return checkProperty(subProperty, subFields);
		}
		
		return true;
	}
}
