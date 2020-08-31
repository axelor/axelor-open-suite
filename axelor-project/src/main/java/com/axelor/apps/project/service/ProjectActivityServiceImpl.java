/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectActivity;
import com.axelor.apps.project.db.Topic;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.db.repo.ProjectActivityRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ContextHandlerFactory;
import com.axelor.rpc.Resource;
import com.axelor.team.db.TeamTask;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class ProjectActivityServiceImpl implements ProjectActivityService {

  protected ProjectActivityRepository projectActivityRepo;
  protected ProjectRepository projectRepo;
  protected ProjectStatusRepository projectStatusRepo;

  protected final List<PropertyType> allowedTypes =
      ImmutableList.of(PropertyType.ONE_TO_ONE, PropertyType.MANY_TO_ONE);;
  protected final List<PropertyType> ignoreTypes =
      ImmutableList.of(PropertyType.ONE_TO_MANY, PropertyType.MANY_TO_MANY);
  protected final List<String> ignoreFields = ImmutableList.of("id", "createdOn", "updatedOn");

  @Inject
  public ProjectActivityServiceImpl(
      ProjectActivityRepository projectActivityRepo,
      ProjectRepository projectRepo,
      ProjectStatusRepository projectStatusRepo) {
    this.projectActivityRepo = projectActivityRepo;
    this.projectRepo = projectRepo;
    this.projectStatusRepo = projectStatusRepo;
  }

  @Transactional
  @Override
  public void createTaskProjectActivity(Map<String, Object> dataMap) {
    TeamTask task = getBean(dataMap, TeamTask.class);
    ProjectActivity projectActivity = getDefaultActivity(dataMap, task.getProject(), task);
    if (projectActivity != null) {
      projectActivity.setRecordTitle(task.getName());
      projectActivityRepo.save(projectActivity);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void createTaskProjectActivityByKanban(Map<String, Object> recordsMap) {
    Map<String, Object> taskStatusMap = (HashMap<String, Object>) recordsMap.get("taskStatus");
    String statusName =
        projectStatusRepo.find(Long.valueOf(taskStatusMap.get("id").toString())).getName();
    taskStatusMap.put("name", statusName);
    recordsMap.replace("taskStatus", taskStatusMap);
    createTaskProjectActivity(recordsMap);
  }

  @Transactional
  @Override
  public void createWikiProjectActivity(Map<String, Object> dataMap) {
    Wiki wiki = getBean(dataMap, Wiki.class);
    ProjectActivity projectActivity = getDefaultActivity(dataMap, wiki.getProject(), wiki);
    if (projectActivity != null) {
      projectActivity.setRecordTitle(wiki.getTitle());
      projectActivityRepo.save(projectActivity);
    }
  }

  @Transactional
  @Override
  public void createTopicProjectActivity(Map<String, Object> dataMap) {
    Topic topic = getBean(dataMap, Topic.class);
    ProjectActivity projectActivity = getDefaultActivity(dataMap, topic.getProject(), topic);
    if (projectActivity != null) {
      projectActivity.setRecordTitle(topic.getTitle());
      projectActivityRepo.save(projectActivity);
    }
  }

  protected ProjectActivity getDefaultActivity(
      Map<String, Object> dataMap, Project project, Model model) {
    String activity = getActivity(dataMap, model);
    if (StringUtils.isBlank(activity)) {
      return null;
    }
    ProjectActivity projectActivity = new ProjectActivity();
    projectActivity.setActivity(activity);
    if (model.getId() == null && project != null) {
      project = projectRepo.find(project.getId());
    }
    projectActivity.setProject(project);
    projectActivity.setUser(AuthUtils.getUser());
    projectActivity.setDoneOn(LocalDateTime.now());
    projectActivity.setObjectUpdated(model.getClass().getSimpleName());
    return projectActivity;
  }

  protected String getActivity(Map<String, Object> newDataMap, Model model) {
    if (model.getId() == null) {
      return "Record Created";
    }
    StringBuilder activity = new StringBuilder();
    Mapper mapper = Mapper.of(model.getClass());
    Map<String, Object> oldDataMap = Mapper.toMap(model);
    for (Map.Entry<String, Object> me : newDataMap.entrySet()) {
      String key = me.getKey();
      Property property = mapper.getProperty(key);
      if (oldDataMap.containsKey(key)
          && !ignoreTypes.contains(property.getType())
          && !ignoreFields.contains(property.getName())) {
        Object oldValue = oldDataMap.get(key);
        Object newValue = toProxy(property, me.getValue());
        if (!isEqual(oldValue, newValue)) {
          activity.append(getTitle(property) + " : ");
          activity.append(format(property, oldValue) + ">>");
          activity.append(format(property, newValue) + "\n");
        }
      }
    }
    return activity.toString();
  }

  protected String format(Property property, Object value) {
    if (value == null) {
      return "";
    }
    if (value == Boolean.TRUE) {
      return "True";
    }
    if (value == Boolean.FALSE) {
      return "False";
    }
    if (property.getType() == PropertyType.TEXT) {
      return getSpanContent(value.toString());
    }
    if (allowedTypes.contains(property.getType())) {
      return Mapper.of(property.getTarget()).get(value, property.getTargetName()).toString();
    }
    if (value instanceof BigDecimal) {
      return ((BigDecimal) value).toPlainString();
    }
    return value.toString();
  }

  @SuppressWarnings("unchecked")
  protected Object toProxy(Property property, Object context) {
    if (context == null) {
      return null;
    }
    if (property.getType() == PropertyType.DECIMAL) {
      return new BigDecimal(context.toString());
    }
    if (property.getType() == PropertyType.MANY_TO_ONE) {
      return EntityHelper.getEntity(
          ContextHandlerFactory.newHandler(
                  property.getTarget(),
                  Resource.toMapCompact(
                      Mapper.toBean(property.getTarget(), (HashMap<String, Object>) context)))
              .getProxy());
    }
    return context;
  }

  protected boolean isEqual(Object o1, Object o2) {
    if (o1 == null && o2 == null) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    if (o1 instanceof BigDecimal) {
      return ((BigDecimal) o1).compareTo((BigDecimal) o2) == 0;
    }
    if (o1 instanceof Long) {
      o2 = Long.valueOf(o2.toString());
    }
    if (o1 instanceof LocalDate) {
      o1 = o1.toString();
    }
    return o1.equals(o2);
  }

  protected String getTitle(Property property) {
    String title = property.getTitle();
    if (title == null) {
      title = Inflector.getInstance().humanize(property.getName());
    }
    return I18n.get(title);
  }

  protected <T extends Model> T getBean(Map<String, Object> dataMap, Class<T> klass) {
    Object id = dataMap.get("id");
    return id != null
        ? JPA.find(klass, Long.parseLong(id.toString()))
        : Mapper.toBean(klass, dataMap);
  }

  protected String getSpanContent(String value) {
    Elements spanElements = Jsoup.parse(value).select("span");
    if (ObjectUtils.isEmpty(spanElements)) {
      return value;
    }
    return spanElements.get(0).text();
  }
}
