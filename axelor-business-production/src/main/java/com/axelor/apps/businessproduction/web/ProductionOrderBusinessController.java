/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproduction.web;

import com.axelor.apps.businessproduction.service.ProductionOrderSaleOrderServiceBusinessImpl;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductionOrderBusinessController {

  public void generateSaleOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ProductionOrder productionOrder = request.getContext().asType(ProductionOrder.class);

    Beans.get(ProductionOrderSaleOrderServiceBusinessImpl.class)
        .createSaleOrder(Beans.get(ProductionOrderRepository.class).find(productionOrder.getId()));

    response.setReload(true);
  }
}
