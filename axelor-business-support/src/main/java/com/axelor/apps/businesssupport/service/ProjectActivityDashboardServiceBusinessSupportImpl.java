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
package com.axelor.apps.businesssupport.service;

import com.axelor.apps.businesssupport.db.ProjectAnnouncement;
import com.axelor.apps.businesssupport.db.repo.ProjectAnnouncementRepository;
import com.axelor.apps.hr.service.project.ProjectActivityDashboardServiceHRImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.mail.db.MailMessage;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectActivityDashboardServiceBusinessSupportImpl
    extends ProjectActivityDashboardServiceHRImpl
    implements ProjectActivityDashboardBusinessSupportService {

  @Inject protected ProjectAnnouncementRepository projectAnnouncementRepo;

  @Override
  protected String getActionLink(String model) {
    if (ProjectAnnouncement.class.getName().equals(model)) {
      return "#/ds/project.announcement/edit/";
    }
    return super.getActionLink(model);
  }

  @Override
  protected List<String> getRelatedModels() {
    List<String> relatedModelsList = super.getRelatedModels();
    relatedModelsList.add(ProjectAnnouncement.class.getName());
    return relatedModelsList;
  }

  @Override
  protected Project getActivityProject(
      Project project, MailMessage message, Set<Long> projectIdSet) {
    if (ProjectAnnouncement.class.getName().equals(message.getRelatedModel())) {
      ProjectAnnouncement announcement = projectAnnouncementRepo.find(message.getRelatedId());
      if (announcement != null) {
        Project announcementproject = announcement.getProject();
        if (project == null
            || (announcementproject != null
                && projectIdSet.contains(announcementproject.getId()))) {
          return announcementproject;
        }
      }
    }
    return super.getActivityProject(project, message, projectIdSet);
  }

  @Override
  protected Map<String, Object> getModelWithUtilityClass(MailMessage message) {
    Map<String, Object> dataMap = super.getModelWithUtilityClass(message);
    if (ProjectAnnouncement.class.getName().equals(message.getRelatedModel())) {
      dataMap.put("modelName", "Project announcement");
      dataMap.put("utilityClass", "label-info");
    }
    return dataMap;
  }

  @Override
  public ActionResponse getProjectActivityView(Long announcementId) {
    ActionResponse response = new ActionResponse();
    response.setView(
        ActionView.define(I18n.get("Project Activity"))
            .model(Wizard.class.getName())
            .add("form", "project-activity-announcement-dashboard-form")
            .context("announcementDate", projectAnnouncementRepo.find(announcementId).getDate())
            .map());

    return response;
  }
}
