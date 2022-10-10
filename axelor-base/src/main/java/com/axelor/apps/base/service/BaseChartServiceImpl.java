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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.views.ChartView;
import com.axelor.meta.schema.views.ChartView.ChartCategory;
import com.axelor.meta.schema.views.ChartView.ChartConfig;
import com.axelor.meta.schema.views.ChartView.ChartSeries;
import com.axelor.meta.schema.views.Search.SearchField;
import com.google.common.base.CaseFormat;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;

public class BaseChartServiceImpl implements BaseChartService {
  @Inject MetaViewRepository metaViewRepo;
  @Inject AppBaseService appBaseService;
  @Inject UserService userService;

  protected static final String INQUERY_SELECT_TO_WHERE_PATTERN =
      "select((.+(\\(select.+where.+\\)).+))(where)";
  protected static final String QUERY_SELECT_TO_WHERE_PATTERN = "(?i)(select)(.+?)(where)";
  protected static final String QUERY_PARAMETERS_PATTERN = ":([a-zA-z0-9_]+)";

  @Override
  public ActionViewBuilder getActionView(ChartView chartView) {

    List<ChartConfig> chartConfigs = chartView.getConfig();
    if (ObjectUtils.isEmpty(chartConfigs)) {
      return null;
    }

    Optional<ChartConfig> modelConfigOpt =
        chartConfigs.stream().filter(config -> config.getName().equals("_model")).findFirst();
    if (!modelConfigOpt.isPresent()) {
      return null;
    }

    String modelFullName = modelConfigOpt.get().getValue();
    if (StringUtils.isBlank(modelFullName)) {
      return null;
    }

    String viewName =
        CaseFormat.UPPER_CAMEL.to(
            CaseFormat.LOWER_HYPHEN, StringUtils.substringAfterLast(modelFullName, "."));
    String filter = "self.id IN :ids";

    ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get(chartView.getTitle()));
    actionViewBuilder.model(modelFullName);
    actionViewBuilder.add("grid", getViewName(viewName, "grid", modelFullName));
    actionViewBuilder.add("form", getViewName(viewName, "form", modelFullName));
    actionViewBuilder.domain(filter);

