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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.SubrogationReleaseService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SubrogationReleaseController {

  public void retrieveInvoices(ActionRequest request, ActionResponse response) {
    try {
      SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
      Company company = subrogationRelease.getCompany();
      List<Invoice> invoiceList =
          Beans.get(SubrogationReleaseService.class).retrieveInvoices(company);
      response.setValue("invoiceSet", invoiceList);
    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }

  public void transmitRelease(ActionRequest request, ActionResponse response) {
    try {
      SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
      subrogationRelease =
          Beans.get(SubrogationReleaseRepository.class).find(subrogationRelease.getId());
      Beans.get(SubrogationReleaseService.class).transmitRelease(subrogationRelease);
      response.setReload(true);
    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }

  public void exportToCSV(ActionRequest request, ActionResponse response) {
    try {
      SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
      Beans.get(SubrogationReleaseService.class).exportToCSV(subrogationRelease);
      response.setReload(true);
    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }

  public void enterReleaseInTheAccounts(ActionRequest request, ActionResponse response) {
    try {
      SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
      subrogationRelease =
          Beans.get(SubrogationReleaseRepository.class).find(subrogationRelease.getId());
      Beans.get(SubrogationReleaseService.class).enterReleaseInTheAccounts(subrogationRelease);
      response.setReload(true);
    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }

  public void displayMoveLines(ActionRequest request, ActionResponse response) {
    try {
      SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);

      List<Long> moveLineIdList = new ArrayList<Long>();
      for (Move move : subrogationRelease.getMoveList()) {
        for (MoveLine moveLine : move.getMoveLineList()) {
          moveLineIdList.add(moveLine.getId());
        }
      }
      response.setView(
          ActionView.define(I18n.get("MoveLines"))
              .model(MoveLine.class.getName())
              .add("grid", "move-line-grid")
              .add("form", "move-line-form")
              .param("search-filters", "move-line-filters")
              .domain("self.id in (" + Joiner.on(",").join(moveLineIdList) + ")")
              .map());

    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }
}
