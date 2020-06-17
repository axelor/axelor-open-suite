/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.service;

import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/** @author axelor */
public class ArchivingToolServiceImpl implements ArchivingToolService {

  /** */
  @Override
  public Map<String, String> getObjectLinkTo(Object object, Long id) throws AxelorException {
    Map<String, String> objectsLinkToMap = new HashMap<String, String>();
    Query FindModelWithobjectFieldQuery =
        JPA.em()
            .createNativeQuery(
                "SELECT field.name as fieldName, model.name as ModelName,field.relationship as relationship,field.mapped_by as mappedBy,model.table_name as tableName"
                    + " FROM meta_field field"
                    + " LEFT JOIN meta_model model on field.meta_model = model.id"
                    + " WHERE field.type_name like :objectName");
    FindModelWithobjectFieldQuery.setParameter("objectName", object.getClass().getSimpleName());
    List<Object[]> resultList = FindModelWithobjectFieldQuery.getResultList();
    for (Object[] result : resultList) {

      String fieldName = ((String) result[0]).replaceAll("([A-Z])", "_$1").toLowerCase();
      String modelName = (String) result[1];
      String modelNameBDDFormat =
          modelName.replaceAll("([A-Z])", "_$1").toLowerCase().replace("^_", "");
      String relationship = (String) result[2];
      String mappedBy = null;
      if (result[3] != null) {
        mappedBy = ((String) result[3]).replaceAll("([A-Z])", "_$1").toLowerCase();
      }
      String tableObjectLinkName = ((String) result[4]).toLowerCase().replace(" ", "_");
      String tableObjectName = this.getTableObjectName(object);

      Query findobjectQuery = null;
      if (relationship.equals("ManyToOne") || relationship.equals("OneToOne")) {
        findobjectQuery =
            JPA.em()
                .createNativeQuery(
                    "SELECT DISTINCT ol."
                        + fieldName
                        + " FROM "
                        + tableObjectLinkName
                        + " ol LEFT JOIN "
                        + tableObjectName
                        + " o ON ol."
                        + fieldName
                        + " = "
                        + "o.id"
                        + " WHERE o.id =  :objectId ");
        findobjectQuery.setParameter("objectId", id);
      } else if (result[3].equals("OneToMany")) {
        String manyToOneMappedfield = null;
        if (mappedBy != null && !mappedBy.isEmpty()) {
          manyToOneMappedfield = mappedBy;
        } else {
          manyToOneMappedfield = modelNameBDDFormat;
        }
        findobjectQuery =
            JPA.em()
                .createNativeQuery(
                    "SELECT DISTINCT ol."
                        + fieldName
                        + " FROM "
                        + tableObjectLinkName
                        + " ol LEFT JOIN "
                        + tableObjectName
                        + " o ON ol.id = o."
                        + manyToOneMappedfield
                        + " WHERE o.id =  :objectId ");
        findobjectQuery.setParameter("objectId", id);
      } else if (result[2].equals("ManyToMany")) {
        String tableNameSet = tableObjectLinkName + "_" + fieldName;
        findobjectQuery =
            JPA.em()
                .createNativeQuery(
                    "SELECT DISTINCT "
                        + fieldName
                        + " FROM "
                        + tableNameSet
                        + " WHERE "
                        + fieldName
                        + " = :objectId");
        findobjectQuery.setParameter("objectId", id);
      }

      if (findobjectQuery != null) {
        Object objectToCheck = null;
        try {
          objectToCheck = (Object) findobjectQuery.getSingleResult();
        } catch (NoResultException nRE) {
          // nothing to do
        }
        if (objectToCheck != null) {
          objectsLinkToMap.put(modelName, relationship);
        }
      }
    }
    return objectsLinkToMap;
  }

  protected String getTableObjectName(Object object) {
    String moduleName =
        object
            .getClass()
            .getPackage()
            .getName()
            .replace("com.axelor.apps.", "")
            .replace(".db", "")
            .replace(".", "_")
            .toLowerCase();
    String objectName =
        object.getClass().getSimpleName().replaceAll("([A-Z])", "_$1").toLowerCase();
    ;
    return moduleName + objectName;
  }

  @Override
  public String getModelTitle(String modelName) throws AxelorException {
    Query FindModelWithobjectFieldQuery =
        JPA.em()
            .createNativeQuery(
                "SELECT view.title as viewTitle"
                    + " FROM meta_view view"
                    + " WHERE view.name like :viewtName");
    FindModelWithobjectFieldQuery.setParameter(
        "viewtName", modelName.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase() + "-form");
    return (String) FindModelWithobjectFieldQuery.getSingleResult();
  }
}
