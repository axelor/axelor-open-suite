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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PriceListController {

  public void historizePriceList(ActionRequest request, ActionResponse response) {
    PriceList priceList = request.getContext().asType(PriceList.class);
    priceList = Beans.get(PriceListRepository.class).find(priceList.getId());
    priceList = Beans.get(PriceListService.class).historizePriceList(priceList);
    response.setReload(true);
  }

  public void checkPriceListLineList(ActionRequest request, ActionResponse response) {
    PriceList priceList = request.getContext().asType(PriceList.class);
    if (priceList.getPriceListLineList() != null
        && priceList.getPriceListLineList().stream().anyMatch(o -> o.getAnomalySelect() > 0)) {
      response.setAlert(
          I18n.get(
              "Warning, the price list contains at least one product that is not renewed or not available for sale."));
    }
  }

  public void checkDates(ActionRequest request, ActionResponse response) {
    PriceList priceList = request.getContext().asType(PriceList.class);
    try {
      Beans.get(PriceListService.class).checkDates(priceList);
    } catch (Exception e) {
      response.setError(e.getMessage());
    }
  }
}
