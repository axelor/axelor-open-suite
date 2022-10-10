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
package com.axelor.apps.base.service;

import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.views.ChartView;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;

public interface BaseChartService {
  public static final String QUERY_PATTERN = "(?i)(select)(.+?)(from)";
  public static final String QUERY_GROUP_BY_PATTERN = "(?i)(group by)(?!.*(group by))(.+)";
  public static final String QUERY_ORDER_BY_PATTERN = "(?i)(order by)(?!.*(order by))(.+)";
  public static final String QUERY_DATE_PATTERN = ":__date__";
  public static final String QUERY_DATE_TIME_PATTERN = ":__datetime__";
  public static final String QUERY_USER_PATTERN = "(?i)(\\s=\\s)(:_user_id|:__user__)";

  public List<Long> getIdList(Map<String, Object> context, ChartView chartView);

  public Map<String, String> getConfigs(Map<String, Object> context, ChartView chartView);

  public Query setParamValue(Map<String, Object> context, ChartView chartView, String queryStr);

  public ActionViewBuilder getActionView(ChartView chartView);
}
