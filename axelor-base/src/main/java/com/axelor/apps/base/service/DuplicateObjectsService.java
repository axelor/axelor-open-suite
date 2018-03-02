/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import com.axelor.apps.tool.StringTool;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class DuplicateObjectsService {
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Transactional
	public void removeDuplicate(List<Long> selectedIds, String modelName) {
		
		List<Object> duplicateObjects = getDuplicateObject(selectedIds, modelName);
		Object originalObjct = getOriginalObject(selectedIds,modelName);
		List<MetaField> allField = metaFieldRepo.all().filter("(relationship = 'ManyToOne' AND typeName = ?1) OR (relationship = 'ManyToMany' AND (typeName = ?1 OR metaModel.name =?1))",modelName).fetch();
		for (MetaField metaField : allField) {
			if ("ManyToOne".equals(metaField.getRelationship())) {
					Query update = JPA.em().createQuery("UPDATE " + metaField.getMetaModel().getFullName() + " self SET self." + metaField.getName() + " = :value WHERE self." + metaField.getName() + " in (:duplicates)");
					update.setParameter("value", originalObjct);
					update.setParameter("duplicates", duplicateObjects);
					update.executeUpdate();
			} else if ("ManyToMany".equals(metaField.getRelationship())) {
				
				if(metaField.getTypeName().equals(modelName)) {      
					Query select = JPA.em().createQuery("select self from " + metaField.getMetaModel().getFullName() + " self LEFT JOIN self." + metaField.getName() + " as x WHERE x IN (:ids)");
					select.setParameter("ids", duplicateObjects);
					List<?> list = select.getResultList();
					for(Object obj : list) {
						Set<Object> items = (Set<Object>) Mapper.of(obj.getClass()).get(obj, metaField.getName());
						for(Object dupObj: duplicateObjects) {
							if (items.contains(dupObj)) {
								items.remove(dupObj);
							}
						}					
						items.add(originalObjct);
					}
				}
					Mapper mapper = Mapper.of(originalObjct.getClass());				
					Set<Object> existRelationalObjects = (Set<Object>) mapper.get(originalObjct, metaField.getName());
					
					for(int i=0; i < duplicateObjects.size() ; i++){ 
						Set<Object> newRelationalObjects = (Set<Object>) mapper.get(duplicateObjects.get(i), metaField.getName());
						if (newRelationalObjects != null) {
							existRelationalObjects.addAll(newRelationalObjects);
							mapper.set(duplicateObjects.get(i), metaField.getName(), new HashSet<>());
						}
					}				
			} 
		}
		JPA.em().flush();
		JPA.em().clear();
		for(Object obj : getDuplicateObject(selectedIds, modelName)) {
			JPA.em().remove(obj);
		
		}
	}
	
	@Transactional
	public Object getOriginalObject(List<Long> selectedIds, String modelName) {
		Query originalObj = JPA.em().createQuery("SELECT self FROM " + modelName + " self WHERE self.id = :ids");
		originalObj.setParameter("ids", selectedIds.get(0));
		Object originalObjct = originalObj.getSingleResult();
		return originalObjct;
	}
	
	@Transactional
	public List<Object> getDuplicateObject(List<Long> selectedIds,String modelName) {
		Query duplicateObj = JPA.em().createQuery("SELECT self FROM " + modelName + " self WHERE self.id IN (:ids)");
		duplicateObj.setParameter("ids", selectedIds.subList(1, selectedIds.size()));
		List<Object> duplicateObjects = duplicateObj.getResultList();
		return duplicateObjects;
	}
	
	@Transactional
	public List<Object> getAllSelectedObject(List<Long> selectedIds,String modelName) {
		Query duplicateObj = JPA.em().createQuery("SELECT self FROM " + modelName + " self WHERE self.id IN (:ids)");
		duplicateObj.setParameter("ids", selectedIds);
		List<Object> duplicateObjects = duplicateObj.getResultList();
		return duplicateObjects;
	}
	
	@Transactional
	public Object getWizardValue(Long id, String modelName, String nameColumn) {
		Query selectedObj;
		if(nameColumn == null) {
			selectedObj = JPA.em().createQuery("SELECT self.id FROM " + modelName + " self WHERE self.id = :id");
		} else {
			selectedObj = JPA.em().createQuery("SELECT self.id ,self."+nameColumn+" FROM " + modelName + " self WHERE self.id = :id");
		}
		selectedObj.setParameter("id", id);
		Object selectedObject = selectedObj.getSingleResult();
		return selectedObject;
	}
	
	/*
	 * find duplicate records
	 */
	public String findDuplicateRecords(List<String> fieldList, String object, String selectedRecored, String filter) {

		List<List<String>> allRecords = getAllRecords(fieldList, object, selectedRecored, filter);

		Map<String, List<String>> recordMap = new HashMap<String, List<String>>();

		for (List<String> rec : allRecords) {

			List<String> record = new ArrayList<String>();
			for (String field : rec) {
				if (field != null) {
					record.add(StringTool.deleteAccent(field.toLowerCase()));
				}
			}

			String recId = record.get(0);
			record.remove(0);
			if (!record.isEmpty()) {
				recordMap.put(recId, record);
			}
		}

		Iterator<String> keys = recordMap.keySet().iterator();

		List<String> ids = getDuplicateIds(keys, recordMap, new ArrayList<String>());

		return Joiner.on(",").join(ids);
	}
	
	/*
	 * get all records for duplicate records
	 */
	@SuppressWarnings("unchecked")
	private List<List<String>> getAllRecords(List<String> fieldList,String object,String selectedRecored,String filter){
		
		String query = "SELECT new List( CAST ( self.id AS string )";
		
		for(String field : fieldList) {
			query += ", self." + field;
		}
		List<List<Object>> resultList = new ArrayList<>();
		if(selectedRecored == null || selectedRecored.isEmpty()) {
			if(filter.equals("null")) {
				resultList = JPA.em().createQuery(query + ") FROM " + object + " self").getResultList();
			} else {
				resultList = JPA.em().createQuery(query + ") FROM " + object + " self WHERE " + filter).getResultList();
			}
		} else {
			if(filter.equals("null")) {
				resultList = JPA.em().createQuery(query + ") FROM " + object + " self where self.id in("+selectedRecored+")").getResultList();
			} else {
				resultList = JPA.em().createQuery(query + ") FROM " + object + " self where self.id in("+selectedRecored+") AND " + filter).getResultList();
			}
		}
		
		List<List<String>> records = new ArrayList<List<String>>();
		
		for(List<Object> result : resultList){
			
			List<String> record = new ArrayList<String>();
			for(Object field : result){
				if(field == null){
					continue;
				}
				if(field instanceof Model){
					record.add(((Model)field).getId().toString());
				}
				else{
					record.add(field.toString());
				}
			}
			
			records.add(record);
		}
		
		return records;
	}
	
	/*
	 * get duplicate records id
	 */
	private List<String> getDuplicateIds(Iterator<String> keys, Map<String, List<String>> recordMap, List<String> ids) {

		if (!keys.hasNext()) {
			return ids;
		}

		String recId = keys.next();
		List<String> record = recordMap.get(recId);
		keys.remove();
		recordMap.remove(recId);

		Iterator<String> compareKeys = recordMap.keySet().iterator();

		while (compareKeys.hasNext()) {
			String compareId = compareKeys.next();
			List<String> value = recordMap.get(compareId);
			if (value.containsAll(record)) {
				ids.add(recId);
				ids.add(compareId);
				compareKeys.remove();
				recordMap.remove(compareId);
				keys = recordMap.keySet().iterator();
			}
		}

		return getDuplicateIds(keys, recordMap, ids);
	}
}
