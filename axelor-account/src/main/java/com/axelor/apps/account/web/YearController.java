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

import com.axelor.apps.account.service.YearControlService;
import com.axelor.apps.account.service.YearServiceAccountImpl;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.db.Wizard;
import com.google.inject.Singleton;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@Singleton
public class YearController {

  public void close(ActionRequest request, ActionResponse response) {
    Year year = request.getContext().asType(Year.class);

    try {
      YearServiceAccountImpl yearServiceAccount = Beans.get(YearServiceAccountImpl.class);
      yearServiceAccount.closeYearProcess(year);

      Pair<Set<Long>, Integer> anomalies = yearServiceAccount.getClosureAnomalies();
      Set<Long> moveIdSet = anomalies.getLeft();
      if (CollectionUtils.isNotEmpty(moveIdSet)) {
        response.setView(
            ActionView.define(I18n.get("Moves with anomalies"))
                .model(Wizard.class.getName())
                .add("form", "year-move-anomaly-wizard-form")
                .param("popup", "true")
                .param("popup-save", "false")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .context("_moveIds", StringUtils.join(moveIdSet, ","))
                .context("_moveCount", moveIdSet.size())
                .context("_anomalyCount", anomalies.getRight())
                .map());
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void adjust(ActionRequest request, ActionResponse response) {
    Year year = request.getContext().asType(Year.class);

    try {
      Beans.get(YearServiceAccountImpl.class).adjust(year);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void controlDates(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(YearControlService.class).controlDates(request.getContext().asType(Year.class));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setReadOnly(ActionRequest request, ActionResponse response) {
    try {
      Year year =
          Beans.get(YearRepository.class).find(request.getContext().asType(Year.class).getId());
      if (year != null) {
        Boolean isInMove = Beans.get(YearControlService.class).isLinkedToMove(year);
        response.setAttr("fromDate", "readonly", isInMove);
        response.setAttr("toDate", "readonly", isInMove);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
