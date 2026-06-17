/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.period.PeriodControlService;
import com.axelor.apps.account.service.period.PeriodServiceAccount;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.TraceBack;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.ClosePeriodCallableService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.db.Wizard;
import com.axelor.utils.helpers.StringHelper;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

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

      PeriodServiceAccount periodServiceAccount = Beans.get(PeriodServiceAccount.class);
      List<Move> moves = periodServiceAccount.getMoves();
      int anomalyCount = periodServiceAccount.getAnomalyCount();

      if (!CollectionUtils.isEmpty(moves)) {
        response.setView(
            ActionView.define(I18n.get("Moves with anomalies"))
                .model(Wizard.class.getName())
                .add("form", "period-move-anomaly-wizard-form")
                .param("popup", "true")
                .param("popup-save", "false")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .context("_moveIds", StringHelper.getIdListString(moves))
                .context("_moveCount", moves.size())
                .context("_anomalyCount", anomalyCount)
                .map());
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showMoves(ActionRequest request, ActionResponse response) {
    try {
      String moveIds = (String) request.getContext().get("_moveIds");
      response.setView(
          ActionView.define(I18n.get("Moves with anomalies"))
              .model(Move.class.getName())
              .add("grid", "move-grid")
              .add("form", "move-form")
              .domain("self.id IN (" + moveIds + ")")
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showAnomalies(ActionRequest request, ActionResponse response) {
    try {
      String moveIds = (String) request.getContext().get("_moveIds");
      int anomalyCount = (int) request.getContext().get("_anomalyCount");
      List<TraceBack> anomalies =
          Beans.get(PeriodServiceAccount.class).getAnomalies(moveIds, anomalyCount);
      response.setView(
          ActionView.define(I18n.get("Anomalies"))
              .model(TraceBack.class.getName())
              .add("grid", "trace-back-grid")
              .add("form", "trace-back-form")
              .domain("self.id IN (" + StringHelper.getIdListString(anomalies) + ")")
              .map());
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