    return actionViewBuilder;
  }

  protected List<String> getParam(ChartView chartView, String queryStr) {

    if (ObjectUtils.isEmpty(chartView.getSearchFields())) {
      return null;
    }

    List<String> searchNames =
        chartView.getSearchFields().stream().map(SearchField::getName).collect(Collectors.toList());
    Matcher matcher = Pattern.compile(QUERY_PARAMETERS_PATTERN).matcher(queryStr);
    List<String> contextKeys = new ArrayList<>();
    while (matcher.find()) {
      String matchedStr = matcher.group(1);
      if (searchNames.contains(matchedStr)) {
        continue;
      }
      contextKeys.add(matchedStr);
    }

    return contextKeys;
  }

  protected Boolean isMatcher(ChartView chartView) {

    String queryStr =
        chartView
            .getDataSet()
            .getText()
            .trim()
            .replaceAll("\\n", " ")
            .replaceAll("(.+?)( +)", String.format("$1%s", " "))
            .toLowerCase();
    queryStr = queryStr.replaceFirst("\\(( )(select)", String.format("%s$2", "("));
    Matcher matcher = Pattern.compile(INQUERY_SELECT_TO_WHERE_PATTERN).matcher(queryStr);
    if (matcher.find()) {
      return matcher.group(3) != null;
    }

    matcher = Pattern.compile(QUERY_SELECT_TO_WHERE_PATTERN).matcher(queryStr);
    if (matcher.find()) {
      return matcher.group(3) != null;
    }

    return false;
  }

  protected String getViewName(String viewName, String type, String model) {
    String view = String.format("%s-%s", viewName, type);
    MetaView metaView = metaViewRepo.findByName(view);
    if (metaView == null) {
      metaView =
          metaViewRepo
              .all()
              .filter("self.type = :type AND self.model = :model")
              .bind("type", type)
              .bind("model", model)
              .fetchOne();
    }
    return metaView != null ? metaView.getName() : view;
  }

  @Override
  public Map<String, String> getConfigs(Map<String, Object> context, ChartView chartView) {
    List<ChartConfig> chartConfigs = chartView.getConfig();
    Map<String, String> configs = new HashMap<>();
    for (ChartConfig chartConfig : chartConfigs) {
      String configName = chartConfig.getName();
      configs.put(configName, chartConfig.getValue());
    }
    return configs;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getSerachValues(Map<String, Object> context, ChartView chartView) {
    List<SearchField> searchFields = chartView.getSearchFields();
    if (ObjectUtils.isEmpty(searchFields)) {
      return null;
    }
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    Map<String, Object> searchMap = new HashMap<>();
    for (SearchField searchField : searchFields) {
      String searchFieldName = searchField.getName();
      if (context.containsKey(searchFieldName)) {
        Object searchFieldValue = context.get(searchFieldName);
        if (searchField.getServerType() != null) {
          if (searchField.getServerType().equals("datetime")) {
            searchFieldValue = LocalDateTime.parse(searchFieldValue.toString(), dateTimeFormatter);
          } else if (searchField.getServerType().equals("date")) {
            searchFieldValue = LocalDate.parse(searchFieldValue.toString());
          } else if (searchField.getServerType().equals("reference")) {
            try {
              if (chartView.getDataSet().getType().equals("jpql")) {
                String referenceType = searchField.getWidget();
                if (referenceType == null
                    || referenceType.equals("many-to-one")
                    || referenceType.equals("one-to-one")) {
                  String target = searchField.getTarget();
                  Class<? extends Model> klass = (Class<? extends Model>) Class.forName(target);
                  JpaRepository<? extends Model> repoKlass = JpaRepository.of(klass);
                  Map<String, Object> obj = (Map<String, Object>) searchFieldValue;
                  searchFieldValue = repoKlass.find(Long.parseLong(obj.get("id").toString()));
                }
              }
            } catch (Exception e) {
            }
          }
        }
        searchMap.put(searchFieldName, searchFieldValue);
      }
    }

    return searchMap;
  }

  protected String getFilterValue(Map<String, Object> context, ChartView chartView) {
    String filterValue = null;
    ChartCategory chartCategory = chartView.getCategory();
    if (context.containsKey(chartCategory.getKey())) {
      filterValue = context.get(chartCategory.getKey()).toString();
    }
    return filterValue;
  }

  protected String getQueryStr(Map<String, Object> context, ChartView chartView) {
    Boolean isMatcher = isMatcher(chartView);
    Map<String, String> chartConfigs = getConfigs(context, chartView);
    String whereFilter = String.format("(%s = :chartFilterValue)", chartConfigs.get("_filter"));
    if (StringUtils.isNotBlank(chartConfigs.get("_groupFilter"))) {
      whereFilter +=
          String.format(" AND (%s = :chartGroupFilterValue)", chartConfigs.get("_groupFilter"));
    }

    String queryStr =
        StringUtils.normalizeSpace(chartView.getDataSet().getText().trim().replaceAll("\\n", " "))
            .replaceFirst(
                QUERY_PATTERN,
                String.format(
                    "$1 %s.id $3",
                    chartConfigs.get("_selectFilter") != null
                        ? chartConfigs.get("_selectFilter")
                        : "self"))
            .replaceAll(QUERY_ORDER_BY_PATTERN, " ")
            .replaceAll(
                QUERY_DATE_PATTERN, String.format("'%s'", appBaseService.getTodayDate(null)))
            .replaceAll(
                QUERY_DATE_TIME_PATTERN,
                String.format("'%s'", appBaseService.getTodayDateTime(null)))
            .trim();
    if (StringUtils.isNotBlank(chartConfigs.get("_optSelectConfig"))) {
      queryStr =
          queryStr.replaceFirst(
              QUERY_PATTERN, String.format("$1 %s $3", chartConfigs.get("_optSelectConfig")));
      if (isMatcher) {
        queryStr =
            queryStr.replaceAll(
                QUERY_GROUP_BY_PATTERN,
                String.format("AND %s $1 %s", whereFilter, chartConfigs.get("_optSelectConfig")));
      } else {
        queryStr =
            queryStr.replaceAll(
                QUERY_GROUP_BY_PATTERN,
                String.format("WHERE %s $1 %s", whereFilter, chartConfigs.get("_optSelectConfig")));
      }
    } else if (isMatcher) {
      queryStr =
          queryStr.replaceAll(
              QUERY_GROUP_BY_PATTERN,
              String.format(
                  "AND %s $1 %s.id",
                  whereFilter,
                  chartConfigs.get("_selectFilter") != null
                      ? chartConfigs.get("_selectFilter")
                      : "self"));
    } else {
      queryStr =
          queryStr.replaceAll(
              QUERY_GROUP_BY_PATTERN,
              String.format(
                  "WHERE %s $1 %s.id",
                  whereFilter,
                  chartConfigs.get("_selectFilter") != null
                      ? chartConfigs.get("_selectFilter")
                      : "self"));
    }
    return queryStr;
  }

  @SuppressWarnings({"unused", "unchecked"})
  protected String addIdStr(Map<String, Object> context, ChartView chartView, String queryStr) {
    List<String> params = getParam(chartView, queryStr);
    String pattern = "([\\S]+)([ ]*=[ ]*)";
    if (ObjectUtils.notEmpty(params)) {
      for (String param : params) {
        if (context.containsKey(param)) {
          try {
            Map<String, Object> paramObj = (Map<String, Object>) context.get(param);
            String finalPattern = pattern.concat("(:" + param + ")");
            Matcher matcher = Pattern.compile(finalPattern).matcher(queryStr);
            if (matcher.find()) {
              queryStr = queryStr.replaceFirst(finalPattern, String.format("$1%s$2$3", ".id"));
            }
          } catch (Exception e) {
          }
        }
      }
    }
    return queryStr;
  }

  protected Query getQuery(Map<String, Object> context, ChartView chartView, String queryStr) {
    Query query = null;
    String filterValue = getFilterValue(context, chartView);
    User user = userService.getUser();
    if (chartView.getDataSet().getType().equals("sql")) {
      queryStr = queryStr.replaceAll(QUERY_USER_PATTERN, String.format("$1%s", user.getId()));
      query = JPA.em().createNativeQuery(queryStr).setParameter("chartFilterValue", filterValue);
    } else if (chartView.getDataSet().getType().equals("jpql")) {
      queryStr = addIdStr(context, chartView, queryStr);
      query = JPA.em().createQuery(queryStr).setParameter("chartFilterValue", filterValue);
      Matcher matcher = Pattern.compile("__user__").matcher(queryStr);
      if (matcher.find()) {
        query = query.setParameter("__user__", user);
      }
    } else {
      query = JPA.em().createQuery(queryStr).setParameter("chartFilterValue", filterValue);
    }
    return query;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Query setParamValue(Map<String, Object> context, ChartView chartView, String queryStr) {
    List<String> params = getParam(chartView, queryStr);
    Query query = getQuery(context, chartView, queryStr);
    Map<String, Object> searchValues = getSerachValues(context, chartView);
    ChartSeries chartSeries = chartView.getSeries().get(0);
    String groupFilterValue = null;
    if (ObjectUtils.notEmpty(searchValues)) {
      for (Entry<String, Object> searchValue : searchValues.entrySet()) {
        query = query.setParameter(searchValue.getKey(), searchValue.getValue());
      }
    }
    if (StringUtils.isNotBlank(chartSeries.getGroupBy())
        && context.containsKey(chartSeries.getGroupBy())) {
      groupFilterValue = context.get(chartSeries.getGroupBy()).toString();
    }

    if (ObjectUtils.notEmpty(params)) {
      for (String param : params) {
        String paramName = param.toString();
        if (context.containsKey(paramName)) {
          String paramValueId = null;
          try {
            Map<String, Object> paramValue = (Map<String, Object>) context.get(paramName);
            paramValueId = paramValue.get("id").toString();
          } catch (Exception e) {
          }
          if (paramValueId != null) {
            query = query.setParameter(paramName, Long.parseLong(paramValueId));
          } else {
            query = query.setParameter(paramName, context.get(paramName));
          }
        }
      }
    }
    if (StringUtils.isNotBlank(groupFilterValue)) {
      query = query.setParameter("chartGroupFilterValue", groupFilterValue);
    }
    return query;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public List<Long> getIdList(Map<String, Object> context, ChartView chartView) {
    String queryStr = getQueryStr(context, chartView);
    Query query = setParamValue(context, chartView, queryStr);
    if (query != null) {
      return query.getResultList();
    }
    return new ArrayList<>();
  }
}
