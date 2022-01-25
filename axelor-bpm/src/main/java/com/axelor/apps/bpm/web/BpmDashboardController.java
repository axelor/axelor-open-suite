/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.web;

import com.axelor.apps.bpm.service.BpmDashboardService;
import com.axelor.apps.bpm.service.BpmDashboardServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class BpmDashboardController {

  public void getBpmManagerData(ActionRequest request, ActionResponse response) {
    this.getData(0, response);
  }

  public void getPreManagerData(ActionRequest request, ActionResponse response) {
    this.getData(this.getOffset(request, false), response);
  }

  public void getNxtManagerData(ActionRequest request, ActionResponse response) {
    this.getData(this.getOffset(request, true), response);
  }

  private int getOffset(ActionRequest request, boolean isNext) {
    if (isNext) {
      return (int) request.getContext().get("offset") + BpmDashboardServiceImpl.FETCH_LIMIT;
    } else {
      return (int) request.getContext().get("offset") - BpmDashboardServiceImpl.FETCH_LIMIT;
    }
  }

  private void getData(int offset, ActionResponse response) {
    response.setValues(Beans.get(BpmDashboardService.class).getData(offset));
  }
}
