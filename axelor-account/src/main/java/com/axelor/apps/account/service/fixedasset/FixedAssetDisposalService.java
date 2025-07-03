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

import com.axelor.apps.account.db.AssetDisposalReason;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface FixedAssetDisposalService {

  List<FixedAsset> processDisposal(
      FixedAsset fixedAsset,
      List<FixedAsset> fixedAssetList,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException;

  FixedAsset fullDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException;
}
