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
package com.axelor.apps.intervention.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.repo.CustomerRequestRepository;
import com.axelor.apps.intervention.service.CustomerRequestService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CustomerRequestController {

  public void takeIntoAccount(ActionRequest request, ActionResponse response) {
    try {
      CustomerRequest customerRequest = request.getContext().asType(CustomerRequest.class);
      customerRequest = Beans.get(CustomerRequestRepository.class).find(customerRequest.getId());
      Beans.get(CustomerRequestService.class).takeIntoAccount(customerRequest);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnIntervention(ActionRequest request, ActionResponse response) {
    try {
      CustomerRequest customerRequest = request.getContext().asType(CustomerRequest.class);
      customerRequest = Beans.get(CustomerRequestRepository.class).find(customerRequest.getId());
      Intervention intervention =
          Beans.get(CustomerRequestService.class).createAnIntervention(customerRequest);
      response.setReload(true);
      response.setView(
          ActionView.define(I18n.get("Intervention"))
              .model(Intervention.class.getName())
              .add("form", "intervention-form")
              .add("grid", "intervention-grid")
              .context("_showRecord", String.valueOf(intervention.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateSaleOrder(ActionRequest request, ActionResponse response) {
    try {
      CustomerRequest customerRequest = request.getContext().asType(CustomerRequest.class);
      customerRequest = Beans.get(CustomerRequestRepository.class).find(customerRequest.getId());
      if (customerRequest == null) {
        return;
      }
      SaleOrder saleOrder =
          Beans.get(CustomerRequestService.class).generateSaleOrder(customerRequest);
      if (saleOrder != null) {
        response.setView(
            ActionView.define(I18n.get("Sale order"))
                .model(SaleOrder.class.getName())
                .add("form", "sale-order-form")
                .add("grid", "sale-order-grid")
                .context("_showRecord", saleOrder.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
