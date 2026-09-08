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
package com.axelor.apps.contract.service.analytic;

import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.analytic.AnalyticLineModelInitAccountService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.google.common.base.Preconditions;
import java.util.Optional;

public class AnalyticLineModelInitContractService {

  public static AnalyticLineModel castAsAnalyticLineModel(
      ContractLine contractLine, ContractVersion contractVersion, Contract contract) {
    Preconditions.checkNotNull(contractLine);

    contractVersion = contractVersion != null ? contractVersion : contractLine.getContractVersion();
    contract =
        contract != null
            ? contract
            : Optional.ofNullable(contractVersion).map(ContractVersion::getContract).orElse(null);

    Company company = null;
    TradingName tradingName = null;
    Partner partner = null;
    boolean isPurchase = false;

    if (contract != null) {
      company = contract.getCompany();
      tradingName = contract.getTradingName();
      partner = contract.getPartner();
      isPurchase = contract.getTargetTypeSelect() == ContractRepository.SUPPLIER_CONTRACT;
    }

    AnalyticLineModel analyticLineModel =
        new AnalyticLineModel(
            contractLine,
            contractLine.getProduct(),
            AnalyticLineModelInitAccountService.getProductAccount(
                contractLine.getProduct(), company, null, isPurchase),
            company,
            tradingName,
            partner,
            isPurchase,
            contractLine.getExTaxTotal(),
            null);
    analyticLineModel.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_CONTRACT);

    return analyticLineModel;
  }
}
