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
package com.axelor.apps.web;

import com.axelor.apps.sale.db.PriceStudy;
import com.axelor.apps.service.PriceStudyService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PriceStudyController {

  public void onPriceChange(ActionRequest request, ActionResponse response) {

    PriceStudy priceStudy = request.getContext().asType(PriceStudy.class);
    Beans.get(PriceStudyService.class).onPriceChange(priceStudy);
    response.setValues(priceStudy);
  }

  public void onGeneralExpensesChange(ActionRequest request, ActionResponse response) {

    PriceStudy priceStudy = request.getContext().asType(PriceStudy.class);
    Beans.get(PriceStudyService.class).onGeneralExpensesChange(priceStudy);
    response.setValues(priceStudy);
  }

  public void onMargeChange(ActionRequest request, ActionResponse response) {

    PriceStudy priceStudy = request.getContext().asType(PriceStudy.class);
    Beans.get(PriceStudyService.class).onMargeChange(priceStudy);
    response.setValues(priceStudy);
  }
}
