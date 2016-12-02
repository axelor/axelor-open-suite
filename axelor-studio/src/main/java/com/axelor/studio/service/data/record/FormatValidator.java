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
	
	public boolean isValidColumn(String column) {
		
		if (column == null || column.startsWith("*")) {
			return false;
		}
		
		return true;
	}
	
	private void validateFields(Class<?> klass, String[] row) {
		
		Mapper mapper = Mapper.of(klass);
		
		List<String> invalidFields = new ArrayList<String>();
		List<String> invalidRules = new ArrayList<String>();
		for (String col : row) {
			if (!isValidColumn(col)) {
				continue;
			}
			String[] cols = col.split("\\(");
			log.debug("Validating field: {}", col);
			String[] field= cols[0].split("\\.");
			String name = isValidName(field[0]);
			if (name == null) {
				invalidFields.add(name);
			}
			
			Property property = checkProperty(mapper.getProperty(name), field);
			if (property == null) {
				invalidFields.add(col);
			}
			if (cols.length > 1) {
			    String[] rules = cols[1].replaceAll("\\)", "").split(",");
			    if (!RULES.containsAll(Arrays.asList(rules))) {
			    	invalidRules.add(col);
			    }
			    else if (field.length > 1 
			    		&& (property.getTarget() != null || !cols[0].contains(".$"))) {
			    	log.debug("Property name: {}", property.getName());
			    	invalidRules.add(col);
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

	private String isValidName(String field) {
		
		String name = field;
		if (name.contains("[") && name.contains("]")) {
			if (name.matches("\\w+\\[[0-9]+\\]")) {
				name = field.split("\\[")[0];
			}
			else {
				return null;
			}
		}
		
		return name;
	}

	private Property checkProperty(Property property, String[] field) {
		
		if (field.length > 1) {
			if (property.getTarget() == null) {
				log.debug("Property '{}' is not a reference", property.getName());
				return null;
			}
			String[] subFields = Arrays.copyOfRange(field, 1, field.length);
			String name = subFields[0];
			if (name.startsWith("$")) {
				name = name.substring(1);
			}
			name = isValidName(name);
			if (name == null) {
				return null;
			}
			property =  Mapper.of(property.getTarget()).getProperty(name);
			
			if (subFields[0].startsWith("$") && property != null && property.isCollection()) {
				return null;
			}
			
			return checkProperty(property, subFields);
		}
		
		return property;
	}
	
}
