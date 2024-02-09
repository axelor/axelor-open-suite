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
package com.axelor.apps.account.web;

import com.axelor.apps.account.service.PeriodControlService;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.ClosePeriodCallableService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PeriodController {

  public void callClosePeriodService(ActionRequest request, ActionResponse response) {
    try {
      Period period = request.getContext().asType(Period.class);
      period = Beans.get(PeriodRepository.class).find(period.getId());
      ClosePeriodCallableService closePeriodCallableService =
          Beans.get(ClosePeriodCallableService.class);
      closePeriodCallableService.setPeriod(period);
      ControllerCallableTool<Period> controllerCallableTool = new ControllerCallableTool<>();
      controllerCallableTool.runInSeparateThread(closePeriodCallableService, response);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showButtons(ActionRequest request, ActionResponse response) {
    Period period = request.getContext().asType(Period.class);
    User user = AuthUtils.getUser();

    try {
      PeriodServiceAccount periodServiceAccount = Beans.get(PeriodServiceAccount.class);
      if (periodServiceAccount.isTemporarilyClosurePeriodManage(period, user)) {
        response.setAttr(
            "temporarilyCloseBtn",
            "hidden",
            period.getStatusSelect() == PeriodRepository.STATUS_TEMPORARILY_CLOSED
                || period.getStatusSelect() == PeriodRepository.STATUS_CLOSED
                || period.getStatusSelect() == PeriodRepository.STATUS_CLOSURE_IN_PROGRESS);
        response.setAttr(
            "openBtn",
            "hidden",
            !(period.getStatusSelect() == PeriodRepository.STATUS_CLOSED
                    || period.getStatusSelect() == PeriodRepository.STATUS_TEMPORARILY_CLOSED)
                || period.getYear().getStatusSelect() != YearRepository.STATUS_OPENED);
      }
      if (periodServiceAccount.isManageClosedPeriod(period, user)) {
        response.setAttr(
            "closeBtn",
            "hidden",
            period.getStatusSelect() == PeriodRepository.STATUS_CLOSED
                || period.getStatusSelect() == PeriodRepository.STATUS_CLOSURE_IN_PROGRESS);
        response.setAttr(
            "openBtn",
            "hidden",
            !(period.getStatusSelect() == PeriodRepository.STATUS_CLOSED
                    || period.getStatusSelect() == PeriodRepository.STATUS_TEMPORARILY_CLOSED)
                || period.getYear().getStatusSelect() != YearRepository.STATUS_OPENED);
        response.setAttr(
            "adjustBtn",
            "hidden",
            !(period.getStatusSelect() == PeriodRepository.STATUS_CLOSED
                && period.getYear().getStatusSelect() == YearRepository.STATUS_ADJUSTING));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void controlDates(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(PeriodControlService.class).controlDates(request.getContext().asType(Period.class));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setReadOnly(ActionRequest request, ActionResponse response) {
    try {
      Period period =
          Beans.get(PeriodRepository.class).find(request.getContext().asType(Period.class).getId());
      if (period != null) {
        boolean isReadOnly =
            period.getStatusSelect() == PeriodRepository.STATUS_CLOSED
                || period.getStatusSelect() == PeriodRepository.STATUS_TEMPORARILY_CLOSED
                || period.getStatusSelect() == PeriodRepository.STATUS_CLOSURE_IN_PROGRESS;

        Boolean isInMove =
            (Beans.get(PeriodControlService.class).isLinkedToMove(period)
                && Beans.get(PeriodControlService.class).isStatusValid(period));
        response.setAttr("mainPanel", "readonly", isReadOnly);
        response.setAttr("fromDate", "readonly", isReadOnly || isInMove);
        response.setAttr("toDate", "readonly", isReadOnly || isInMove);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
