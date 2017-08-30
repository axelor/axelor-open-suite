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
package com.axelor.apps.base.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class DuplicateObjectsService {
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private EntityManager em;
	
	@Transactional
	public void removeDuplicate(List<Long> selectedIds, String modelName) {
		
		List<Object> duplicateObjects = getDuplicateObject(selectedIds, modelName);
		Object originalObjct = getOriginalObject(selectedIds,modelName);
		List<MetaField> allField = metaFieldRepo.all().filter("(relationship = 'ManyToOne' AND typeName = ?1) OR (relationship = 'ManyToMany' AND (typeName = ?1 OR metaModel.name =?1))",modelName).fetch();
		for (MetaField metaField : allField) {
			if ("ManyToOne".equals(metaField.getRelationship())) {
					Query update = em.createQuery("UPDATE " + metaField.getMetaModel().getFullName() + " self SET self." + metaField.getName() + " = :value WHERE self." + metaField.getName() + " in (:duplicates)");
					update.setParameter("value", originalObjct);
					update.setParameter("duplicates", duplicateObjects);
					update.executeUpdate();
			} else if ("ManyToMany".equals(metaField.getRelationship())) {
				
				if(metaField.getTypeName().equals(modelName)) {      
					Query select = em.createQuery("select self from " + metaField.getMetaModel().getFullName() + " self LEFT JOIN self." + metaField.getName() + " as x WHERE x IN (:ids)");
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
		em.flush();
		em.clear();
		for(Object obj : getDuplicateObject(selectedIds, modelName)) {
			em.remove(obj);
		
		}
	}
	
	@Transactional
	public Object getOriginalObject(List<Long> selectedIds, String modelName) {
		Query originalObj = em.createQuery("SELECT self FROM " + modelName + " self WHERE self.id = :ids");
		originalObj.setParameter("ids", selectedIds.get(0));
		Object originalObjct = originalObj.getSingleResult();
		return originalObjct;
	}
	
	@Transactional
	public List<Object> getDuplicateObject(List<Long> selectedIds,String modelName) {
		Query duplicateObj = em.createQuery("SELECT self FROM " + modelName + " self WHERE self.id IN (:ids)");
		duplicateObj.setParameter("ids", selectedIds.subList(1, selectedIds.size()));
		List<Object> duplicateObjects = duplicateObj.getResultList();
		return duplicateObjects;
	}
	
	@Transactional
	public List<Object> getAllSelectedObject(List<Long> selectedIds,String modelName) {
		Query duplicateObj = em.createQuery("SELECT self FROM " + modelName + " self WHERE self.id IN (:ids)");
		duplicateObj.setParameter("ids", selectedIds);
		List<Object> duplicateObjects = duplicateObj.getResultList();
		return duplicateObjects;
	}
	
	@Transactional
	public Object getWizardValue(Long id, String modelName, String nameColumn) {
		Query selectedObj;
		if(nameColumn == null) {
			selectedObj = em.createQuery("SELECT self.id FROM " + modelName + " self WHERE self.id = :id");
		} else {
			selectedObj = em.createQuery("SELECT self.id ,self."+nameColumn+" FROM " + modelName + " self WHERE self.id = :id");
		}
		selectedObj.setParameter("id", id);
		Object selectedObject = selectedObj.getSingleResult();
		return selectedObject;
	}
}
