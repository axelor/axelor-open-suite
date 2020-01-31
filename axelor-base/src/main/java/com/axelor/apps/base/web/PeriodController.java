/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PeriodController {

  private PeriodService periodService;
  private PeriodRepository periodRepository;

  @Inject
  public PeriodController(PeriodService periodService, PeriodRepository periodRepository) {
    this.periodService = periodService;
    this.periodRepository = periodRepository;
  }

  public void close(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    period = periodRepository.find(period.getId());

    try {
      periodService.close(period);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void adjust(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    period = periodRepository.find(period.getId());

    try {
      periodService.adjust(period);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
