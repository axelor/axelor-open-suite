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
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class SequenceController {

  public void getDefaultTitle(ActionRequest request, ActionResponse response) {
    Sequence sequence = request.getContext().asType(Sequence.class);
    if (!Strings.isNullOrEmpty(sequence.getCodeSelect())) {
      String defautlTitle = Beans.get(SequenceService.class).getDefaultTitle(sequence);
      response.setValue("name", I18n.get(defautlTitle));
    }
  }

  public void computeFullName(ActionRequest request, ActionResponse response) {
    Sequence sequence = request.getContext().asType(Sequence.class);
    String fullName = Beans.get(SequenceService.class).computeFullName(sequence);
    response.setValue("fullName", fullName);
  }

  public void updateSequenceVersionsMonthly(ActionRequest request, ActionResponse response) {
    try {
      Sequence sequence = request.getContext().asType(Sequence.class);
      SequenceService sequenceService = Beans.get(SequenceService.class);
      LocalDate todayDate = Beans.get(AppBaseService.class).getTodayDate(sequence.getCompany());
      LocalDate endOfDate = todayDate.withDayOfMonth(todayDate.lengthOfMonth());
      if (sequence.getMonthlyResetOk()) {
        response.setValue(
            "sequenceVersionList",
            sequenceService.updateSequenceVersions(sequence, todayDate, endOfDate));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateSequenceVersionsYearly(ActionRequest request, ActionResponse response) {
    try {
      Sequence sequence = request.getContext().asType(Sequence.class);
      SequenceService sequenceService = Beans.get(SequenceService.class);
      LocalDate todayDate = Beans.get(AppBaseService.class).getTodayDate(sequence.getCompany());
      LocalDate endOfDate = todayDate.withDayOfYear(todayDate.lengthOfYear());
      if (sequence.getYearlyResetOk()) {
        response.setValue(
            "sequenceVersionList",
            sequenceService.updateSequenceVersions(sequence, todayDate, endOfDate));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void isLengthValid(ActionRequest request, ActionResponse response) {

    try {
      Sequence sequence = request.getContext().asType(Sequence.class);
      sequence = EntityHelper.getEntity(sequence);
      Beans.get(SequenceService.class).checkSequenceLengthValidity(sequence);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
