package com.axelor.studio.service.data.record;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class RecordImporterService {
	
	private final Logger logger = LoggerFactory.getLogger(RecordImporterService.class);
	
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
	private Set<String> uniqueSet = null;
	private Map<String, String> searchMap = null;

	@Transactional
	public void importRecords(DataReader reader, MetaFile inputFile) throws AxelorException {
		
		if (inputFile == null) {
			throw new AxelorException(I18n.get("Invalid input file"), 1);
		}
		
		if (reader == null) {
			throw new AxelorException(I18n.get("Invalid import call"), 2);
		}
		
		reader.initialize(inputFile);
		header = null; 
		List<MetaModel> models = getModels(reader);
		log = formatValidator.validate(models, reader);
		if (!Strings.isNullOrEmpty(log)) {
			throw new AxelorException(I18n.get("Error in input format. Please check the log"), 1);
		}
		
		for (MetaModel model : models) {
			importModel(model, reader);
		}
		
	}
	
	public String getLog() {
		return log;
	}

	private List<MetaModel> getModels(DataReader reader) throws AxelorException {
		
		List<MetaModel> models = new ArrayList<MetaModel>();
		
		for (String name : reader.getKeys()) {
			MetaModel model = metaModelRepo.findByName(name);
			if (model == null) {
				log += "\n " + name;
			}
			else {
				try {
					ClassUtils.findClass(model.getFullName());
					models.add(model);
				}catch(IllegalArgumentException e) {
					log += "\n " + name;
				}
			}
			
		}
		
		if (log != null) {
			throw new AxelorException(I18n.get("Some models are missing, please check the log"), 1);
		}
		
		return models;
	}
	
	@Transactional
	public void importModel(MetaModel model, DataReader reader) {
		
		Class<?> klass = ClassUtils.findClass(model.getFullName());
		Mapper mapper = Mapper.of(klass);
		String tab = model.getName();
		header = Arrays.asList(reader.read(tab, 0));
		setRuleMap();
		
		String query = null;
		if(!uniqueSet.isEmpty()) {
			query = getQuery(false, null);
		}
		
		for (int i = 2; i < reader.getTotalLines(tab); i++) {
			String[] row = reader.read(tab, i);
			Model obj = getModel(mapper, query, row);
			List<String> cleared = new ArrayList<String>();
			String[] refKey = null;
			Map<String, String> refData = null;
			for (int j = 0; j < row.length;  j++) {
				String field = header.get(j).split("\\(")[0];
				String[] target = field.split("\\.");
				String val = row[j];
				if (target.length == 1) {
					Property property = mapper.getProperty(target[0]);
					property.set(obj, adapt(property.getJavaType(), val));
				}
				else {
					if (refKey != null && (!refKey[0].equals(target[0]) || refKey[1].equals(target[1]))) {
						String refQuery = getQuery(true, refData.keySet());
						processRefField(mapper.getProperty(refKey[0]), obj, refData, cleared, refQuery);
						refKey = null;
					}
					if (refKey == null) {
						refKey = target;
						refData = new HashMap<String, String>();
					}
					refData.put(field, val);
				}
			}
			if (refKey != null) {
				String refQuery = getQuery(true, refData.keySet());
				processRefField(mapper.getProperty(refKey[0]), obj, refData, cleared, refQuery);
			}
			
//			logger.debug("Model: {}, obj: {}", klass.getName(), obj);
			JPA.save(obj);
		}

	}

	private Model getModel(Mapper mapper, String query, String[] row) {
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		if (query != null) {
			for (String field : uniqueSet) {
				String target = field.split("\\(")[0];
				Property property =  mapper.getProperty(target);
				data.put(target, adapt(property.getJavaType(), row[header.indexOf(field)]));
			}
			Model searched = searchObject(mapper, data, query);
			if (searched != null) {
				return searched;
			}
		}
	
		return (Model) Mapper.toBean(mapper.getBeanClass(), data);
	}

	private void setRuleMap() {
		
		searchMap = new HashMap<String, String>();
		uniqueSet = new HashSet<String>();
		
		int count = 0;
		
		for(String field : header) {
			count++;
			String[] column = field.split("\\(");
			if (column.length > 1) {
				String rule = column[1].replace(")", "");
				switch(rule){
				case SEARCH:
					if (field.contains(".") && !searchMap.containsKey(column[0])) {
						searchMap.put(column[0], "param" + count);
					}
					break;
				case UNIQUE:
					if (!field.contains(".")) {
						uniqueSet.add(field);
					}
					break;
				}
			}
			
		}
		
	}
	
	private String getQuery(boolean refQuery, Set<String> refFields) {
		
		String query = null;
		
		Set<String> fields = new HashSet<String>();
		if (refQuery) {
			fields.addAll(searchMap.keySet());
			fields.retainAll(refFields);
		}
		else {
			fields = uniqueSet;
		}
		
		for (String field : fields) {
			field = field.split("\\(")[0];
			String param = field;
			if (refQuery) {
				param = searchMap.get(field);
				field = field.substring(field.indexOf(".") + 1);
			}
			String condition = "self." + field + " = :" + param;
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
	
	private void processRefField(Property property, Model obj, Map<String, String> refData, List<String> cleared, String query) {
		
		logger.debug("Processing field : {}, data: {}", property.getName(), refData);
		Model refObj = getRefObj(property.getTarget(), refData, query);
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

	private Model getRefObj(Class<?> klass, Map<String, String> data, String query) {
		
		Model refObj = null;
		Mapper mapper = Mapper.of(klass);
		
		if (query != null) {
			refObj = searchRefModel(klass, query, data);
		}
		
		if (refObj == null) {
			refObj = (Model) Mapper.toBean(klass, new HashMap<String, Object>());
		}
		
		List<String> cleared = new ArrayList<String>();
		String[] refKey = null;
		Map<String, String> refData = null;
		for (String key : data.keySet()) {
			String field = key.substring(key.indexOf(".") + 1);
			String[] target = field.split("\\.");
			Property property = mapper.getProperty(target[0]);
			if (target.length == 1) {
				property.set(refObj, adapt(property.getJavaType(), data.get(key)));
			}
			else {
				if (refKey != null && (refKey[0] != target[0] || refKey[1] == target[1])) {
					processRefField(mapper.getProperty(refKey[0]), refObj, refData, cleared, null);
					refKey = null;
				}
				if (refKey == null) {
					refKey = target;
					refData = new HashMap<String, String>();
				}
				refData.put(field, data.get(key));
			}
		}
		if (refKey != null) {
			processRefField(mapper.getProperty(refKey[0]), refObj, refData, cleared, null);
		}
		
		logger.debug("Ref object: {}", refObj);
		
		return refObj;
	}

	private Model searchRefModel(Class<?> klass, String query, Map<String, String> data) {
		
		Mapper mapper = Mapper.of(klass);
		
		Map<String, Object> typedData = new HashMap<String, Object>();
		Set<String> fields = data.keySet();
		fields.retainAll(searchMap.keySet());
		for (String field : fields) {
			String[] target = field.substring(field.indexOf(".") + 1).split("\\.");
			Object val = getTypedValue(data.get(field), mapper, target);
			typedData.put(searchMap.get(field), val);
		}
		
		return searchObject(mapper, typedData, query);
	}

	private Object getTypedValue(String value, Mapper mapper, String[] target) {
		
		Property property = mapper.getProperty(target[0]);
		if (target.length > 1) {
			mapper = Mapper.of(property.getTarget());
			return getTypedValue(value, mapper, Arrays.copyOfRange(target, 1, target.length));
		}
		
		return adapt(property.getJavaType(), value);
	}	


}	
