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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.db.ProjectFolder;
import com.axelor.apps.businessproject.report.ITranslation;
import com.axelor.apps.businessproject.service.ProjectFolderService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ProjectFolderController {

  public void printProjectsPlanificationAndCost(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ProjectFolder projectFolder = request.getContext().asType(ProjectFolder.class);
    String fileLink;
    String title;

    try {
      fileLink =
          Beans.get(ProjectFolderService.class).printProjectsPlanificationAndCost(projectFolder);
      title = I18n.get(ITranslation.PROJECT_REPORT_TITLE_FOR_PLANIFICATION_AND_COST);
      response.setView(ActionView.define(title).add("html", fileLink).map());

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printProjectsFinancialReport(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ProjectFolder projectFolder = request.getContext().asType(ProjectFolder.class);
    String fileLink;
    String title;

    try {
      fileLink = Beans.get(ProjectFolderService.class).printProjectFinancialReport(projectFolder);
      title = I18n.get(ITranslation.PROJECT_REPORT_TITLE_FOR_FINANCIAL);
      response.setView(ActionView.define(title).add("html", fileLink).map());

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
