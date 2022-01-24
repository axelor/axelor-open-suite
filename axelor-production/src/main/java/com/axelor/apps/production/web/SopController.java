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
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.Sop;
import com.axelor.apps.production.db.repo.SopRepository;
import com.axelor.apps.production.service.SopService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SopController {
  public void generateSOPLines(ActionRequest request, ActionResponse response) {
    Sop sop = request.getContext().asType(Sop.class);
    if (sop.getYear() == null) {
      response.setError("Please specify the year to generate lines.");
    } else {
      try {
        sop = Beans.get(SopRepository.class).find(sop.getId());
        Beans.get(SopService.class).generateSOPLines(sop);
        response.setReload(true);
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }
}
