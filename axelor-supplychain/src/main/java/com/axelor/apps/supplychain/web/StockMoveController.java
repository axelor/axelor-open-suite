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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.PartnerSupplychainLinkTypeRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.PartnerSupplychainLinkService;
import com.axelor.apps.supplychain.service.StockMoveReservedQtyService;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class StockMoveController {

  public void verifyProductStock(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      if (stockMove.getPickingIsEdited() && !stockMove.getAvailabilityRequest()) {
        response.setValue("availabilityRequest", true);
        response.setInfo(
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_AVAILABILITY_REQUEST_NOT_UPDATABLE));
        return;
      }
      Beans.get(StockMoveServiceSupplychain.class).verifyProductStock(stockMove);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setValue("availabilityRequest", false);
    }
  }

  /**
   * Called from stock move form view, on available qty boolean change. Only called if the user
   * accepted to allocate everything. Call {@link
   * StockMoveReservedQtyService#allocateAll(StockMove)}.
   *
   * @param request
   * @param response
   */
  public void allocateAll(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Company company = stockMove.getCompany();
      if (company == null) {
        return;
      }

      SupplyChainConfig supplyChainConfig =
          Beans.get(SupplyChainConfigService.class).getSupplyChainConfig(company);
      if (!Beans.get(AppSupplychainService.class).getAppSupplychain().getManageStockReservation()
          || !stockMove.getAvailabilityRequest()
          || !supplyChainConfig.getAutoAllocateOnAvailabilityRequest()) {
        return;
      }
      Beans.get(StockMoveReservedQtyService.class).allocateAll(stockMove);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void isAllocatedStockMoveLineRemoved(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    if (stockMove.getId() != null
        && Beans.get(StockMoveServiceSupplychain.class)
            .isAllocatedStockMoveLineRemoved(stockMove)) {
      response.setValue("stockMoveLineList", stockMove.getStockMoveLineList());
      response.setInfo(
          I18n.get(SupplychainExceptionMessage.ALLOCATED_STOCK_MOVE_LINE_DELETED_ERROR));
    }
  }

  /**
   * Called from stock move form view, on delivered partner select. Call {@link
   * PartnerSupplychainLinkService#computePartnerFilter}
   *
   * @param request
   * @param response
   */
  public void setInvoicedPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      String strFilter =
          Beans.get(PartnerSupplychainLinkService.class)
              .computePartnerFilter(
                  stockMove.getPartner(),
                  PartnerSupplychainLinkTypeRepository.TYPE_SELECT_INVOICED_BY);

      response.setAttr("invoicedPartner", "domain", strFilter);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaultInvoicedPartner(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Beans.get(StockMoveServiceSupplychain.class).setDefaultInvoicedPartner(stockMove);
      response.setValues(stockMove);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkInvoiceStatus(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Beans.get(StockMoveServiceSupplychain.class).checkInvoiceStatus(stockMove);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }

  public void setInvoicingStatusInvoicedDelayed(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    Beans.get(StockMoveServiceSupplychain.class).setInvoicingStatusInvoicedDelayed(stockMove);
    response.setReload(true);
  }

  public void setInvoicingStatusInvoicedValidated(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    Beans.get(StockMoveServiceSupplychain.class).setInvoicingStatusInvoicedValidated(stockMove);
    response.setReload(true);
  }
}
