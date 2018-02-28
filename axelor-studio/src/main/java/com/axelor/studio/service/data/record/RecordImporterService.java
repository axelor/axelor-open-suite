/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.data.record;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.ClassUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.service.data.importer.DataReader;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class RecordImporterService {
	
	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	private static final String SEARCH = "Search";
	private static final String UNIQUE = "Unique";
	
	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/YYYY");
	private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/YYYY HH:mm:ss");
	
	@Inject
	private FormatValidator formatValidator;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	private String log = null;
	private List<String> header = null;
	private Map<String, String> uniqueMap = null;
	private Map<String, String> searchMap = null;
	private int preference = 0;

	public void importRecords(DataReader reader, MetaFile inputFile, int preference) throws AxelorException {
		
		if (inputFile == null || reader == null) {
			return;
		}
		
		this.preference = preference;
		
		reader.initialize(inputFile);
		List<Class<?>> classes = getClasses(reader);
		log = formatValidator.validate(classes, reader);
		if (!Strings.isNullOrEmpty(log)) {
			throw new AxelorException(I18n.get("Error in input format. Please check the log"), 1);
		}
		
		for (Class<?> klass : classes) {
			Mapper mapper = Mapper.of(klass);
			importSheet(mapper, reader);
		}
		
	}
		
	public String getLog() {
		return log;
	}

	private List<Class<?>> getClasses(DataReader reader) throws AxelorException {
		
		List<Class<?>> classes = new ArrayList<Class<?>>();
		
		for (String name : reader.getKeys()) {
			MetaModel model = metaModelRepo.findByName(name);
			if (model == null) {
				log += "\n " + name;
			}
			else {
				try {
					classes.add(ClassUtils.findClass(model.getFullName()));
				}catch(IllegalArgumentException e) {
					log += "\n " + name;
				}
			}
			
		}
		
		if (log != null) {
			throw new AxelorException(I18n.get("Some models are missing, please check the log"), 1);
		}
		
		return classes;
	}
	
	private void importSheet(Mapper mapper, DataReader reader) {
		
		String klass = mapper.getBeanClass().getSimpleName();
		header = Arrays.asList(reader.read(klass, 0));
		setRuleMap();
		String query = getQuery(false, null, 0);
		
		Integer totalLines = reader.getTotalLines(klass);
		logger.debug("Sheet: {} Total lines: {}", klass, totalLines);
		for (Integer i = 2; i < reader.getTotalLines(klass); i++) {
			String[] row = reader.read(klass, i);
			importModel(mapper, query, row, i);
		}
	}
	
	
	private void setRuleMap() {
		
		searchMap = new HashMap<String, String>();
		uniqueMap = new HashMap<String, String>();
		
		int count = 0;
		
		for (String field : header) {
			count++;
			if (!formatValidator.isValidColumn(field)) {
				continue;
			}
			String[] column = field.split("\\(");
			if (column.length < 2) {
				continue;
			}
			String[] rules = column[1].replace(")", "").split(",");
			for (String rule : rules) {
				switch(rule) {
				case SEARCH:
					searchMap.put(column[0], "param" + count);
					break;
				case UNIQUE:
					uniqueMap.put(field, "param" + count);
					break;
				}
			}
		}
		
		logger.debug("Search map: {}", searchMap);
		logger.debug("Unique map: {}", uniqueMap);
		
	}
	
	private String getQuery(boolean refQuery, Set<String> refFields, int index) {
		
		String query = null;
		Set<String> fields = getQueryFields(refQuery, refFields);
		
		for (String field : fields) {
			
			String target = field.split("\\(")[0].replace("$", "");
			
			if (refQuery) {
				if (field.split("\\$")[0].split("\\.").length > index) {
					continue;
				}
				else {
					String[] targets = target.split("\\.");
					target = Joiner.on(".").join(Arrays.copyOfRange(targets, index, targets.length));
				}
			}
			String param;
			if (refQuery) {
				param = searchMap.get(field);
			}
			else {
				param = uniqueMap.get(field);
			}
			String condition = "self." + target + " = :" + param;
			if (query == null) {
				query = condition;
			}
			else {
				query += " AND " + condition;
			}
			
		}
		
		logger.debug("Query prepared: {}", query);
		
		return query;
	}

	private Set<String> getQueryFields(boolean refQuery, Set<String> refFields) {
		
		Set<String> fields = new HashSet<String>();

		if (refQuery) {
			fields.addAll(searchMap.keySet());
			fields.retainAll(refFields);
		}
		else {
			fields.addAll(uniqueMap.keySet());
		}
		return fields;
	}
	
	
	private void importModel(Mapper mapper, String query, String[] row, int ind) {
		
		Model obj = getModel(mapper, query, row);
		if (preference == 0 && obj.getId() != null) {
			return;
		}
		
		processFields(mapper, obj, row);
		
		try {
			if (obj != null) {
				if(!JPA.em().getTransaction().isActive()) {
					JPA.em().getTransaction().begin();
				}
				JPA.save(obj);
				JPA.em().getTransaction().commit();
				if(!JPA.em().getTransaction().isActive()) {
					JPA.em().getTransaction().begin();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			log += "\n " + String.format(I18n.get("Row: %s Error: %s"), ind, e.getMessage());  
			if (JPA.em().getTransaction().isActive()) {
				JPA.em().getTransaction().rollback();
			}
		}
	}
	
	private Model getModel(Mapper mapper, String query, String[] row) {
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		if (query != null) {
			for (String field : uniqueMap.keySet()) {
				String[] target = field.split("\\(")[0].split("\\.");
				data.put(uniqueMap.get(field), getTypedValue(row[header.indexOf(field)], mapper, target));
			}
			Model searched = searchObject(mapper, data, query);
			if (searched != null) {
				return searched;
			}
		}
	
		return (Model) Mapper.toBean(mapper.getBeanClass(), data);
	}
	
	private Object getTypedValue(String value, Mapper mapper, String[] target) {
		
		String name = target[0].split("\\[")[0].replace("$", "");
		Property property = mapper.getProperty(name);
		if (target.length > 1) {
			mapper = Mapper.of(property.getTarget());
			return getTypedValue(value, mapper, Arrays.copyOfRange(target, 1, target.length));
		}
		
		return adapt(property.getJavaType(), value);
	}	
	
	
	private Object adapt(Class<?> javaType, String value) {
		
		if (Strings.isNullOrEmpty(value)) {
			return value;
		}
		
		if (javaType.equals(LocalDate.class)) {
			return LocalDate.parse(value, DATE_FORMATTER);
		}
		
		if (javaType.equals(LocalDateTime.class)) {
			return LocalDateTime.parse(value, TIME_FORMATTER);
		}
		
		return value;
	}

	@SuppressWarnings("unchecked")
	private Model searchObject(Mapper mapper, Map<String, Object> data, String query) {
		
		logger.debug("Class: {}", mapper.getBeanClass());
		logger.debug("Query: {}", query);
		logger.debug("Data: {}", data);
		
		Model model =  JPA.all((Class<Model>)mapper.getBeanClass()).filter(query).bind(data).fetchOne();
		logger.debug("Model found: {}", model);

		return model;
		
	}

	private void processFields(Mapper mapper, Model obj, String[] row) {
		
		List<String> cleared = new ArrayList<String>();
		String refKey = null;
		Map<String, String> refData = null;

		for (int j = 0; j < row.length; j++) {
			
			String column = header.get(j);
			if (!formatValidator.isValidColumn(column)) {
				continue;
			}
			
			String field = column.split("\\(")[0];
			String[] target = field.split("\\.");
			String val = row[j];
			String fieldName = target[0];
			if (target.length == 1) {
				Property property = mapper.getProperty(fieldName);
				if (preference == 2) {
					if (property.get(obj) != null) {
						continue;
					}
				}
				property.set(obj, adapt(property.getJavaType(), val));
			}
			else {
				if (refKey != null && !refKey.equals(fieldName)) {
					refKey = refKey.split("\\[")[0];
					processRefField(mapper.getProperty(refKey), obj, refData, cleared, 1);
					refKey = null;
				}
				if (refKey == null) {
					refKey = fieldName;
					refData = new HashMap<String, String>();
				}
				refData.put(field, val);
			}
		}
		
		if (refKey != null) {
			refKey = refKey.split("\\[")[0];
			processRefField(mapper.getProperty(refKey), obj, refData, cleared, 1);
		}
		
	}

	
	private void processRefField(Property property, Model obj, Map<String, String> refData, List<String> cleared, int index) {
		
		logger.debug("Processing field : {}, data: {}", property.getName(), refData);
		if (preference == 2 && hasValue(property.get(obj), property.isCollection())) {
			return;
		}

		String query = getQuery(true, refData.keySet(), index);
		Model refObj = getRefObj(property.getTarget(), refData, query, index);
		if (property.isCollection()) {
			if (!cleared.contains(property.getName())) {
				property.clear(obj);
				cleared.add(property.getName());
			}
			property.add(obj, refObj);
		}
		else {
			property.set(obj, refObj);
		}
	
	}

	private boolean hasValue(Object value, boolean isCollection) {
		
		if (value == null) {
			return false;
		}
		
		if (isCollection) {
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) value;
			if (collection.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}

	private Model getRefObj(Class<?> klass, Map<String, String> data, String query, int index) {
		
		logger.debug("Ref object data: {}", data);
		Model refObj = null;
		Mapper mapper = Mapper.of(klass);
		
		if (query != null) {
			refObj = searchRefModel(klass, query, data, index);
		}
		if (refObj == null) {
			refObj = (Model) Mapper.toBean(klass, new HashMap<String, Object>());
		}
		
		List<String> cleared = new ArrayList<String>();
		String refKey = null;
		Map<String, String> refData = null;
		
		for (String key : data.keySet()) {
			String[] target = key.split("\\.");
			String fieldName = target[index].replace("$", "");
			if (target.length == index + 1) {
				Property property = mapper.getProperty(fieldName);
				logger.debug("Setting property: {}, value: {}", property.getName(), data.get(key));
				property.set(refObj, adapt(property.getJavaType(), data.get(key)));
			}
			else {
				if (refKey != null && !refKey.equals(fieldName)) {
					refKey = refKey.split("\\[")[0];
					processRefField(mapper.getProperty(refKey), refObj, refData, cleared, index + 1);
					refKey = null;
				}
				if (refKey == null) {
					refKey = fieldName;
					refData = new HashMap<String, String>();
				}
				refData.put(key, data.get(key));
			}
		}
		
		if (refKey != null) {
			refKey = refKey.split("\\[")[0];
			processRefField(mapper.getProperty(refKey), refObj, refData, cleared, index + 1);
		}
		
		logger.debug("Ref object created: {}", refObj);
		
		return refObj;
	}

	private Model searchRefModel(Class<?> klass, String query, Map<String, String> data, int index) {
		
		Mapper mapper = Mapper.of(klass);
		
		Map<String, Object> typedData = new HashMap<String, Object>();
		for (String field : data.keySet()) {
			if (searchMap.containsKey(field)) {
				String[] target = field.split("\\.");
				target = Arrays.copyOfRange(target, index, target.length);
				Object val = getTypedValue(data.get(field), mapper, target);
				typedData.put(searchMap.get(field), val);
			}
		}
		
		return searchObject(mapper, typedData, query);
	}


}	
