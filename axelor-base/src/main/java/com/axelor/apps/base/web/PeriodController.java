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

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PeriodController {

  public void close(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    period = Beans.get(PeriodRepository.class).find(period.getId());

    try {
      Beans.get(PeriodService.class).close(period);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void closeTemporarily(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    period = Beans.get(PeriodRepository.class).find(period.getId());

    try {
      Beans.get(PeriodService.class).closeTemporarily(period);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void adjust(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    period = Beans.get(PeriodRepository.class).find(period.getId());

    try {
      Beans.get(PeriodService.class).adjust(period);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateTempClosure(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    try {
      Beans.get(PeriodService.class).validateTempClosure(period);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }

  public void validateClosure(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    try {
      Beans.get(PeriodService.class).validateClosure(period);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void open(ActionRequest request, ActionResponse response) {
    Period period =
        Beans.get(PeriodRepository.class).find(request.getContext().asType(Period.class).getId());
    try {
      if (period != null) {
        Beans.get(PeriodService.class).openPeriod(period);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
