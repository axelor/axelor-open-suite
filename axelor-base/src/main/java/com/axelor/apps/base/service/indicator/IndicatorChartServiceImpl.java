/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.indicator;

import com.axelor.apps.base.db.IndicatorConfig;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.views.ChartView;
import com.axelor.meta.schema.views.ChartView.ChartCategory;
import com.axelor.meta.schema.views.ChartView.ChartSeries;
import com.axelor.meta.schema.views.DataSet;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.*;
import javax.persistence.Query;

public class IndicatorChartServiceImpl implements IndicatorChartService {

  private static final String CHART_NAME_PATTERN = "indicator-result-chart-%d";
  private static final String DATASET_TYPE = "rpc";
  private static final String DATASET_METHOD =
      "com.axelor.apps.base.web.IndicatorController:fetchResultLinesForChart";
  private static final String CATEGORY_KEY = "date";
  private static final String CATEGORY_TYPE = "month";
  private static final String SERIES_KEY = "indicator";
  private static final String SERIES_TYPE = "bar";

  protected final MetaViewRepository metaViewRepository;

  @Inject
  public IndicatorChartServiceImpl(MetaViewRepository metaViewRepository) {
    this.metaViewRepository = metaViewRepository;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> fetchData(IndicatorConfig config, Long relatedId) {
    String queryString = getDataSetQuery();

    Query query = JPA.em().createQuery(queryString);
    query.setParameter("configId", config.getId());
    query.setParameter("relatedId", relatedId);
    query.setParameter("relatedTo", config.getTargetModel().getFullName());

    if (config.getForLastNResults() > 0) {
      query.setMaxResults(config.getForLastNResults());
    }

    List<Object[]> resultList = query.getResultList();
    List<Map<String, Object>> mappedResults = new ArrayList<>();

    for (Object[] row : resultList) {
      Map<String, Object> map = new HashMap<>();
      map.put(SERIES_KEY, row[0]);
      map.put(CATEGORY_KEY, row[1]);
      mappedResults.add(map);
    }

    return mappedResults;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createOrUpdateChart(IndicatorConfig config) {
    if (Boolean.FALSE.equals(config.getDisplayBarChart())) {
      return;
    }

    final String chartName = String.format(CHART_NAME_PATTERN, config.getId());
    final ChartView chart = buildChartView(chartName, config);

    final String xml = XMLViews.toXml(chart, true);
    MetaView metaView =
        Optional.ofNullable(metaViewRepository.findByName(chartName)).orElseGet(MetaView::new);
    metaView.setName(chartName);
    metaView.setType(chart.getType());
    metaView.setTitle(chart.getTitle());
    metaView.setXml(xml);

    metaViewRepository.save(metaView);
  }

  protected ChartView buildChartView(String chartName, IndicatorConfig config) {
    final ChartView chart = new ChartView();
    chart.setName(chartName);
    chart.setTitle(config.getName());

    final DataSet dataSet = new DataSet();
    dataSet.setType(DATASET_TYPE);
    dataSet.setText(DATASET_METHOD);
    chart.setDataSet(dataSet);

    final ChartCategory category = new ChartCategory();
    category.setKey(CATEGORY_KEY);
    category.setType(CATEGORY_TYPE);
    chart.setCategory(category);

    final ChartSeries series = new ChartSeries();
    series.setKey(SERIES_KEY);
    series.setType(SERIES_TYPE);
    series.setTitle(I18n.get("Indicator"));
    chart.setSeries(List.of(series));

    return chart;
  }

  protected String getDataSetQuery() {
    return "SELECT self.indicator, self.indicatorResult.dateT "
        + "FROM IndicatorResultLine self "
        + "WHERE self.indicatorResult.indicatorConfig.id = :configId "
        + "AND (self.relatedId = :relatedId AND self.metaModel.fullName = :relatedTo) "
        + "ORDER BY self.indicatorResult.dateT DESC";
  }
}
