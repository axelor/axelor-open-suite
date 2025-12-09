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
package com.axelor.apps.contract.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.contract.db.ContractLine;
import com.google.inject.Inject;
import java.util.ArrayList;

public class AnalyticLineModelFromContractServiceImpl
    implements AnalyticLineModelFromContractService {

  protected AnalyticMoveLineRepository analyticMoveLineRepo;

  @Inject
  public AnalyticLineModelFromContractServiceImpl(AnalyticMoveLineRepository analyticMoveLineRepo) {
    this.analyticMoveLineRepo = analyticMoveLineRepo;
  }

  @Override
  public void copyAnalyticsDataFromContractLine(
      ContractLine contractLine, AnalyticLine analyticLine) {
    if (analyticLine.getAnalyticMoveLineList() == null) {
      analyticLine.setAnalyticMoveLineList(new ArrayList<>());
    }

    analyticLine.setAnalyticDistributionTemplate(contractLine.getAnalyticDistributionTemplate());

    analyticLine.setAxis1AnalyticAccount(contractLine.getAxis1AnalyticAccount());
    analyticLine.setAxis2AnalyticAccount(contractLine.getAxis2AnalyticAccount());
    analyticLine.setAxis3AnalyticAccount(contractLine.getAxis3AnalyticAccount());
    analyticLine.setAxis4AnalyticAccount(contractLine.getAxis4AnalyticAccount());
    analyticLine.setAxis5AnalyticAccount(contractLine.getAxis5AnalyticAccount());

    for (AnalyticMoveLine originalAnalyticMoveLine : contractLine.getAnalyticMoveLineList()) {
      AnalyticMoveLine analyticMoveLine =
          analyticMoveLineRepo.copy(originalAnalyticMoveLine, false);

      analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);
      analyticMoveLine.setContractLine(null);
      analyticLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
  }
}
