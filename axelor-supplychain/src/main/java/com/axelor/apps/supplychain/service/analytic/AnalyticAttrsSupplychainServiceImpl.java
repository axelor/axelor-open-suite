/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticLineModelService;
import com.axelor.apps.base.AxelorException;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AnalyticAttrsSupplychainServiceImpl implements AnalyticAttrsSupplychainService {

  protected AnalyticLineModelService analyticLineModelService;
  protected AnalyticAttrsService analyticAttrsService;

  @Inject
  public AnalyticAttrsSupplychainServiceImpl(
      AnalyticLineModelService analyticLineModelService,
      AnalyticAttrsService analyticAttrsService) {
    this.analyticLineModelService = analyticLineModelService;
    this.analyticAttrsService = analyticAttrsService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAnalyticAccountRequiredAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    analyticAttrsService.addAnalyticAccountRequired(
        analyticLineModel, analyticLineModel.getCompany(), attrsMap);
  }

  @Override
  public void addAnalyticDistributionPanelHiddenAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    boolean displayPanel =
        !(analyticLineModelService.productAccountManageAnalytic(analyticLineModel)
            || analyticLineModelService.analyticDistributionTemplateRequired(
                analyticLineModel.getIsPurchase(), analyticLineModel.getCompany()));

    this.addAttr("analyticDistributionPanel", "hidden", displayPanel, attrsMap);
  }
}
