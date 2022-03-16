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
package com.axelor.apps.base.service.wordreport.config;

import com.axelor.apps.base.db.ReportQueryBuilder;
import com.axelor.apps.base.db.ReportQueryBuilderParams;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.QueryBinder;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.persistence.Query;
import javax.script.ScriptException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.hibernate.transform.BasicTransformerAdapter;

public class WordReportQueryBuilderServiceImpl implements WordReportQueryBuilderService {

  @Inject private WordReportGroovyService groovyService;
  @Inject private MetaModelRepository metaModelRepository;
  @Inject private WordReportHelperService helperService;
  @Inject private WordReportTranslationService translationService;

  @Override
  public Map<String, List<Object>> getAllReportQueryBuilderResult(
      Set<ReportQueryBuilder> reportQueryBuilderList, Object bean) {
    Map<String, List<Object>> reportQueryBuilderResultMap = new HashMap<>();
    if (ObjectUtils.isEmpty(reportQueryBuilderList)) {
      return reportQueryBuilderResultMap;
    }
    String queryString = null;
    Map<String, Map<String, Object>> query = new HashMap<>();
    for (ReportQueryBuilder rqb : reportQueryBuilderList) {
      queryString = rqb.getQueryText();
      Map<String, Object> context = new HashMap<>();
      if (ObjectUtils.notEmpty(rqb.getReportQueryBuilderParamsList())) {

        for (ReportQueryBuilderParams params : rqb.getReportQueryBuilderParamsList()) {
          String expression = params.getValue();
          Object value = null;
          if (expression.trim().startsWith("eval:")) {
            value = groovyService.validateCondition(expression, bean);
          } else {
            value = expression;
          }
          context.put(params.getName(), value);
        }
      }
      // add select query according to metaModel
      queryString = getQueryString(queryString, rqb);

      query.put(queryString, context);
      reportQueryBuilderResultMap.put(rqb.getVar(), this.getResult(query));
    }
    return reportQueryBuilderResultMap;
  }

  @Override
  public void setReportQueryTextValue(
      Text text,
      List<Object> collection,
      String value,
      String operationString,
      ResourceBundle resourceBundle)
      throws ClassNotFoundException, ScriptException {
    if (ObjectUtils.notEmpty(collection) && collection.size() == 1) {
      Triple<String, String, Pair<Mapper, Boolean>> modelAliasFieldNameIsModelTriple =
          this.getCollectionInfo(collection, value);
      boolean isModel = modelAliasFieldNameIsModelTriple.getRight().getRight();
      text.setValue(
          this.getKeyValue(
              resourceBundle,
              collection.get(0),
              modelAliasFieldNameIsModelTriple.getRight().getLeft(),
              Triple.of(
                  operationString,
                  modelAliasFieldNameIsModelTriple.getLeft(),
                  modelAliasFieldNameIsModelTriple.getMiddle()),
              isModel));
    }
  }

  @Override
  public List<String> getReportQueryColumnData(
      Tbl table,
      String value,
      List<Object> collection,
      String operationString,
      ResourceBundle resourceBundle)
      throws ClassNotFoundException, ScriptException {
    List<String> columnData = new ArrayList<>();
    Triple<String, String, Pair<Mapper, Boolean>> modelAliasFieldNameIsModelTriple =
        this.getCollectionInfo(collection, value);
    boolean isModel = modelAliasFieldNameIsModelTriple.getRight().getRight();

    for (Object ob : collection) {
      columnData.add(
          this.getKeyValue(
              resourceBundle,
              ob,
              modelAliasFieldNameIsModelTriple.getRight().getLeft(),
              Triple.of(
                  operationString,
                  modelAliasFieldNameIsModelTriple.getLeft(),
                  modelAliasFieldNameIsModelTriple.getMiddle()),
              isModel));
    }
    return columnData;
  }

  @SuppressWarnings("unchecked")
  private Triple<String, String, Pair<Mapper, Boolean>> getCollectionInfo(
      List<Object> collection, String value) throws ClassNotFoundException {
    String modelFullName =
        ((LinkedHashMap<String, Object>) collection.get(0))
            .values()
            .iterator()
            .next()
            .getClass()
            .getName();
    boolean isModel =
        metaModelRepository.all().filter("self.fullName = ?1", modelFullName).count() > 0;
    String fieldName = value.substring(value.indexOf(".") + 1);

    String className = null;
    Mapper mapper = null;
    String modelAlias = null;

    if (isModel) {
      modelAlias = ((LinkedHashMap<String, Object>) collection.get(0)).keySet().iterator().next();
      className =
          ((LinkedHashMap<String, Object>) collection.get(0)).get(modelAlias).getClass().getName();
      mapper = helperService.getMapper(className);
    }
    return Triple.of(modelAlias, fieldName, Pair.of(mapper, isModel));
  }

