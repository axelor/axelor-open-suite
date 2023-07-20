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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineComputationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineEconomicComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineEconomicUpdateComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineFiscalComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineIfrsComputationServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class FixedAssetLineComputationServiceFactory {

  protected FixedAssetLineEconomicUpdateComputationServiceImpl
      fixedAssetLineEconomicUpdateComputationService;
  protected FixedAssetLineEconomicComputationServiceImpl fixedAssetLineEconomicComputationService;
  protected FixedAssetLineFiscalComputationServiceImpl fixedAssetLineFiscalComputationService;
  protected FixedAssetLineIfrsComputationServiceImpl fixedAssetLineIfrsComputationService;

  @Inject
  public FixedAssetLineComputationServiceFactory(
      FixedAssetLineEconomicUpdateComputationServiceImpl
          fixedAssetLineEconomicUpdateComputationService,
      FixedAssetLineEconomicComputationServiceImpl fixedAssetLineEconomicComputationService,
      FixedAssetLineFiscalComputationServiceImpl fixedAssetLineFiscalComputationService,
      FixedAssetLineIfrsComputationServiceImpl fixedAssetLineIfrsComputationService) {
    this.fixedAssetLineEconomicUpdateComputationService =
        fixedAssetLineEconomicUpdateComputationService;
    this.fixedAssetLineEconomicComputationService = fixedAssetLineEconomicComputationService;
    this.fixedAssetLineFiscalComputationService = fixedAssetLineFiscalComputationService;
    this.fixedAssetLineIfrsComputationService = fixedAssetLineIfrsComputationService;
  }

  public FixedAssetLineComputationService getFixedAssetComputationService(
      FixedAsset fixedAsset, int typeSelect) throws AxelorException {

    switch (typeSelect) {
      case FixedAssetLineRepository.TYPE_SELECT_ECONOMIC:
        if (fixedAsset.getCorrectedAccountingValue() != null
            && fixedAsset.getCorrectedAccountingValue().signum() != 0) {
          return fixedAssetLineEconomicUpdateComputationService;
        }
        return fixedAssetLineEconomicComputationService;
      case FixedAssetLineRepository.TYPE_SELECT_FISCAL:
        return fixedAssetLineFiscalComputationService;
      case FixedAssetLineRepository.TYPE_SELECT_IFRS:
        return fixedAssetLineIfrsComputationService;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_LINE_COMPUTATION_SERVICE_NOT_FOUND));
    }
  }
}
