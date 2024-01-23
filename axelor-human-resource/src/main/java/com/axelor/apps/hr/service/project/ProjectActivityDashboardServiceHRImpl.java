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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.ProjectActivityDashboardServiceImpl;
import com.axelor.mail.db.MailMessage;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectActivityDashboardServiceHRImpl extends ProjectActivityDashboardServiceImpl {

  @Inject protected TimesheetLineRepository timesheetLineRepo;

  @Override
  protected String getActionLink(String model) {
    if (TimesheetLine.class.getName().equals(model)) {
      return "#/ds/project.spent.time/edit/";
    }
    return super.getActionLink(model);
  }

  @Override
  protected List<String> getRelatedModels() {
    List<String> relatedModelsList = super.getRelatedModels();
    relatedModelsList.add(TimesheetLine.class.getName());
    return relatedModelsList;
  }

  @Override
  protected Project getActivityProject(
      Project project, MailMessage message, Set<Long> projectIdSet) {
    if (TimesheetLine.class.getName().equals(message.getRelatedModel())) {
      TimesheetLine timesheetLine = timesheetLineRepo.find(message.getRelatedId());
      if (timesheetLine != null) {
        Project tsLineProject = timesheetLine.getProject();
        if (project == null
            || (tsLineProject != null && projectIdSet.contains(tsLineProject.getId()))) {
          return tsLineProject;
        }
      }
    }
    return super.getActivityProject(project, message, projectIdSet);
  }

  @Override
  protected Map<String, Object> getModelWithUtilityClass(MailMessage message) {
    Map<String, Object> dataMap = super.getModelWithUtilityClass(message);
    if (TimesheetLine.class.getName().equals(message.getRelatedModel())) {
      dataMap.put("modelName", "Timesheet line");
      dataMap.put("utilityClass", "label-important");
    }
    return dataMap;
  }
}
