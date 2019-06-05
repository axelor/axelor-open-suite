/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DuplicateObjectsService {

  private final Logger log = LoggerFactory.getLogger(DuplicateObjectsService.class);

  @Inject private MetaFieldRepository metaFieldRepo;

  @Transactional
  public void removeDuplicate(Long originalId, List<Long> duplicateIds, Class<?> modelName) {
    List<Object> duplicateObjects = this.getDuplicateObjects(duplicateIds, modelName);
    Object originalObject = JPA.em().find(modelName, originalId);

    Mapper mapper = Mapper.of(originalObject.getClass());

    for (MetaField metaField : this.getAllRelationalFields(modelName)) {
      if ("ManyToOne".equals(metaField.getRelationship())) {

        this.updateManyToOneField(metaField, originalObject, duplicateObjects);

      } else {

        this.updateManyToManyField(metaField, originalObject, duplicateObjects, mapper);
      }
    }

    JPA.em().flush();
    JPA.em().clear();

    for (Object obj : this.getDuplicateObjects(duplicateIds, modelName)) {
      JPA.em().remove(obj);
    }
  }

  @Transactional
  private List<Object> getDuplicateObjects(List<Long> duplicateIds, Class<?> modelName) {
    Query query =
        JPA.em()
            .createQuery(
                "SELECT self FROM " + modelName.getSimpleName() + " self WHERE self.id IN (:ids)");
    query.setParameter("ids", duplicateIds);
    return query.getResultList();
  }

  public List<?> findDuplicatedRecordIds(Set<String> fieldSet, Class<?> modelClass, String filter)
      throws AxelorException {

    if (fieldSet == null || fieldSet.isEmpty()) {
      return null;
    }

    String concatedFields = concatFields(modelClass, fieldSet);

    String subQuery = createSubQuery(modelClass, filter, concatedFields);
    log.debug("Duplicate check subquery: {}", concatedFields);

    return fetchDuplicatedRecordIds(modelClass, concatedFields, subQuery);
  }

  private String concatFields(Class<?> modelClass, Set<String> fieldSet) throws AxelorException {

    StringBuilder concatField = new StringBuilder("concat(");
    Mapper mapper = Mapper.of(modelClass);

    for (String field : fieldSet) {
      Property property = mapper.getProperty(field);
      if (property == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.GENERAL_8),
            field,
            modelClass.getSimpleName());
      }
      if (property.isCollection()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.GENERAL_9),
            field);
      }

      int count = 0;
      if (count != 0) {
        concatField.append(",");
      }
      count++;

      concatField.append("cast(self" + "." + field);

      if (property.getTarget() != null) {
        concatField.append(".id");
      }
      concatField.append(" as string)");
    }

    concatField.append(")");

    return concatField.toString();
  }

  private String createSubQuery(Class<?> modelClass, String filter, String concatedFields) {

    StringBuilder queryBuilder = new StringBuilder("SELECT ");
    queryBuilder.append(concatedFields);
    queryBuilder.append(" FROM ");
    queryBuilder.append(modelClass.getSimpleName() + " self");
    if (filter != null) {
      queryBuilder.append(" WHERE " + filter);
    }
    queryBuilder.append(" GROUP BY ");
    queryBuilder.append(concatedFields);
    queryBuilder.append(" HAVING COUNT(self) > 1");

    return queryBuilder.toString();
  }

  private List<?> fetchDuplicatedRecordIds(
      Class<?> modelClass, String concatedFields, String subQuery) {

    StringBuilder queryBuilder = new StringBuilder("SELECT self.id FROM ");
    queryBuilder.append(modelClass.getSimpleName() + " self");
    queryBuilder.append(" WHERE ");
    queryBuilder.append(concatedFields);
    queryBuilder.append(" IN ");
    queryBuilder.append("(" + subQuery + ")");

    List<?> recordIds = JPA.em().createQuery(queryBuilder.toString()).getResultList();

    return recordIds;
  }

  private List<MetaField> getAllRelationalFields(Class<?> modelName) {
    return metaFieldRepo
        .all()
        .filter(
            "(relationship = 'ManyToOne' AND typeName = ?1) OR (relationship = 'ManyToMany' AND (typeName = ?1 OR metaModel.name =?1))",
            modelName.getSimpleName())
        .fetch();
  }

  private void updateManyToOneField(
      MetaField metaField, Object originalObject, List<Object> duplicateObjects) {
    Query update =
        JPA.em()
            .createQuery(
                "UPDATE "
                    + metaField.getMetaModel().getFullName()
                    + " self SET self."
                    + metaField.getName()
                    + " = :value WHERE self."
                    + metaField.getName()
                    + " in (:duplicates)");
    update.setParameter("value", originalObject);
    update.setParameter("duplicates", duplicateObjects);
    update.executeUpdate();
  }

  private void removeDuplicateFromList(
      MetaField metaField, Object originalObject, List<Object> duplicateObjects) {

    Query select =
        JPA.em()
            .createQuery(
                "select self from "
                    + metaField.getMetaModel().getFullName()
                    + " self LEFT JOIN self."
                    + metaField.getName()
                    + " as x WHERE x IN (:ids)");
    select.setParameter("ids", duplicateObjects);

    for (Object object : select.getResultList()) {
      Set<Object> items =
          (Set<Object>) Mapper.of(object.getClass()).get(object, metaField.getName());
      items.removeAll(duplicateObjects);
      items.add(originalObject);
    }
  }

  private void addToExistRelationalFields(
      Mapper mapper, List<Object> duplicateObjects, String metaFieldName, Object originalObject) {

    Set<Object> existRelationalFields = (Set<Object>) mapper.get(originalObject, metaFieldName);

    for (Object object : duplicateObjects) {
      Set<Object> newRelationalFields = (Set<Object>) mapper.get(object, metaFieldName);
      if (newRelationalFields != null) {
        existRelationalFields.addAll(newRelationalFields);
        mapper.set(object, metaFieldName, new HashSet<>());
      }
    }
  }

  private void updateManyToManyField(
      MetaField metaField, Object originalObject, List<Object> duplicateObjects, Mapper mapper) {
    if (metaField.getTypeName().equals(mapper.getBeanClass().getSimpleName())) {

      this.removeDuplicateFromList(metaField, originalObject, duplicateObjects);
    }

    this.addToExistRelationalFields(mapper, duplicateObjects, metaField.getName(), originalObject);
  }
}
