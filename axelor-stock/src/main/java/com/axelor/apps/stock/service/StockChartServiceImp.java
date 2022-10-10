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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.service.BaseChartService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.views.ChartView;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;

public class StockChartServiceImp implements StockChartService {
  @Inject MetaViewRepository metaViewRepo;
  @Inject AppBaseService appBaseService;
  @Inject UserService userService;
  @Inject BaseChartService baseChartService;

  protected static final String QUERY_PATTERN = "(?i)(moves)(.+?)(from)";

  @SuppressWarnings("static-access")
  protected String getQueryStr(Map<String, Object> context, ChartView chartView) {
    Map<String, String> chartConfigs = baseChartService.getConfigs(context, chartView);
    String whereFilter = String.format("%s = :chartFilterValue", chartConfigs.get("_filter"));
    String queryStr =
        StringUtils.normalizeSpace(chartView.getDataSet().getText().trim().replaceAll("\\n", " "))
            .replaceFirst(QUERY_PATTERN, String.format("$1 $3"))
            .replaceAll(
                baseChartService.QUERY_DATE_PATTERN,
                String.format("'%s'", appBaseService.getTodayDate(null)))
            .replaceAll(
                baseChartService.QUERY_DATE_TIME_PATTERN,
                String.format("'%s'", appBaseService.getTodayDateTime(null)))
            .trim()
            .concat(" WHERE " + whereFilter);
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
