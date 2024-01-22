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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.WikiRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.mail.MailConstants;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection;
import com.axelor.meta.schema.views.Selection.Option;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProjectActivityDashboardServiceImpl implements ProjectActivityDashboardService {

  @Inject protected MailMessageRepository mailMessageRepo;
  @Inject protected ProjectTaskRepository projectTaskRepo;
  @Inject protected WikiRepository wikiRepo;
  @Inject protected ProjectService projectService;
  @Inject protected ObjectMapper objectMapper;
  @Inject protected ProjectRepository projectRepo;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

  @Override
  public Map<String, Object> getData(LocalDate startDate, LocalDate endDate, Long projectId) {
    Map<String, Object> dataMap = new HashMap<>();

    List<MailMessage> mailMessageList = getMailMessages(startDate, endDate);

    Map<String, List<Map<String, List<Map<String, Object>>>>> activityDataMap =
        new LinkedHashMap<>();

    Project project = null;
    Set<Long> projectIdSet = new HashSet<Long>();

    if (projectId != null) {
      project = projectRepo.find(projectId);
      projectService.getChildProjectIds(projectIdSet, project);
    } else {
      project = AuthUtils.getUser().getContextProject();
      projectIdSet = projectService.getContextProjectIds();
    }

    for (MailMessage message : mailMessageList) {
      LocalDateTime createdOn = message.getCreatedOn();
      String date = getActivityDate(createdOn);

      Map<String, Object> activityMap = new HashMap<>();
      Project activityProject = getActivityProject(project, message, projectIdSet);
      if (activityProject == null) {
        continue;
      }
      activityMap.put(
          "objectLink", getActionLink(message.getRelatedModel()) + message.getRelatedId());
      if (project == null
          || (projectIdSet.contains(activityProject.getId()) && !project.equals(activityProject))) {
        activityMap.put("subProjectName", activityProject.getName());
      }
      activityMap.put("title", message.getRelatedName());
      activityMap.put("time", createdOn);
      activityMap.put("userId", message.getAuthor().getId());
      activityMap.put("user", message.getAuthor().getName());
      activityMap.putAll(getModelWithUtilityClass(message));
      try {
        activityMap.put("activity", getActivity(message));
      } catch (Exception e) {
      }

      List<Map<String, List<Map<String, Object>>>> titleMapList =
          activityDataMap.getOrDefault(date, new ArrayList<>());
      Optional<Map<String, List<Map<String, Object>>>> titleMapOptional =
          titleMapList.stream()
              .filter(titleMap -> titleMap.get(message.getRelatedName()) != null)
              .findAny();
      if (titleMapOptional.isPresent()) {
        Map<String, List<Map<String, Object>>> titleMap = titleMapOptional.get();
        List<Map<String, Object>> activitiyList = titleMap.get(message.getRelatedName());
        List<Map<String, Object>> newActivityList = new ArrayList<>();
        newActivityList.addAll(activitiyList);
        newActivityList.add(activityMap);
        titleMap.put(message.getRelatedName(), newActivityList);
      } else {
        Map<String, List<Map<String, Object>>> newTitleMap = new HashMap<>();
        newTitleMap.put(
            message.getRelatedName() == null ? "" : message.getRelatedName(),
            Arrays.asList(activityMap));
        titleMapList.add(newTitleMap);
      }
      activityDataMap.put(date, titleMapList);
    }

    dataMap.put("$startDate", startDate.format(DATE_FORMATTER));
    dataMap.put("$endDate", endDate.format(DATE_FORMATTER));
    dataMap.put("$activityList", activityDataMap.isEmpty() ? null : Arrays.asList(activityDataMap));
    return dataMap;
  }

  @Override
  public Map<String, Object> getPreviousData(String date, Long projectId) {
    LocalDate formattedDate = LocalDate.parse(date, DATE_FORMATTER);
    return this.getData(formattedDate.minusDays(30), formattedDate.minusDays(1), projectId);
  }

  @Override
  public Map<String, Object> getNextData(String date, Long projectId) {
    LocalDate formattedDate = LocalDate.parse(date, DATE_FORMATTER);
    LocalDate endDate = formattedDate.plusDays(30);
    LocalDate todayDate = LocalDate.now();
    if (todayDate.isBefore(endDate)) {
      endDate = todayDate;
    }
    return this.getData(formattedDate.plusDays(1), endDate, projectId);
  }

  protected String getActionLink(String model) {
    if (ProjectTask.class.getName().equals(model)) {
      return "#/ds/all.open.project.tasks/edit/";
    }
    if (Wiki.class.getName().equals(model)) {
      return "#/ds/all.wiki/edit/";
    }
    return null;
  }

  protected Map<String, Object> getModelWithUtilityClass(MailMessage message) {
    String model = message.getRelatedModel();
    Map<String, Object> dataMap = new HashMap<>();
    if (ProjectTask.class.getName().equals(model)) {
      ProjectTask projectTask = projectTaskRepo.find(message.getRelatedId());
      String modelName =
          Optional.ofNullable(projectTask.getTypeSelect())
              .map(typeSelect -> MetaStore.getSelectionItem("project.task.type.select", typeSelect))
              .map(Option::getTitle)
              .orElse("Project task");
      dataMap.put("modelName", modelName);
      dataMap.put("utilityClass", "label-success");

    } else if (Wiki.class.getName().equals(model)) {
      dataMap.put("modelName", "Wiki");
      dataMap.put("utilityClass", "label-warning");
    }
    return dataMap;
  }

  protected List<String> getRelatedModels() {
    List<String> relatedModelsList = new ArrayList<>();
    relatedModelsList.add(ProjectTask.class.getName());
    relatedModelsList.add(Wiki.class.getName());
    return relatedModelsList;
  }

  protected Project getActivityProject(
      Project project, MailMessage message, Set<Long> projectIdSet) {

    if (ProjectTask.class.getName().equals(message.getRelatedModel())) {
      ProjectTask projectTask = projectTaskRepo.find(message.getRelatedId());
      if (projectTask != null
          && (project == null || projectIdSet.contains(projectTask.getProject().getId()))) {
        return projectTask.getProject();
      }
    } else if (Wiki.class.getName().equals(message.getRelatedModel())) {
      Wiki wiki = wikiRepo.find(message.getRelatedId());
      if (wiki != null) {
        Project wikiProject = wiki.getProject();
        if (project == null
            || (wikiProject != null && projectIdSet.contains(wikiProject.getId()))) {
          return wikiProject;
        }
      }
    }
    return null;
  }

  protected String getActivityDate(LocalDateTime dateTime) {
    LocalDate date = dateTime.toLocalDate();
    return LocalDate.now().equals(date) ? I18n.get("Today") : date.format(DATE_FORMATTER);
  }

  protected List<MailMessage> getMailMessages(LocalDate startDate, LocalDate endDate) {
    return mailMessageRepo
        .all()
        .filter(
            "self.type = :type AND self.relatedModel IN :relatedModels AND self.createdOn <= :endDate AND self.createdOn >= :startDate")
        .bind("type", MailConstants.MESSAGE_TYPE_NOTIFICATION)
        .bind("relatedModels", getRelatedModels())
        .bind("startDate", startDate.atTime(LocalTime.MIN))
        .bind("endDate", endDate.atTime(LocalTime.MAX))
        .order("-id")
        .fetch();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected Map<String, Object> getActivity(MailMessage message) throws Exception {
    final String body = message.getBody();
    final Map<String, Object> bodyData = objectMapper.readValue(body, Map.class);
    if (body == null || body.trim().charAt(0) != '{') {
      return bodyData;
    }
    final Mapper mapper = Mapper.of(Class.forName(message.getRelatedModel()));

    final List<Map<String, String>> values = new ArrayList<>();

    for (Map<String, String> item : (List<Map>) bodyData.get("tracks")) {
      values.add(item);
      final Property property = mapper.getProperty(item.get("name"));
      if (property == null || StringUtils.isBlank(property.getSelection())) {
        continue;
      }
      final Selection.Option d1 =
          MetaStore.getSelectionItem(property.getSelection(), item.get("value"));
      final Selection.Option d2 =
          MetaStore.getSelectionItem(property.getSelection(), item.get("oldValue"));
      item.put("displayValue", d1 == null ? null : d1.getLocalizedTitle());
      item.put("oldDisplayValue", d2 == null ? null : d2.getLocalizedTitle());
    }

    bodyData.put("tracks", values);
    return bodyData;
  }
}
