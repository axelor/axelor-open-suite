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
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.meta.schema.views.FormView;
import com.axelor.web.ITranslation;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import net.fortuna.ical4j.util.Optional;
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

    final FormView formView = (FormView) view;
    final String viewName = view.getName();
    final String modelName = view.getModel();
    final boolean isIndicatorResultViewer =
        viewName != null && viewName.startsWith("indicator-result-viewer-");
    final List<IndicatorConfig> indicatorConfigs =
        configRepo.findByMetaModelNameActiveForDisplay(modelName).fetch();

    if (CollectionUtils.isEmpty(indicatorConfigs)) {
      return;
    }

    boolean addBaseDashlet = false;
    boolean addToolbarButton = false;
    final List<String> chartActions = new ArrayList<>();

    for (IndicatorConfig config : indicatorConfigs) {
      final int displayType = config.getDisplayInRecordViewTypeSelect();
      final boolean displayBarChart = config.getDisplayBarChart();

      if (displayType == IndicatorConfigRepository.DISPLAY_TYPE_VIEW) {
        addBaseDashlet = true;
        if (displayBarChart) {
          chartActions.add(CHART_ACTION_PREFIX + config.getId());
        }
      } else if (displayType == IndicatorConfigRepository.DISPLAY_TYPE_BUTTON) {
        addToolbarButton = true;
        if (displayBarChart && isIndicatorResultViewer) {
          chartActions.add(CHART_ACTION_PREFIX + config.getId());
        }
      }
    }

    if (addToolbarButton && !isIndicatorResultViewer) {
      addToolBarBtn(formView);
    }

    if (addBaseDashlet || isIndicatorResultViewer) {
      addDashlet(formView, BASE_ACTION);
    }
    for (String action : chartActions) {
      addDashlet(formView, action);
    }
  }

  protected void addToolBarBtn(FormView form) {
    List<Button> toolbar = Optional.ofNullable(form.getToolbar()).orElse(new ArrayList<>());
    Button btn = new Button();
    btn.setName("indicatorBtn");
    btn.setOnClick("com.axelor.apps.base.web.IndicatorController:viewIndicatorResults");
    btn.setTitle(ITranslation.INDICATOR_RESULT_TOOLBAR_BTN);
    btn.setShowIf("id");
    btn.setIcon("analytics");
    toolbar.add(btn);
    form.setToolbar(toolbar);
  }

  protected void addDashlet(FormView formView, String action) {
    final Dashlet dashlet = new Dashlet();
    dashlet.setAction(action);
    dashlet.setTitle(INDICATOR_TITLE);
    formView.getItems().add(dashlet);
  }
}
