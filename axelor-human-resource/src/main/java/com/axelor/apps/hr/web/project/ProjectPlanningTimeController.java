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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.service.project.PlannedTimeValueService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ProjectPlanningTimeController {

  public void addMultipleProjectPlanningTime(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    Beans.get(ProjectPlanningTimeService.class).addMultipleProjectPlanningTime(context);

    response.setCanClose(true);
  }

  public void addSingleProjectPlanningTime(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);
    Beans.get(ProjectPlanningTimeService.class)
        .addSingleProjectPlanningTime(
            JPA.find(ProjectPlanningTime.class, projectPlanningTime.getId()));

    response.setCanClose(true);
  }

  @SuppressWarnings("unchecked")
  public void removeProjectPlanningTime(ActionRequest request, ActionResponse response) {

    List<Integer> projectPlanningTimeLineIds = (List<Integer>) request.getContext().get("_ids");

    if (projectPlanningTimeLineIds != null) {
      Beans.get(ProjectPlanningTimeService.class)
          .removeProjectPlanningLines(projectPlanningTimeLineIds);
    }

    response.setReload(true);
  }

  public void removeSingleProjectPlanningTime(ActionRequest request, ActionResponse response) {

    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    if (projectPlanningTime != null) {
      Beans.get(ProjectPlanningTimeService.class)
          .removeProjectPlanningLine(
              JPA.find(ProjectPlanningTime.class, projectPlanningTime.getId()));
    }

    response.setReload(true);
  }

  public void setDefaultSiteFromProjectTask(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    if (Beans.get(AppBaseService.class).getAppBase().getEnableSiteManagementForProject()) {
      Map<String, Object> objMap = (Map) context.get("projectTask");
      if (objMap == null) {
        return;
      }
      ProjectTask projectTask =
          JPA.find(ProjectTask.class, Long.parseLong(objMap.get("id").toString()));
      response.setValue(
          "site", Optional.ofNullable(projectTask).map(ProjectTask::getSite).orElse(null));
    }
  }

  public void updateEvent(ActionRequest request, ActionResponse response) {
    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    Beans.get(ProjectPlanningTimeService.class).updateLinkedEvent(projectPlanningTime);
  }

  public void computePlannedTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    response.setValue(
        "plannedTime",
        Beans.get(ProjectPlanningTimeService.class).computePlannedTime(projectPlanningTime));
    Optional<Company> optCompany =
        Optional.ofNullable(projectPlanningTime)
            .map(ProjectPlanningTime::getProject)
            .map(Project::getCompany);
    if (optCompany.isPresent()
        && Beans.get(ProjectConfigService.class)
            .getProjectConfig(optCompany.get())
            .getIsSelectionOnDisplayPlannedTime()) {
      if (projectPlanningTime.getDisplayPlannedTimeRestricted() != null) {
        response.setValue(
            "displayPlannedTime",
            Optional.of(projectPlanningTime)
                .map(ProjectPlanningTime::getDisplayPlannedTimeRestricted)
                .map(PlannedTimeValue::getPlannedTime)
                .orElse(BigDecimal.ZERO));
      }
    } else {
      response.setValue(
          "displayPlannedTimeRestricted",
          Beans.get(PlannedTimeValueService.class)
              .createPlannedTimeValue(projectPlanningTime.getDisplayPlannedTime()));
    }
  }

  public void computeDisplayTimeUnitDomain(ActionRequest request, ActionResponse response) {
    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    response.setAttr(
        "displayTimeUnit",
        "domain",
        Beans.get(ProjectPlanningTimeService.class)
            .computeDisplayTimeUnitDomain(projectPlanningTime));
  }

  public void computeDisplayPlannedTimeRestrictedDomain(
      ActionRequest request, ActionResponse response) throws AxelorException {
    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    response.setAttr(
        "displayPlannedTimeRestricted",
        "domain",
        Beans.get(ProjectPlanningTimeService.class)
            .computeDisplayPlannedTimeRestrictedDomain(projectPlanningTime));
  }

  public void getCompanyConfig(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    Optional<Company> optCompany =
        Optional.ofNullable(projectPlanningTime)
            .map(ProjectPlanningTime::getProject)
            .map(Project::getCompany);
    if (optCompany.isPresent()) {
      response.setValue(
          "$isSelectionOnDisplayPlannedTime",
          Beans.get(ProjectConfigService.class)
              .getProjectConfig(optCompany.get())
              .getIsSelectionOnDisplayPlannedTime());
    }
  }

  public void getDefaultPlanningTime(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);
    ProjectPlanningTimeService projectPlanningTimeService =
        Beans.get(ProjectPlanningTimeService.class);

    response.setValue(
        "displayPlannedTime",
        projectPlanningTimeService.getDefaultPlanningTime(projectPlanningTime));
    response.setValue(
        "displayPlannedTimeRestricted",
        projectPlanningTimeService.getDefaultPlanningRestrictedTime(projectPlanningTime));
  }

  public void updateDatesFromSprint(ActionRequest request, ActionResponse response) {

    ProjectPlanningTime projectPlanningTime =
        request.getContext().asType(ProjectPlanningTime.class);

    ProjectTask projectTask = projectPlanningTime.getProjectTask();

    if (projectTask != null) {
      Sprint sprint = projectTask.getSprint();

      if (sprint != null && sprint.getFromDate() != null && sprint.getToDate() != null) {
        response.setValue("startDateTime", sprint.getFromDate());
        response.setValue("endDateTime", sprint.getToDate());
      }
    }
  }
}
