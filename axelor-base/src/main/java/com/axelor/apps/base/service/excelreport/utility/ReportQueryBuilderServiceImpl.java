/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.excelreport.utility;

import com.axelor.apps.base.db.ReportQueryBuilder;
import com.axelor.apps.base.db.ReportQueryBuilderParams;
import com.axelor.apps.base.service.excelreport.config.ExcelReportConstants;
import com.axelor.apps.base.service.excelreport.config.ExcelReportHelperService;
import com.axelor.apps.base.service.excelreport.config.ReportParameterVariables;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.QueryBinder;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.inject.Beans;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import javax.script.ScriptException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hibernate.transform.BasicTransformerAdapter;

public class ReportQueryBuilderServiceImpl implements ReportQueryBuilderService {

  @Override
  public Map<String, List<Object>> getAllReportQueryBuilderResult(
      List<ReportQueryBuilder> reportQueryBuilderList, Object bean) {
    Map<String, List<Object>> reportQueryBuilderResultMap = new HashMap<>();

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
            value =
                Beans.get(ExcelReportGroovyService.class)
                    .validateCondition(expression, bean)
                    .toString();
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
  public ImmutablePair<Integer, Map<Integer, Map<String, Object>>>
      getReportQueryBuilderCollectionEntry(ReportParameterVariables reportVariables)
          throws ScriptException, ClassNotFoundException {

    boolean isFirstIteration = true;

    if (reportVariables.isHide()) {
      reportVariables.getRemoveCellKeyList().add(reportVariables.getEntry().getKey());
    }

    Map<String, Object> newEntryValueMap = new HashMap<>(reportVariables.getEntryValueMap());
    Beans.get(ExcelReportShiftingService.class)
        .shiftRows(
            newEntryValueMap,
            reportVariables.getTotalRecord(),
            reportVariables.getCollectionEntryRow(),
            reportVariables.getMergedCellsRangeAddressList(),
            reportVariables.getMergedCellsRangeAddressSetPerSheet());

    reportVariables.setRowNumber((int) newEntryValueMap.get(ExcelReportConstants.KEY_ROW));

    int localMergeOffset = 0;
    int rowOffset = 0;
    String fieldName = reportVariables.getFieldName().trim();
    boolean isModel = reportVariables.isModel();
    String className = null;
    Mapper mapper = null;
    String modelAlias = null;

    if (!reportVariables.getCollection().isEmpty()) {
      List<Object> collectionList = (List<Object>) reportVariables.getCollection();

      if (isModel) {
        modelAlias =
            ((LinkedHashMap<String, Object>) collectionList.get(0)).keySet().iterator().next();
        className =
            ((LinkedHashMap<String, Object>)
                    ((List<Object>) reportVariables.getCollection()).get(0))
                .get(modelAlias)
                .getClass()
                .getName();
        mapper = Beans.get(ExcelReportHelperService.class).getMapper(className);
      }
      for (Object ob : reportVariables.getCollection()) {
        Map<String, Object> newMap = new HashMap<>();

        newMap.putAll(newEntryValueMap);
        newMap.replace(
            ExcelReportConstants.KEY_ROW,
            reportVariables.getRowNumber() + rowOffset + localMergeOffset);

        Object value = null;
        if (isModel) {
          ImmutablePair<Property, Object> resultPair =
              Beans.get(ExcelReportHelperService.class)
                  .findField(
                      mapper, ((LinkedHashMap<String, Object>) ob).get(modelAlias), fieldName);
          value =
              ObjectUtils.notEmpty(resultPair)
                  ? resultPair.getLeft().get(resultPair.getRight())
                  : "";
        } else {
          Map<String, String> recordMap = (LinkedHashMap<String, String>) ob;
          value = recordMap.get(fieldName);
        }

        Object keyValue = "";
        if (ObjectUtils.isEmpty(value) || reportVariables.isHide()) {
          keyValue = "";
        } else {
          keyValue = value;
        }

        if (StringUtils.notEmpty(reportVariables.getOperationString())) {
          keyValue =
              Beans.get(ExcelReportGroovyService.class)
                  .calculateFromString(
                      keyValue.toString().concat(reportVariables.getOperationString()),
                      Beans.get(ExcelReportHelperService.class).getBigDecimalScale());
        }

        keyValue =
            Beans.get(ExcelReportTranslationService.class)
                .getTranslatedValue(keyValue, reportVariables.getPrintTemplate());

        newMap.replace(ExcelReportConstants.KEY_VALUE, keyValue);

        while (reportVariables.getOutputMap().containsKey(reportVariables.getIndex()))
          reportVariables.setIndex(reportVariables.getIndex() + 1);
        if (isFirstIteration) {
          reportVariables.setIndex(reportVariables.getEntry().getKey());
          isFirstIteration = false;
        }

        reportVariables.getOutputMap().put(reportVariables.getIndex(), newMap);
        reportVariables.setIndex(reportVariables.getIndex() + 1);

        rowOffset = rowOffset + localMergeOffset + 1;
        if (localMergeOffset == 0 && reportVariables.getMergeOffset() != 0)
          localMergeOffset = reportVariables.getMergeOffset();
      }
      if (reportVariables.getRecord() == 0) reportVariables.setRecord(rowOffset - 1);
    } else {

      newEntryValueMap.replace(ExcelReportConstants.KEY_VALUE, "");
      reportVariables.getOutputMap().put(reportVariables.getEntry().getKey(), newEntryValueMap);
    }
    if (!reportVariables.isNextRowCheckActive()) reportVariables.setNextRowCheckActive(true);

    return ImmutablePair.of(reportVariables.getTotalRecord(), reportVariables.getOutputMap());
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
