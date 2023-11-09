/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AnalyticAttrsSupplychainServiceImpl implements AnalyticAttrsSupplychainService {

  protected AnalyticLineModelService analyticLineModelService;
  protected AnalyticToolService analyticToolService;

  @Inject
  public AnalyticAttrsSupplychainServiceImpl(
      AnalyticLineModelService analyticLineModelService, AnalyticToolService analyticToolService) {
    this.analyticLineModelService = analyticLineModelService;
    this.analyticToolService = analyticToolService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAnalyticDistributionPanelHiddenAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    boolean accountManageAnalytic =
        analyticLineModel.getAccount() == null
            ? analyticLineModelService.productAccountManageAnalytic(analyticLineModel)
            : analyticToolService.isManageAnalytic(
                analyticLineModel.getCompany(), analyticLineModel.getAccount());
    boolean hidePanel =
        !(accountManageAnalytic
            || analyticLineModelService.analyticDistributionTemplateRequired(
                analyticLineModel.getIsPurchase(), analyticLineModel.getCompany()));

    this.addAttr("analyticDistributionPanel", "hidden", hidePanel, attrsMap);
  }
}
