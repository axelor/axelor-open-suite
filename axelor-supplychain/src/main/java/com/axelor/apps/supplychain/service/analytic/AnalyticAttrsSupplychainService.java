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
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface AnalyticAttrsSupplychainService {

  /**
   * Adds the "required" attribute of the five analytic axis fields, so that the isRequired flag
   * configured on the analytic axis by company also applies to lines that carry no accounting
   * account of their own (sale and purchase order lines, contract lines).
   */
  void addAnalyticAccountRequiredAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addAnalyticDistributionPanelHiddenAttrs(
      AnalyticLineModel analyticLineModel, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;
}
