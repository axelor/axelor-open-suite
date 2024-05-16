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
import com.axelor.apps.hr.service.project.ProjectDashboardHRServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectDashboardBusinessSupportServiceImpl extends ProjectDashboardHRServiceImpl {

  @Inject protected ProjectRepository projectRepo;
  @Inject protected ProjectService projectService;

  @Override
  public Map<String, Object> getData(Project project) {
    Map<String, Object> dataMap = super.getData(project);
    dataMap.put("$announcementList", getLatestNews(project));
    return dataMap;
  }

  protected List<Map<String, Object>> getLatestNews(Project project) {
    List<Map<String, Object>> newsList = new ArrayList<>();
    List<ProjectAnnouncement> announcementList = new ArrayList<>();

    projectRepo.all().filter("self.id IN ?1", projectService.getContextProjectIds()).fetch()
        .stream()
        .forEach(subProject -> announcementList.addAll(subProject.getAnnouncementList()));

    for (ProjectAnnouncement announcement :
        announcementList.stream()
            .sorted(Comparator.comparing(ProjectAnnouncement::getDate).reversed())
            .limit(6)
            .collect(Collectors.toList())) {
      Map<String, Object> announcementMap = new HashMap<>();
      announcementMap.put("id", announcement.getId());
      announcementMap.put("title", announcement.getTitle());
      announcementMap.put("createdById", announcement.getCreatedBy().getId());
      announcementMap.put("createdBy", announcement.getCreatedBy().getFullName());
      announcementMap.put("announcementDate", announcement.getDate());
      newsList.add(announcementMap);
    }
    return newsList;
  }
}
