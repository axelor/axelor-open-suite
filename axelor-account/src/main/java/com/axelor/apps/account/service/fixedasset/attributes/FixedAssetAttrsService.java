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
package com.axelor.apps.account.service.fixedasset.attributes;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.math.BigDecimal;
import java.util.Map;

public interface FixedAssetAttrsService {

  void addDisposalAmountTitle(
      Integer disposalTypeSelect, Map<String, Map<String, Object>> attrsMap);

  void addDisposalAmountReadonly(
      Integer disposalTypeSelect, Map<String, Map<String, Object>> attrsMap);

  void addDisposalAmountScale(FixedAsset fixedAsset, Map<String, Map<String, Object>> attrsMap);

  void addSplitTypeSelectValue(BigDecimal qty, Map<String, Map<String, Object>> attrsMap);

  void addSplitTypeSelectReadonly(BigDecimal qty, Map<String, Map<String, Object>> attrsMap);

  void addGrossValueScale(Company company, Map<String, Map<String, Object>> attrsMap);

  String addCurrentAnalyticDistributionTemplateInDomain(FixedAsset fixedAsset)
      throws AxelorException;
}
