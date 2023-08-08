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
package com.axelor.apps.account.service.fixedasset.factory;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineEconomicServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineFiscalServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineIfrsServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class FixedAssetLineServiceFactory {
  protected FixedAssetLineEconomicServiceImpl fixedAssetLineEconomicService;
  protected FixedAssetLineFiscalServiceImpl fixedAssetLineFiscalService;
  protected FixedAssetLineIfrsServiceImpl fixedAssetLineIfrsService;

  @Inject
  public FixedAssetLineServiceFactory(
      FixedAssetLineEconomicServiceImpl fixedAssetLineEconomicService,
      FixedAssetLineFiscalServiceImpl fixedAssetLineFiscalService,
      FixedAssetLineIfrsServiceImpl fixedAssetLineIfrsService) {
    this.fixedAssetLineEconomicService = fixedAssetLineEconomicService;
    this.fixedAssetLineFiscalService = fixedAssetLineFiscalService;
    this.fixedAssetLineIfrsService = fixedAssetLineIfrsService;
  }

  public FixedAssetLineService getFixedAssetService(int typeSelect) throws AxelorException {

    switch (typeSelect) {
      case FixedAssetLineRepository.TYPE_SELECT_ECONOMIC:
        return fixedAssetLineEconomicService;
      case FixedAssetLineRepository.TYPE_SELECT_FISCAL:
        return fixedAssetLineFiscalService;
      case FixedAssetLineRepository.TYPE_SELECT_IFRS:
        return fixedAssetLineIfrsService;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_LINE_SERVICE_NOT_FOUND));
    }
  }

  public List<Integer> getTypeSelectList(FixedAsset fixedAsset) {
    String depreciationPlanSelect = fixedAsset.getDepreciationPlanSelect();
    List<Integer> typeSelectList = new ArrayList<>();
    if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)) {
      typeSelectList.add(FixedAssetLineRepository.TYPE_SELECT_FISCAL);
    }
    if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)) {
      typeSelectList.add(FixedAssetLineRepository.TYPE_SELECT_IFRS);
    }
    typeSelectList.add(FixedAssetLineRepository.TYPE_SELECT_ECONOMIC);
    return typeSelectList;
  }
}
