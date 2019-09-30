/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.LogisticalFormError;
import com.axelor.apps.stock.exception.LogisticalFormWarning;
import com.axelor.apps.stock.service.LogisticalFormService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class LogisticalFormController {

  public void addStockMove(ActionRequest request, ActionResponse response) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> stockMoveMap =
          (Map<String, Object>) request.getContext().get("stockMove");
      if (stockMoveMap != null) {
        StockMove stockMove = Mapper.toBean(StockMove.class, stockMoveMap);
        stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());

        if (stockMove.getStockMoveLineList() != null) {
          LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
          LogisticalFormService logisticalFormService = Beans.get(LogisticalFormService.class);

          logisticalFormService.addDetailLines(logisticalForm, stockMove);
          response.setValue("logisticalFormLineList", logisticalForm.getLogisticalFormLineList());
          response.setValue("$stockMove", null);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotals(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
      Beans.get(LogisticalFormService.class).computeTotals(logisticalForm);
      response.setValue("totalNetMass", logisticalForm.getTotalNetMass());
      response.setValue("totalGrossMass", logisticalForm.getTotalGrossMass());
      response.setValue("totalVolume", logisticalForm.getTotalVolume());
    } catch (LogisticalFormError e) {
      response.setError(e.getLocalizedMessage());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkLines(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
      LogisticalFormService logisticalFormService = Beans.get(LogisticalFormService.class);

      logisticalFormService.sortLines(logisticalForm);
      logisticalFormService.checkLines(logisticalForm);
    } catch (LogisticalFormWarning e) {
      response.setAlert(e.getLocalizedMessage());
    } catch (LogisticalFormError e) {
      response.setError(e.getLocalizedMessage());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setStockMoveDomain(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
      String domain = Beans.get(LogisticalFormService.class).getStockMoveDomain(logisticalForm);
      response.setAttr("$stockMove", "domain", domain);

      if (logisticalForm.getDeliverToCustomerPartner() == null) {
        response.setNotify(I18n.get("Deliver to customer is not set."));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void processCollected(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
      logisticalForm = Beans.get(LogisticalFormRepository.class).find(logisticalForm.getId());
      Beans.get(LogisticalFormService.class).processCollected(logisticalForm);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setCustomerAccountNumberToCarrier(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
      Optional<String> customerAccountNumberToCarrier =
          Beans.get(LogisticalFormService.class).getCustomerAccountNumberToCarrier(logisticalForm);
      response.setValue(
          "customerAccountNumberToCarrier", customerAccountNumberToCarrier.orElse(null));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void refreshProductNetMass(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
      LogisticalFormService logisticalFormService = Beans.get(LogisticalFormService.class);
      logisticalFormService.updateProductNetMass(logisticalForm);
      response.setValue("logisticalFormLineList", logisticalForm.getLogisticalFormLineList());
      response.setValue("totalNetMass", logisticalForm.getTotalNetMass());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
