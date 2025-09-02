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
import com.axelor.apps.base.db.repo.IndicatorConfigRepository;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.meta.schema.views.FormView;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class IndicatorMetaServiceImpl implements IndicatorMetaService {

  private static final String INDICATOR_TITLE = "Indicators";
  private static final String BASE_ACTION =
      "action-indicator-result-line-view-indicator-result-line";
  private static final String CHART_ACTION_PREFIX = "chart:indicator-result-chart-";

  private final IndicatorConfigRepository configRepo;

  @Inject
  public IndicatorMetaServiceImpl(IndicatorConfigRepository configRepo) {
    this.configRepo = configRepo;
  }

  @Override
  public void process(AbstractView view) {
    if (!(view instanceof FormView)) {
      return;
    }

    FormView formView = (FormView) view;

    final List<IndicatorConfig> indicatorConfigs =
        configRepo.findByMetaModelNameActiveForDisplay(view.getModel()).fetch();

    if (CollectionUtils.isEmpty(indicatorConfigs)) {
      return;
    }
    addDashlet(formView, BASE_ACTION);

    for (IndicatorConfig config : indicatorConfigs) {
      if (Boolean.TRUE.equals(config.getDisplayBarChart())) {
        addDashlet(formView, CHART_ACTION_PREFIX + config.getId());
      }
    }
  }

  private void addDashlet(FormView formView, String action) {
    final Dashlet dashlet = new Dashlet();
    dashlet.setAction(action);
    dashlet.setTitle(INDICATOR_TITLE);
    formView.getItems().add(dashlet);
  }
}
