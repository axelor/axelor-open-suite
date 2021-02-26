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
import com.axelor.inject.Beans;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptException;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class ReportQueryBuilderServiceImpl implements ReportQueryBuilderService {

  public Map<String, Map<String, Object>> getReportQueryBuilderQuery(
      List<ReportQueryBuilder> reportQueryBuilderList, String propertyName, Object bean) {
    String queryString = null;
    Map<String, Map<String, Object>> query = new HashMap<>();
    for (ReportQueryBuilder rqb : reportQueryBuilderList) {
      if (rqb.getVar().equals(propertyName.substring(0, propertyName.indexOf(".")))) {
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
        query.put(queryString, context);
        break;
      }
    }
    return query;
  }

  @Override
  public ImmutablePair<Integer, Map<Integer, Map<String, Object>>>
      getReportQueryBuilderCollectionEntry(ReportParameterVariables reportVariables)
          throws ScriptException {

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
    if (!reportVariables.getCollection().isEmpty()) {

      for (Object ob : reportVariables.getCollection()) {
        Map<String, Object> newMap = new HashMap<>();

        newMap.putAll(newEntryValueMap);
        newMap.replace(
            ExcelReportConstants.KEY_ROW,
            reportVariables.getRowNumber() + rowOffset + localMergeOffset);

        Map<String, String> recordMap = (LinkedHashMap<String, String>) ob;
        Object value = recordMap.get(reportVariables.getKey());
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

        if (reportVariables.isTranslate()) {
          keyValue =
              Beans.get(ExcelReportTranslationService.class)
                  .getTranslatedValue(keyValue, reportVariables.getPrintTemplate());
        }
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
}
