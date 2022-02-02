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
package com.axelor.studio.service;

import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.views.ChartView;
import com.axelor.meta.schema.views.ChartView.ChartSeries;
import com.axelor.meta.schema.views.Search.SearchField;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.db.ChartBuilder;
import com.axelor.studio.db.Filter;
import com.axelor.studio.db.repo.ChartBuilderRepository;
import com.axelor.studio.service.filter.FilterSqlService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.beanutils.ConvertUtils;

public class ChartRecordViewServiceImpl implements ChartRecordViewService {

  protected static final String PARAM_PREFIX = "param";

  protected static final String PARAM_GROUP = "param0";
  protected static final String PARAM_AGG = PARAM_PREFIX + Long.MAX_VALUE;
  protected static final List<String> AGGR_SUPPORTED_CHARTS =
      Arrays.asList("bar", "hbar", "scatter");
  protected static final List<String> TARGET_DATE_TYPES =
      Arrays.asList("DATE", "DATETIME", "LOCALDATE", "LOCALDATETIME", "ZONNEDDATETIME");
  protected ChartBuilderRepository chartBuilderRepository;
  protected MetaJsonModelRepository metaJsonModelRepository;
  protected MetaModelRepository metaModelRepository;
  protected MetaViewRepository metaViewRepository;
  protected FilterSqlService filterSqlService;

  @Inject
  public ChartRecordViewServiceImpl(
      ChartBuilderRepository chartBuilderRepository,
      MetaJsonModelRepository metaJsonModelRepository,
      MetaModelRepository metaModelRepository,
      MetaViewRepository metaViewRepository,
      FilterSqlService filterSqlService) {
    this.chartBuilderRepository = chartBuilderRepository;
    this.metaJsonModelRepository = metaJsonModelRepository;
    this.metaModelRepository = metaModelRepository;
    this.metaViewRepository = metaViewRepository;
    this.filterSqlService = filterSqlService;
  }

  @Override
  public Map<String, Object> getActionView(String chartName, Map<String, Object> context)
      throws AxelorException {
    ChartBuilder chartBuilder = chartBuilderRepository.findByName(chartName);

    if (ObjectUtils.isEmpty(chartBuilder)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("No chart builder found with given chart name"));
    }

    if (chartBuilder.getIsJson()) {
      return getJsonModelActionView(chartBuilder, context);
    }

