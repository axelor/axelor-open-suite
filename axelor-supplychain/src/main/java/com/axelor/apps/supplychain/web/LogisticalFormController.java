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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.supplychain.service.LogisticalFormSupplychainService;
import com.axelor.apps.supplychain.service.packaging.PackagingStockMoveLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class LogisticalFormController {

  public void refreshLogisticalForm(ActionRequest request, ActionResponse response) {
    response.setSignal("refresh-tab", true);
  }

  public void validateAndUpdateStockMoveList(ActionRequest request, ActionResponse response)
      throws AxelorException {
    LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
    LogisticalForm savedLogisticalForm = null;
    if (logisticalForm.getId() != null) {
      savedLogisticalForm = Beans.get(LogisticalFormRepository.class).find(logisticalForm.getId());
    }
    String error =
        Beans.get(PackagingStockMoveLineService.class)
            .validateAndUpdateStockMoveList(savedLogisticalForm, logisticalForm);
    if (!error.isEmpty()) {
      response.setError(error);
      response.setValue("stockMoveList", savedLogisticalForm.getStockMoveList());
      return;
    }
    if (savedLogisticalForm == null) {
      response.setValue("packagingList", logisticalForm.getPackagingList());
    } else {
      response.setValue("packagingList", savedLogisticalForm.getPackagingList());
    }
  }

  public void processCollected(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
      logisticalForm = Beans.get(LogisticalFormRepository.class).find(logisticalForm.getId());
      Beans.get(LogisticalFormSupplychainService.class).processCollected(logisticalForm);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
