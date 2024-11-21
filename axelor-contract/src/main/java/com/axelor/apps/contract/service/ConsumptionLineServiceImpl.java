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
package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.Optional;

public class ConsumptionLineServiceImpl implements ConsumptionLineService {

  protected AppBaseService appBaseService;

  @Inject
  public ConsumptionLineServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public ConsumptionLine fill(ConsumptionLine line, Product product) {
    line.setLineDate(
        appBaseService.getTodayDate(
            Optional.ofNullable(line.getContractLine())
                .map(ContractLine::getContractVersion)
                .map(ContractVersion::getContract)
                .map(Contract::getCompany)
                .orElse(
                    Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null))));
    if (product != null) {
      line.setReference(product.getName());
      line.setUnit(product.getUnit());
    } else {
      line.setReference(null);
      line.setUnit(null);
    }
    return line;
  }
}
