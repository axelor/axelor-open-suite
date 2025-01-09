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
package com.axelor.apps.contract.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.apps.contract.service.ConsumptionLineService;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Optional;

public class ConsumptionLineController {

  public void changeProduct(ActionRequest request, ActionResponse response) {
    ConsumptionLine line = request.getContext().asType(ConsumptionLine.class);
    try {
      Beans.get(ConsumptionLineService.class).fill(line, line.getProduct());
      response.setValues(line);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkConsumptionLineQuantity(ActionRequest request, ActionResponse response) {
    Contract contract = request.getContext().getParent().asType(Contract.class);
    ConsumptionLine consumptionLine = request.getContext().asType(ConsumptionLine.class);
    Optional<BigDecimal> initQt =
        Optional.ofNullable(request.getContext())
            .map(context -> context.get("initQty"))
            .map(String::valueOf)
            .map(BigDecimal::new);
    Optional<Integer> initProductId =
        Optional.ofNullable(request.getContext())
            .map(context -> (LinkedHashMap) context.get("initProduct"))
            .map(linkedHashMap -> (Integer) linkedHashMap.get("id"));

    if (consumptionLine.getProduct() == null
        || contract.getCurrentContractVersion().getContractLineList().isEmpty()) {
      return;
    }
    try {
      boolean isSendAlert =
          Beans.get(ContractService.class)
              .checkConsumptionLineQuantity(
                  contract,
                  consumptionLine,
                  initQt.orElse(BigDecimal.ZERO),
                  initProductId.orElse(null));
      if (isSendAlert) {
        response.setInfo(
            I18n.get(ContractExceptionMessage.CONTRACT_QUANTITIES_EXCEED_MAX), I18n.get("Warning"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
