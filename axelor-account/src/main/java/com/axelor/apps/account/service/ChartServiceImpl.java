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
package com.axelor.apps.account.service;

import com.axelor.apps.base.service.BaseChartService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.views.ChartView;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;

public class ChartServiceImpl implements ChartService {
  @Inject MetaViewRepository metaViewRepo;
  @Inject AppBaseService appBaseService;
  @Inject UserService userService;
  @Inject BaseChartService baseChartService;

  protected static final String SUB_QUERY_PATTERN = "(?i)(\\(select)(.+?)(self.)(.+?)(from)";
  protected static final String QUERY_GROUP_BY_PATTERN = "(?i)(\\))(?!.*\\))(.+)(group By)(.*)";

  @SuppressWarnings("static-access")
  protected String getQueryStr(Map<String, Object> context, ChartView chartView) {
    Map<String, String> chartConfigs = baseChartService.getConfigs(context, chartView);
    String whereFilter = String.format("%s = :chartFilterValue", chartConfigs.get("_filter"));
    String queryStr =
        StringUtils.normalizeSpace(chartView.getDataSet().getText().trim().replaceAll("\\n", " "))
            .trim();
    boolean isMatcher = Pattern.compile(SUB_QUERY_PATTERN).matcher(queryStr).find() ? true : false;
    boolean isConfig = chartConfigs.get("isConfig") != null ? true : false;
    if (!isMatcher && !isConfig) {
      queryStr =
          queryStr
              .replaceAll(
                  QUERY_GROUP_BY_PATTERN,
                  String.format("$1 %s where %s ", chartConfigs.get("_selectFilter"), whereFilter))
              .replaceAll("_amount", "id");
    } else if (isConfig) {
      queryStr =
          "select \n"
              + "from account_invoice self\n"
              + "join auth_user AS _user ON _user.id = :__user__\n"
              + "join base_company as _company\n"
              + "ON _company.id = _user.active_company\n"
              + "AND _company.id = self.company\n"
              + "where self.status_select = 3 AND self.invoice_date BETWEEN DATE(:fromDate) AND DATE(:toDate)\n"
              + "and to_char(self.invoice_date,'yyyy-MM') = :chartFilterValue\n"
              + "GROUP by self.id";
      queryStr = StringUtils.normalizeSpace(queryStr.replaceAll("\\n", " ")).trim();
    } else {
      queryStr =
          queryStr
              .replaceAll(SUB_QUERY_PATTERN, String.format("$1$2$3id $5"))
              .replaceAll(
                  QUERY_GROUP_BY_PATTERN,
                  String.format(
                      "$1 %s where %s $3 %s.id",
                      chartConfigs.get("_selectFilter"),
                      whereFilter,
                      chartConfigs.get("_selectFilter")));
    }
    queryStr =
        queryStr
            .replaceFirst(
                baseChartService.QUERY_PATTERN,
                String.format("$1 %s.id $3", chartConfigs.get("_selectFilter")))
            .replaceAll(
                baseChartService.QUERY_DATE_PATTERN,
                String.format("'%s'", appBaseService.getTodayDate(null)))
            .replaceAll(
                baseChartService.QUERY_DATE_TIME_PATTERN,
                String.format("'%s'", appBaseService.getTodayDateTime(null)));
    return queryStr;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public List<Long> getIdList(Map<String, Object> context, ChartView chartView) {
    String queryStr = getQueryStr(context, chartView);
    Query query = baseChartService.setParamValue(context, chartView, queryStr);
    if (query != null) {
      return query.getResultList();
    }

    return new ArrayList<>();
  }
}
