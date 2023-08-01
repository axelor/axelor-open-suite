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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerPriceList;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PartnerPriceListController {

  /**
   * Called from partner price list form view call {@link
   * PartnerPriceListService#checkDates(PartnerPriceList)} return a warning with the response if
   * dates validation fails for the price list set.
   *
   * @param request
   * @param response
   */
  public void checkDates(ActionRequest request, ActionResponse response) {
    PartnerPriceList partnerPriceList;
    Class partnerOrPriceLists = request.getContext().getContextClass();
    if (partnerOrPriceLists.equals(Partner.class)) {
      partnerPriceList = request.getContext().asType(Partner.class).getSalePartnerPriceList();
    } else if (partnerOrPriceLists.equals(PartnerPriceList.class)) {
      partnerPriceList = request.getContext().asType(PartnerPriceList.class);
    } else {
      return;
    }
    try {
      Beans.get(PartnerPriceListService.class).checkDates(partnerPriceList);
    } catch (Exception e) {
      response.setAlert(e.getMessage());
    }
  }
}
