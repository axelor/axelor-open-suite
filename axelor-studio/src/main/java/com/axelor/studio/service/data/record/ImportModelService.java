package com.axelor.studio.service.data.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaField;
import com.axelor.studio.db.RecordImportRule;
import com.axelor.studio.db.RecordImportRuleLine;
import com.axelor.studio.db.repo.RecordImportRuleLineRepository;
import com.axelor.studio.service.data.importer.DataReader;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportModelService {
	
	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/YYYY");
	
	private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/YYYY HH:mm:ss");
	
	private final Logger log = LoggerFactory.getLogger(ImportModelService.class);
	
	@Inject
	private RecordImportRuleLineRepository ruleLineRepo;
	
//	@Transactional
	public String importModel(String tab, RecordImportRule importRule, DataReader reader) {
		
		try {

			int totalLines = reader.getTotalLines(tab);
			if (totalLines < 1) {
				return null;
			}
			
			log.debug("Importing sheet: {}", tab);
			
			String[] header = reader.read(tab, 0);
			
			ModelImporter importer = new ModelImporter(importRule, true);
			
			for (int i=1; i<totalLines; i++) {
				String[] record = reader.read(tab, i);
				if (record == null) {
					continue;
				}
				
				log.debug("Importing row: {}", Arrays.asList(record));
				Map<String, String> data = prepareData(header, record);
				
				Model model = importer.importData(data);
				if (model != null) {
					JPA.save(model);
				}
				else {
					log.debug("Error in importing row: {}", i);
				}
				
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Map<String, String> prepareData(String[] header, String[] record) {
		
		Map<String, String> data = new HashMap<String, String>();
		
		int count = 0;
		for (String key : header) {
			data.put(key, record[count]);
			count++;
		}

		return data;
	}
	
	class ModelImporter {
		
		private Class<?> klass;
		private Mapper mapper;
		private List<RecordImportRuleLine> lines;
		private boolean search;
		private boolean searchOnly;
		private String query;
		
		public ModelImporter(RecordImportRule importRule, boolean search) throws ClassNotFoundException {
			klass = Class.forName(importRule.getMetaModel().getFullName());
			setRuleLines(importRule);
			setQuery(lines);
			mapper = Mapper.of(klass);
			searchOnly = importRule.getRuleSelect() == 1;
			this.search = search;
		
		}
		
		private void setRuleLines(RecordImportRule importRule) {
			lines =  ruleLineRepo.all()
				.filter("self.recordImportRule = ?1", importRule)
				.order("-searchField")
				.fetch();
				
		}
		
		public Model importData(Map<String, String> data) throws ClassNotFoundException {
			
			Model model =  (Model) Mapper.toBean(klass, new HashMap<String, Object>());
			
			List<String> cleared = new ArrayList<String>();
			
			for (RecordImportRuleLine line : lines) {
				String column = line.getColumnName();
				MetaField field = line.getField();
				Property property = mapper.getProperty(field.getName());
				if (!line.getSearchField() && query != null && search){
					Model searched = searchModel(query, mapper.getBeanClass(), model);
					if(searched == null &&  searchOnly) {
						return null;
					}
					if (searched != null) {
						model = searched;
					}
					query = null;
				}
				
				if (data.containsKey(column) && !property.isReference()) {
					property.set(model, adapt(property.getJavaType(), data.get(column)));
				}
				else if (line.getRefRule() != null) {
					ModelImporter importer = new ModelImporter(line.getRefRule(), field.getRelationship() != "OneToMany");
					Object refRecord = importer.importData(data);
					if (property.isCollection()) {
						if (!cleared.contains(property.getName())) {
							property.clear(model);
							cleared.add(property.getName());
						}
						property.add(model, refRecord);
					}
					else {
						property.set(model, refRecord);
					}
				}
			}
			
			
			return model;
		}
		
		private Model searchModel(String query, Class<?> klass, Model model) {
			
			Map<String, Object> data = Mapper.toMap(model);
			
			Model searched = JPA.all((Class<Model>)klass).filter(query).bind(data).fetchOne();
			
			log.debug("Model searched: {}", searched);
			
			return searched;
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

		private void setQuery(List<RecordImportRuleLine> lines) {
			
			
			for (RecordImportRuleLine line : lines) {
				if (line.getRefRule() != null || !line.getSearchField()) {
					continue;
				}
				
				String field = line.getField().getName();
				field = "self." + field + " = :" + field;
				if (query == null) {
					query = field;
				}
				else {
					query += " AND " + field;
				}
			}
			
			log.debug("Query prepared: {}", query);
			
		}
		
	}

	
}
