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
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.meta.schema.views.FormView;
import com.google.inject.Inject;
import java.util.List;

public class IndicatorMetaServiceImpl implements IndicatorMetaService {

  protected final IndicatorConfigRepository configRepo;

  @Inject
  public IndicatorMetaServiceImpl(IndicatorConfigRepository configRepo) {
    this.configRepo = configRepo;
  }

  @Override
  public void process(AbstractView view) {

    if (!(view instanceof FormView) || !hasIndicator(view.getModel())) {
      return;
    }

    FormView form = (FormView) view;

    List<AbstractWidget> items = form.getItems();

    Dashlet dashlet = new Dashlet();
    dashlet.setAction("action-indicator-result-line-view-indicator-result-line");
    dashlet.setTitle("Indicators");
    items.add(dashlet);
  }

  protected boolean hasIndicator(String model) {
    IndicatorConfig indicatorConfig = configRepo.findByMetaModelNameActiveForDisplay(model);
    return indicatorConfig != null;
  }
}
