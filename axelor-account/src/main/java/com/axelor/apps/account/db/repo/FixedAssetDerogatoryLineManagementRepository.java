/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.axelor.inject.Beans;
import java.util.Map;

public class FixedAssetDerogatoryLineManagementRepository
    extends FixedAssetDerogatoryLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine = this.find((Long) json.get("id"));
      json.put(
          "$currencyNumberOfDecimals",
          Beans.get(CurrencyScaleServiceAccount.class)
              .getCompanyScale(fixedAssetDerogatoryLine.getFixedAsset()));
    }

    return super.populate(json, context);
  }
}