    return getMetaModelActionView(chartBuilder, context);
  }

  protected Map<String, Object> getJsonModelActionView(
      ChartBuilder chartBuilder, Map<String, Object> context) throws AxelorException {
    MetaJsonModel jsonModel = metaJsonModelRepository.findByName(chartBuilder.getModel());
    String title = jsonModel.getTitle();
    if (Strings.isNullOrEmpty(title)) {
      title = chartBuilder.getModel();
    }
    String formView = "custom-model-" + jsonModel.getName() + "-form";
    String gridView = "custom-model-" + jsonModel.getName() + "-grid";

    ActionViewBuilder builder =
        ActionView.define(I18n.get(title))
            .model(MetaJsonRecord.class.getName())
            .add("grid", gridView)
            .add("form", formView);

    String filter = "self.jsonModel = :jsonModel";
    builder.context("jsonModel", jsonModel.getName());
    filter += " AND " + getDomainFilter(chartBuilder, context);
    builder.domain(filter);

    return builder.map();
  }

  protected Map<String, Object> getMetaModelActionView(
      ChartBuilder chartBuilder, Map<String, Object> context) throws AxelorException {
    String domain = getDomainFilter(chartBuilder, context);
    String simpleName = getModelClass(chartBuilder).getSimpleName();
    Inflector instance = Inflector.getInstance();
    String dasherizeModel = instance.dasherize(simpleName);
    return ActionView.define(
            I18n.get(instance.humanize(getModelClass(chartBuilder).getSimpleName())))
        .model(chartBuilder.getModel())
        .domain(domain)
        .add("grid", dasherizeModel + "-grid")
        .add("form", dasherizeModel + "-form")
        .map();
  }

  @SuppressWarnings("unchecked")
  protected String getDomainFilter(ChartBuilder chartBuilder, Map<String, Object> context)
      throws AxelorException {
    ChartView chart = (ChartView) XMLViews.findView(chartBuilder.getName(), "chart");
    Map<String, Object> params = getQueryParams(context, chart);
    String queryString = prepareQuery(chartBuilder, params);
    Query query = JPA.em().createNativeQuery(queryString);
    params.forEach(query::setParameter);
    List<BigInteger> resultList = query.getResultList();
    resultList.add(BigInteger.ZERO);
    return resultList
        .parallelStream()
        .map(String::valueOf)
        .collect(Collectors.joining(",", "self.id in (", ")"));
  }

  protected Map<String, Object> getQueryParams(Map<String, Object> context, ChartView chart) {
    Map<String, Object> params = new HashMap<>();

    String groupByKey = chart.getCategory().getKey();
    Object groupByValue = context.get(groupByKey);
    if (groupByValue != null && !"null".equals(groupByValue)) {
      params.put(PARAM_GROUP, groupByValue);
    }

    String aggByKey =
        chart.getSeries().stream()
            .map(ChartSeries::getGroupBy)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    if (StringUtils.notBlank(aggByKey)) {
      params.put(PARAM_AGG, context.get(aggByKey));
    }

    if (ObjectUtils.notEmpty(chart.getSearchFields())) {
      for (SearchField searchFields : chart.getSearchFields()) {
        String name = searchFields.getName();
        params.put(name, context.get(name));
      }
    }
    return params;
  }

  protected String prepareQuery(ChartBuilder chartBuilder, Map<String, Object> params)
      throws AxelorException {
    ArrayList<String> joins = new ArrayList<>();
    List<Filter> filterList = chartBuilder.getFilterList();

    List<Filter> filterForGroups = getFilters(chartBuilder, params, true);
    filterList.addAll(filterForGroups);

    List<Filter> filterForAggregations = getFilters(chartBuilder, params, false);
    filterList.addAll(filterForAggregations);

    final String tableName = getTableName(chartBuilder);
    String sqlFilters = filterSqlService.getSqlFilters(filterList, joins, true);
    if (sqlFilters != null) {
      return String.format(
          "select self.id from %s self %s where %s",
          tableName, String.join("\n", joins), sqlFilters);
    }
    return String.format("select self.id from %s self %s", tableName, String.join("\n", joins));
  }

  protected List<Filter> getFilters(
      ChartBuilder chartBuilder, Map<String, Object> params, boolean isForGroup)
      throws AxelorException {

    String paramKey = isForGroup ? PARAM_GROUP : PARAM_AGG;

    if (!isForGroup && isAggregationAllowed(chartBuilder)) {
      params.remove(paramKey);
      return new ArrayList<>();
    }

    String targetType =
        isForGroup ? chartBuilder.getGroupOnTargetType() : chartBuilder.getAggregateOnTargetType();
    Object paramObj = params.get(paramKey);
    boolean isNull = ObjectUtils.isEmpty(paramObj);
    Filter filter = createFilter(chartBuilder, isNull, isForGroup);

    if (!TARGET_DATE_TYPES.contains(targetType.toUpperCase()) || isNull) {
      if (isNull) {
        params.remove(paramKey);
      } else {
        Object value = getSelectionFieldValue(chartBuilder, paramObj, isForGroup);
        if (value != null) {
          params.put(paramKey, value);
        }
      }
      return Arrays.asList(filter);
    }

    List<Filter> dateFilters = getFiltersForDateType(chartBuilder, params, filter, false);
    return dateFilters;
  }

  protected boolean isAggregationAllowed(ChartBuilder chartBuilder) {
    return !AGGR_SUPPORTED_CHARTS.contains(chartBuilder.getChartType())
        || (chartBuilder.getAggregateOn() == null && chartBuilder.getAggregateOnJson() == null);
  }

  protected List<Filter> getFiltersForDateType(
      ChartBuilder chartBuilder, Map<String, Object> params, Filter filter, Boolean isForGroup) {
    String paramKey = PARAM_GROUP;
    String dateType = chartBuilder.getGroupDateType();
    String targetType = chartBuilder.getGroupOnTargetType();

    if (!isForGroup) {
      paramKey = PARAM_AGG;
      targetType = chartBuilder.getAggregateOnTargetType();
      dateType = chartBuilder.getAggregateDateType();
    }

    String paramValue = (String) params.get(paramKey);

    if ("day".equals(dateType)) {

      if (LocalDateTime.class.getSimpleName().equalsIgnoreCase(targetType)) {
        params.put(
            paramKey,
            ZonedDateTime.parse(paramValue)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:00")));
      }
      return Arrays.asList(filter);
    }

    long startId = isForGroup ? 10000l : 10001l;
    long endId = isForGroup ? 50000l : 50001l;

    List<Filter> startEndFilter = getYearMonthFilters(filter, startId, endId);

    DateRangeConvertor dateRangeConvertor = new DateRangeConvertor(dateType, paramValue);
    params.put(PARAM_PREFIX + startId, dateRangeConvertor.getStartDateStr());
    params.put(PARAM_PREFIX + endId, dateRangeConvertor.getEndDateStr());
    params.remove(paramKey);

    return startEndFilter;
  }

  protected List<Filter> getYearMonthFilters(Filter filter, long startId, long endId) {
    List<Filter> startEndFilter = new ArrayList<>();

    Filter startDateFilter = JPA.copy(filter, false);
    startDateFilter.setId(startId);
    startDateFilter.setOperator(">=");

    Filter endDateFilter = JPA.copy(filter, false);
    endDateFilter.setId(endId);
    endDateFilter.setOperator("<=");

    startEndFilter.add(startDateFilter);
    startEndFilter.add(endDateFilter);

    return startEndFilter;
  }

  protected Filter createFilter(
      Long id,
      Boolean isTargetJson,
      Boolean isJson,
      MetaJsonField jsonField,
      MetaField metaField,
      String targetField,
      String targetType,
      boolean isNull,
      String operator) {
    Filter filter = new Filter();
    filter.setId(id);

    filter.setIsTargetJson(isTargetJson);

    if (isJson) {
      filter.setIsJson(true);
      filter.setMetaJsonField(jsonField);
    } else {
      filter.setMetaField(metaField);
    }
    filter.setTargetField(targetField);
    filter.setTargetType(targetType);
    if (isNull) {
      filter.setOperator("isNull");
    } else {
      filter.setOperator(operator);
      filter.setIsParameter(true);
    }
    return filter;
  }

  protected Filter createFilter(ChartBuilder chartBuilder, boolean isNull, Boolean isForGroup) {
    Boolean isJson = chartBuilder.getIsJson();
    Filter filter =
        isForGroup
            ? createFilter(
                0l,
                isJson,
                chartBuilder.getIsJsonGroupOn(),
                chartBuilder.getGroupOnJson(),
                chartBuilder.getGroupOn(),
                chartBuilder.getGroupOnTarget(),
                chartBuilder.getGroupOnTargetType(),
                isNull,
                "=")
            : createFilter(
                Long.MAX_VALUE,
                isJson,
                chartBuilder.getIsJsonAggregateOn(),
                chartBuilder.getAggregateOnJson(),
                chartBuilder.getAggregateOn(),
                chartBuilder.getAggregateOnTarget(),
                chartBuilder.getAggregateOnTargetType(),
                isNull,
                "=");
    return filter;
  }

  protected Object getSelectionFieldValue(
      ChartBuilder chartBuilder, Object titleParam, Boolean isForGroup) throws AxelorException {
    Object value = null;
    String selection = null;
    Class<?> targetType = String.class;

    Boolean isJson =
        chartBuilder.getIsJson()
            || (isForGroup ? chartBuilder.getIsJsonGroupOn() : chartBuilder.getIsJsonAggregateOn());
    MetaField target = chartBuilder.getGroupOn();
    MetaJsonField jsonField = chartBuilder.getGroupOnJson();

    if (!isForGroup) {
      target = chartBuilder.getAggregateOn();
      jsonField = chartBuilder.getAggregateOnJson();
    }

    if (isJson
        && ObjectUtils.notEmpty(jsonField.getSelection())
        && (Integer.class.getSimpleName().toLowerCase().equals(jsonField.getType())
            || String.class.getSimpleName().toLowerCase().equals(jsonField.getType()))) {
      selection = jsonField.getSelection();
      if (Integer.class.getSimpleName().toLowerCase().equals(jsonField.getType())) {
        targetType = Integer.class;
      }
    } else {
      try {
        Mapper mapper = Mapper.of(Class.forName(chartBuilder.getModel()));
        Property p = mapper.getProperty(target.getName());
        if (ObjectUtils.notEmpty(p.getSelection())) {
          selection = p.getSelection();
          targetType = p.getJavaType();
        }
      } catch (ClassNotFoundException e) {
        throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
      }
    }

    if (ObjectUtils.isEmpty(selection)) {
      return value;
    }
    List<Option> selectionList = MetaStore.getSelectionList(selection);
    for (Option option : selectionList) {
      if (option.getLocalizedTitle().equals(titleParam)) {
        return ConvertUtils.convert(option.getValue(), targetType);
      }
    }
    return value;
  }

  protected String getTableName(ChartBuilder chartBuilder) throws AxelorException {
    Class<?> modelClass = getModelClass(chartBuilder);
    MetaModel metaModel = metaModelRepository.findByName(modelClass.getSimpleName());
    return metaModel.getTableName();
  }

  protected Class<?> getModelClass(ChartBuilder chartBuilder) throws AxelorException {
    Class<?> modelClass;

    if (chartBuilder.getIsJson()) {
      modelClass = MetaJsonRecord.class;
    } else {
      try {
        modelClass = Class.forName(chartBuilder.getModel());
      } catch (ClassNotFoundException e) {
        throw new AxelorException(e, TraceBackRepository.CATEGORY_NO_VALUE);
      }
    }
    return modelClass;
  }

  private static class DateRangeConvertor {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");

    private final String startDateStr;
    private final String endDateStr;

    public DateRangeConvertor(String dateGroupType, String value) {
      LocalDate startDate = null;
      LocalDate endDate = null;

      if ("month".equals(dateGroupType)) {
        YearMonth yearMonth = YearMonth.parse(value, MONTH_FORMAT);
        startDate = yearMonth.atDay(1);
        endDate = yearMonth.atEndOfMonth();
      } else {
        Year year = Year.parse(value, YEAR_FORMAT);
        startDate = year.atDay(1);
        endDate = year.atMonth(12).atEndOfMonth();
      }
      this.startDateStr = startDate.format(DATE_FORMAT);
      this.endDateStr = endDate.format(DATE_FORMAT);
    }

    public String getStartDateStr() {
      return startDateStr;
    }

    public String getEndDateStr() {
      return endDateStr;
    }
  }
}
