/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
import com.google.inject.Singleton;

@Singleton
public class ConfiguratorController {

  protected static final String saleOrderContextIdKey = "_saleOrderId";

  /**
   * Called from configurator form view, set values for the indicators JSON field. call {@link
   * ConfiguratorService#updateIndicators(Configurator, JsonContext, JsonContext, Long)}
   *
   * @param request
   * @param response
   */
  public void updateIndicators(ActionRequest request, ActionResponse response) {
    Configurator configurator = request.getContext().asType(Configurator.class);
    JsonContext jsonAttributes = (JsonContext) request.getContext().get("$attributes");
    JsonContext jsonIndicators = (JsonContext) request.getContext().get("$indicators");
    configurator = Beans.get(ConfiguratorRepository.class).find(configurator.getId());
    try {
      Beans.get(ConfiguratorService.class)
          .updateIndicators(
              configurator, jsonAttributes, jsonIndicators, getSaleOrderId(request.getContext()));
      response.setValue("indicators", request.getContext().get("indicators"));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from configurator form view, call {@link
   * ConfiguratorService#generateProduct(Configurator, JsonContext, JsonContext, Long)}
   *
   * @param request
   * @param response
   */
  public void generateProduct(ActionRequest request, ActionResponse response) {
    Configurator configurator = request.getContext().asType(Configurator.class);
    JsonContext jsonAttributes = (JsonContext) request.getContext().get("$attributes");
    JsonContext jsonIndicators = (JsonContext) request.getContext().get("$indicators");
    configurator = Beans.get(ConfiguratorRepository.class).find(configurator.getId());
    try {
      Beans.get(ConfiguratorService.class)
          .generateProduct(
              configurator, jsonAttributes, jsonIndicators, getSaleOrderId(request.getContext()));
      response.setReload(true);
      if (configurator.getProduct() != null) {
        response.setView(
            ActionView.define(I18n.get("Product generated"))
                .model(Product.class.getName())
                .add("form", "product-form")
                .add("grid", "product-grid")
                .param("search-filters", "products-filters")
                .context("_showRecord", configurator.getProduct().getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setError(e.getMessage());
    }
  }

  /**
   * Called from configurator wizard form view, call {@link
   * ConfiguratorService#addLineToSaleOrder(Configurator, SaleOrder, JsonContext, JsonContext)}
   *
   * @param request
   * @param response
   */
  public void generateForSaleOrder(ActionRequest request, ActionResponse response) {
    Configurator configurator = request.getContext().asType(Configurator.class);
    long saleOrderId = getSaleOrderId(request.getContext());

    JsonContext jsonAttributes = (JsonContext) request.getContext().get("$attributes");
    JsonContext jsonIndicators = (JsonContext) request.getContext().get("$indicators");

    configurator = Beans.get(ConfiguratorRepository.class).find(configurator.getId());
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    try {
      Beans.get(ConfiguratorService.class)
          .addLineToSaleOrder(configurator, saleOrder, jsonAttributes, jsonIndicators);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from configurator view on selecting {@link Configurator#configuratorCreator}.
   *
   * @param request
   * @param response
   */
  public void createDomainForCreator(ActionRequest request, ActionResponse response) {
    response.setAttr(
        "configuratorCreator",
        "domain",
        Beans.get(ConfiguratorCreatorService.class).getConfiguratorCreatorDomain());
  }

  protected Long getSaleOrderId(Context context) {
    Integer saleOrderIdInt = (Integer) context.get(saleOrderContextIdKey);
    if (saleOrderIdInt != null) {
      return saleOrderIdInt.longValue();
    } else {
      return null;
    }
  }
}
