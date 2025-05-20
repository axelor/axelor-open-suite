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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.fixedasset.attributes.FixedAssetAttrsService;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FixedAssetGroupServiceImpl implements FixedAssetGroupService {

  protected FixedAssetAttrsService fixedAssetAttrsService;
  protected FixedAssetRecordService fixedAssetRecordService;

  @Inject
  public FixedAssetGroupServiceImpl(
      FixedAssetAttrsService fixedAssetAttrsService,
      FixedAssetRecordService fixedAssetRecordService) {
    this.fixedAssetAttrsService = fixedAssetAttrsService;
    this.fixedAssetRecordService = fixedAssetRecordService;
  }

  @Override
  public Map<String, Object> getDisposalWizardValuesMap(
      FixedAsset fixedAsset, Integer disposalTypeSelect) {
    Map<String, Object> valuesMap = new HashMap<>();

    if (!Objects.equals(FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION, disposalTypeSelect)) {
      valuesMap.put("disposalQtySelect", FixedAssetRepository.DISPOSABLE_QTY_SELECT_TOTAL);
    }

    valuesMap.put(
        "disposalAmount",
        fixedAssetRecordService.setDisposalAmount(fixedAsset, disposalTypeSelect));

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getDisposalWizardAttrsMap(
      Integer disposalTypeSelect, FixedAsset fixedAsset) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    fixedAssetAttrsService.addDisposalAmountTitle(disposalTypeSelect, attrsMap);
    fixedAssetAttrsService.addDisposalAmountReadonly(disposalTypeSelect, attrsMap);
    fixedAssetAttrsService.addDisposalAmountScale(fixedAsset, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getInitSplitWizardAttrsMap(
      BigDecimal qty, Company company) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    fixedAssetAttrsService.addSplitTypeSelectValue(qty, attrsMap);
    fixedAssetAttrsService.addSplitTypeSelectReadonly(qty, attrsMap);
    fixedAssetAttrsService.addGrossValueScale(company, attrsMap);

    return attrsMap;
  }
}
