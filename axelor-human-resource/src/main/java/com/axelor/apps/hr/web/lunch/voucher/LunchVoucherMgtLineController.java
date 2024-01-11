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
package com.axelor.apps.hr.web.lunch.voucher;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class LunchVoucherMgtLineController {

  public void compute(ActionRequest request, ActionResponse response) {

    try {
      LunchVoucherMgtLine line = request.getContext().asType(LunchVoucherMgtLine.class);
      Beans.get(LunchVoucherMgtLineService.class).compute(line);

      response.setValue("lunchVoucherNumber", line.getLunchVoucherNumber());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