  private String getQueryString(String queryString, ReportQueryBuilder rqb) {
    if (StringUtils.isEmpty(queryString) && ObjectUtils.notEmpty(rqb.getMetaModel())) {
      queryString = String.format("SELECT self as self FROM %s self", rqb.getMetaModel().getName());
    } else if (StringUtils.notEmpty(queryString)
        && ObjectUtils.notEmpty(rqb.getMetaModel())
        && (!queryString.contains("Select") && !queryString.contains("SELECT"))) {
      queryString =
          String.format(
              "SELECT self as self FROM %s self where %s",
              rqb.getMetaModel().getName(), queryString);
    } else if ((StringUtils.isEmpty(queryString) && ObjectUtils.isEmpty(rqb.getMetaModel()))
        || (StringUtils.notEmpty(queryString)
            && (!queryString.contains("Select") && !queryString.contains("SELECT"))
            && ObjectUtils.isEmpty(rqb.getMetaModel()))) {
      queryString = "";
    }
    return queryString;
  }

  @SuppressWarnings("unchecked")
  private List<Object> getResult(Map<String, Map<String, Object>> reportQuery) {

    String queryString = reportQuery.keySet().stream().findFirst().get();
    Map<String, Object> context = reportQuery.get(queryString);
    if (queryString.isEmpty()) {
      return new ArrayList<>();
    }
    Query query = JPA.em().createQuery(queryString);
    query.unwrap(org.hibernate.query.Query.class).setResultTransformer(new DataSetTransformer());
    QueryBinder.of(query).bind(context);
    return query.getResultList();
  }

  private String getKeyValue(
      ResourceBundle resourceBundle,
      Object ob,
      Mapper mapper,
      Triple<String, String, String> operationStringModelAliasFieldName,
      boolean isModel)
      throws ScriptException {
    Object keyValue = "";
    Object value =
        this.getValue(
            ob,
            mapper,
            operationStringModelAliasFieldName.getMiddle(),
            operationStringModelAliasFieldName.getRight(),
            isModel);

    if (ObjectUtils.isEmpty(value)) {
      keyValue = "";
    } else if (value.getClass() == LocalDate.class || value.getClass() == LocalDateTime.class) {
      keyValue = helperService.getDateTimeFormat(value);
    } else {
      keyValue = value;
    }

    if (StringUtils.notEmpty(operationStringModelAliasFieldName.getLeft())) {
      keyValue =
          groovyService.calculateFromString(
              keyValue.toString().concat(operationStringModelAliasFieldName.getLeft()),
              helperService.getBigDecimalScale());
    }

    return translationService.getValueTranslation(keyValue.toString(), resourceBundle);
  }

  @SuppressWarnings("unchecked")
  private Object getValue(
      Object ob, Mapper mapper, String modelAlias, String fieldName, boolean isModel) {
    Object value = null;
    if (isModel) {
      ImmutablePair<Property, Object> resultPair =
          helperService.findField(
              mapper, ((LinkedHashMap<String, Object>) ob).get(modelAlias), fieldName);
      value =
          ObjectUtils.notEmpty(resultPair)
                  && ObjectUtils.notEmpty(resultPair.getRight())
                  && ObjectUtils.notEmpty(resultPair.getLeft())
              ? resultPair.getLeft().get(resultPair.getRight())
              : "";
    } else {
      Map<String, String> recordMap = (LinkedHashMap<String, String>) ob;
      value = recordMap.get(fieldName);
    }
    return value;
  }

  @SuppressWarnings("serial")
  private static final class DataSetTransformer extends BasicTransformerAdapter {

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
      Map<String, Object> result = new LinkedHashMap<>(tuple.length);
      for (int i = 0; i < tuple.length; ++i) {
        String alias = aliases[i];
        if (alias != null) {
          result.put(alias, tuple[i]);
        }
      }
      return result;
    }
  }
}
