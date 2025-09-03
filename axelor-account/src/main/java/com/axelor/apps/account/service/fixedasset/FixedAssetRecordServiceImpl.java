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
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class FixedAssetRecordServiceImpl implements FixedAssetRecordService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public FixedAssetRecordServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public BigDecimal setDisposalAmount(FixedAsset fixedAsset, Integer disposalTypeSelect) {
    BigDecimal disposalAmount;
    if (fixedAsset == null || disposalTypeSelect == null) {
      return BigDecimal.ZERO;
    }

    switch (disposalTypeSelect) {
      case FixedAssetRepository.DISPOSABLE_TYPE_SELECT_SCRAPPING:
      case FixedAssetRepository.DISPOSABLE_TYPE_SELECT_ONGOING_CESSION:
        disposalAmount = fixedAsset.getAccountingValue();
        break;
      case FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION:
        disposalAmount = fixedAsset.getResidualValue();
        break;
      default:
        disposalAmount = BigDecimal.ZERO;
    }

    return currencyScaleService.getCompanyScaledValue(fixedAsset, disposalAmount);
  }
}
