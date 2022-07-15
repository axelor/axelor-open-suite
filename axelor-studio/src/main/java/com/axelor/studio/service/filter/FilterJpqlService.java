/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.filter;

import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.Filter;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service class use to generate groovy expression from chart filters.
 *
 * @author axelor
 */
public class FilterJpqlService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private FilterCommonService filterCommonService;

  @Inject private FilterSqlService filterSqlService;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  public String getJpqlFilters(List<Filter> filterList) {

    String filters = null;

    if (filterList == null) {
      return filters;
    }

    for (Filter filter : filterList) {

      MetaField field = filter.getMetaField();

      if (filter.getValue() != null) {
        String value = filter.getValue();
        value = value.replaceAll("\"", "");
        value = value.replaceAll("'", "");

        if (filter.getOperator().contains("like") && !value.contains("%")) {
          value = "%" + value + "%";
        }
        filter.setValue("'" + value + "'");
      }
      String relationship = field.getRelationship();
      String fieldName =
          relationship != null ? filter.getTargetField() : filter.getMetaField().getName();
      fieldName = "self." + fieldName;
      String fieldValue;
      if (filter.getTargetType().equals("String")) {
        fieldName = "LOWER(" + fieldName + ")";
        fieldValue = "LOWER(" + filter.getValue() + ")";
      } else {
        fieldValue = filter.getValue();
      }
      String condition =
          filterCommonService.getCondition(fieldName, filter.getOperator(), fieldValue);

      if (filters == null) {
        filters = condition;
      } else {
        String opt = filter.getLogicOp() != null && filter.getLogicOp() == 0 ? " AND " : " OR ";
        filters = filters + opt + condition;
      }
    }

    log.debug("JPQL filter: {}", filters);
    return filters;
  }

  public String getJsonJpql(MetaJsonField jsonField) {

    switch (jsonField.getType()) {
      case "integer":
        return "json_extract_integer";
      case "decimal":
        return "json_extract_decimal";
      case "boolean":
        return "json_extract_boolean";
      default:
        return "json_extract";
    }
  }

  public String getJpqlSearchFieldType(String type) {
    switch (type) {
      case "MANY_TO_ONE":
      case "many-to-one":
        return "string";

      case "ONE_TO_ONE":
      case "one-to-one":
        return "string";

      default:
        return type.toLowerCase();
    }
  }

  public Object parseMetaField(
      MetaField field, String target, List<String> joins, StringBuilder parent, boolean checkJson)
      throws AxelorException {

    if (target == null || !target.contains(".")) {
      if (field.getRelationship() != null && joins != null) {
        target = filterSqlService.getDefaultTarget(field.getName(), field.getTypeName())[0];
      } else {
        return field;
      }
    }

    target = target.substring(target.indexOf(".") + 1);
    String targetName = target.contains(".") ? target.substring(0, target.indexOf(".")) : target;
    if (field.getRelationship() == null) {
      return field;
    }

    MetaModel model = metaModelRepo.findByName(field.getTypeName());
    MetaField subMeta = filterSqlService.findMetaField(targetName, model.getFullName());
    if (subMeta != null) {
      if (joins != null) {
        addJoin(field, joins, parent);
      }
      return parseMetaField(subMeta, target, joins, parent, checkJson);

    } else if (checkJson) {
      MetaJsonField subJson = filterSqlService.findJsonField(targetName, model.getName());
      if (subJson != null) {
        if (joins != null) {
          addJoin(field, joins, parent);
        }
        return parseJsonField(subJson, target, joins, parent);
      }
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_MISSING_FIELD,
        "No sub field found field: %s model: %s ",
        targetName,
        model.getFullName());
  }

  public Object parseJsonField(
      MetaJsonField field, String target, List<String> joins, StringBuilder parent)
      throws AxelorException {

    log.debug("Parse json target: {}", target);

    if (target == null || !target.contains(".")) {
      if (field.getTargetJsonModel() != null && joins != null) {
        target =
            filterSqlService.getDefaultTargetJson(field.getName(), field.getTargetJsonModel())[0];
      } else if (field.getTargetModel() != null && joins != null) {
        target = filterSqlService.getDefaultTarget(field.getName(), field.getTargetModel())[0];
      } else {
        return field;
      }
    }

    target = target.substring(target.indexOf(".") + 1);

    String targetName = target.contains(".") ? target.substring(0, target.indexOf(".")) : target;
    if (field.getTargetJsonModel() == null && field.getTargetModel() == null) {
      return field;
    }

    if (field.getTargetJsonModel() != null) {
      MetaJsonField subJson =
          metaJsonFieldRepo
              .all()
              .filter(
                  "self.name = ?1 and self.jsonModel = ?2", targetName, field.getTargetJsonModel())
              .fetchOne();
      if (subJson != null) {
        if (joins != null) {
          addJoin(field, joins, parent);
        }
        return parseJsonField(subJson, target, joins, parent);
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          "No sub field found model: %s field %s ",
          field.getTargetJsonModel().getName(),
          targetName);
    } else {
      MetaField subMeta = filterSqlService.findMetaField(targetName, field.getTargetModel());
      if (subMeta != null) {
        if (joins != null) {
          addJoin(field, joins, parent);
        }
        return parseMetaField(subMeta, target, joins, parent, true);
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          "No sub field found model: %s field %s ",
          field.getTargetModel(),
          targetName);
    }
  }

  private void addJoin(MetaField field, List<String> joins, StringBuilder parent) {
    String parentField = field.getName();
    joins.add("LEFT JOIN " + parent.toString() + "." + parentField + " " + "obj" + joins.size());
    parent.replace(0, parent.length(), "obj" + (joins.size() - 1));
  }

  private void addJoin(MetaJsonField field, List<String> joins, StringBuilder parent) {

    String targetModel = null;
    if (field.getTargetModel() != null) {
      targetModel = field.getTargetModel();
    } else if (field.getTargetJsonModel() != null) {
      targetModel = MetaJsonRecord.class.getName();
    }

    MetaModel metaModel = metaModelRepo.all().filter("self.fullName = ?1", targetModel).fetchOne();

    joins.add(
        "LEFT JOIN "
            + metaModel.getName()
            + " "
            + "obj"
            + joins.size()
            + " ON (obj"
            + joins.size()
            + ".id = "
            + "cast(json_extract_integer("
            + parent
            + "."
            + filterSqlService.getColumn(field.getModel(), field.getModelField())
            + ", '"
            + field.getName()
            + "', 'id') as integer))");

    parent.replace(0, parent.length(), "obj" + (joins.size() - 1));
  }

  public String[] getJpqlField(Object target, String source, List<String> joins) {

    String field = null;
    String type = null;
    String selection = null;

    if (target instanceof MetaField) {
      MetaField metaField = (MetaField) target;
      field = source + "." + metaField.getName();
      type = metaField.getTypeName();
      try {
        selection =
            Mapper.of(Class.forName(metaField.getMetaModel().getFullName()))
                .getProperty(metaField.getName())
                .getSelection();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      MetaJsonField metaJsonField = (MetaJsonField) target;
      selection = metaJsonField.getSelection();
      String jsonColumn =
          filterSqlService.getColumn(metaJsonField.getModel(), metaJsonField.getModelField());
      String jsonType = getJsonJpql(metaJsonField);
      field = jsonType + "(" + source + "." + jsonColumn + ", '" + metaJsonField.getName() + "')";
      type = metaJsonField.getType();
    }

    log.debug("Selection for field :{} : {}", field, selection);
    if (joins != null && !Strings.isNullOrEmpty(selection)) {
      int join = joins.size();
      joins.add(
          "LEFT JOIN MetaSelect obj" + join + " on (obj" + join + ".name = '" + selection + "')");
      join = joins.size();
      joins.add(
          "LEFT JOIN MetaSelectItem obj"
              + join
              + " ON (obj"
              + join
              + ".select = obj"
              + (join - 1)
              + ".id and obj"
              + join
              + ".value = cast("
              + field
              + " as text))");
      join = joins.size();
      joins.add(
          "LEFT JOIN MetaTranslation obj"
              + join
              + " ON (obj"
              + join
              + ".key = obj"
              + (join - 1)
              + ".title and obj"
              + join
              + ".language = (SELECT language FROM User WHERE id = :__user__))");
      field = "COALESCE(nullif(obj" + join + ".message,''), obj" + (join - 1) + ".title)";
    }

    return new String[] {field, type};
  }
}
