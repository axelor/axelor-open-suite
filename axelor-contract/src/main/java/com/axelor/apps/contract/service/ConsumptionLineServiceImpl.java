/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
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
    Preconditions.checkNotNull(product, I18n.get(IExceptionMessage.CONTRACT_EMPTY_PRODUCT));
    line.setLineDate(
        appBaseService.getTodayDate(
            Optional.ofNullable(line.getContractLine())
                .map(ContractLine::getContractVersion)
                .map(ContractVersion::getContract)
                .map(Contract::getCompany)
                .orElse(AuthUtils.getUser().getActiveCompany())));
    line.setProduct(product);
    line.setReference(product.getName());
    line.setUnit(product.getUnit());
    return line;
  }
}
