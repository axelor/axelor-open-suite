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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisClass;
import com.axelor.apps.base.db.repo.ABCAnalysisRepository;
import com.axelor.apps.base.service.ABCAnalysisService;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import java.util.List;

public class ABCAnalysisController {

  @SuppressWarnings("unchecked")
  public void runAnalysis(ActionRequest request, ActionResponse response) {
    ABCAnalysis abcAnalysis = request.getContext().asType(ABCAnalysis.class);
    try {
      Class<? extends ABCAnalysisServiceImpl> clazz =
          (Class<? extends ABCAnalysisServiceImpl>) Class.forName(abcAnalysis.getTypeSelect());
      Beans.get(clazz)
          .runAnalysis(Beans.get(ABCAnalysisRepository.class).find(abcAnalysis.getId()));
      response.setReload(true);
    } catch (ClassNotFoundException | AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void initABCClasses(ActionRequest request, ActionResponse response) {
    List<ABCAnalysisClass> abcAnalysisClassList =
        Beans.get(ABCAnalysisService.class).initABCClasses();
    response.setValue("abcAnalysisClassList", abcAnalysisClassList);
  }

  public void setSequence(ActionRequest request, ActionResponse response) {
    ABCAnalysis abcAnalysis = request.getContext().asType(ABCAnalysis.class);
    try {
      Beans.get(ABCAnalysisServiceImpl.class).setSequence(abcAnalysis);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
    response.setValue("abcAnalysisSeq", abcAnalysis.getAbcAnalysisSeq());
  }

  public void printReport(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    Long abcAnalysisId = Long.parseLong(context.get("_abcAnalysisId").toString());
    String reportType = (String) context.get("reportTypeSelect");
    ABCAnalysis abcAnalysis = Beans.get(ABCAnalysisRepository.class).find(abcAnalysisId);
    try {
      String name = I18n.get("ABC Analysis N°") + " " + abcAnalysis.getAbcAnalysisSeq();
      String fileLink =
          Beans.get(ABCAnalysisServiceImpl.class).printReport(abcAnalysis, reportType);
      if (!Strings.isNullOrEmpty(fileLink)) {
        response.setView(ActionView.define(name).add("html", fileLink).map());
      }
    } catch (AxelorException e) {
      response.setError(e.getMessage());
    }
    response.setCanClose(true);
  }

  public void checkClasses(ActionRequest request, ActionResponse response) {
    ABCAnalysis abcAnalysis = request.getContext().asType(ABCAnalysis.class);

    try {
      Beans.get(ABCAnalysisServiceImpl.class).checkClasses(abcAnalysis);
    } catch (AxelorException e) {
      response.setError(e.getMessage());
    }
  }
}
