/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.service.execution;

import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfProcessConfigRepository;
import com.axelor.apps.bpm.service.WkfCommonService;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.camunda.bpm.engine.delegate.DelegateExecution;

public class WkfUserActionServiceImpl implements WkfUserActionService {

  public static final String DESCRIPTION = /*$$(*/
      "BPM state <b>%s</b> is activated on<br/> <a href=\"%s\">%s</a><br/>" /*)*/;

  @Inject private WkfCommonService wkfService;

  @Inject private UserRepository userRepository;

  @Inject private WkfProcessConfigRepository wkfProcessConfigRepository;

  @Inject private TeamTaskRepository teamTaskRepository;

  protected static final Pattern FIELD_PATTERN = Pattern.compile("(\\$\\{[^\\}]+\\})");

  @Override
  @Transactional
  public void createUserAction(WkfTaskConfig wkfTaskConfig, DelegateExecution execution) {

    String title = wkfTaskConfig.getTaskEmailTitle();

    if (title == null) {
      return;
    }

    try {
      FullContext wkfContext = getModelCtx(wkfTaskConfig, execution);
      if (wkfContext != null) {
        title = processTitle(title, wkfContext);
      }

      TeamTask teamTask = new TeamTask(title);
      teamTask.setStatus("new");
      if (wkfTaskConfig.getDeadlineFieldPath() != null) {
        teamTask.setTaskDate(getDeadLineDate(wkfTaskConfig.getDeadlineFieldPath(), wkfContext));
      }
      if (teamTask.getTaskDate() == null) {
        teamTask.setTaskDate(LocalDate.now());
      }

      String userPath = getUserPath(wkfTaskConfig, execution.getProcessDefinitionId());
      if (userPath != null) {
        teamTask.setAssignedTo(getUser(userPath, wkfContext));
      }

      String url =
          Beans.get(WkfEmailService.class).createUrl(wkfContext, wkfTaskConfig.getDefaultForm());
      teamTask.setDescription(
          String.format(DESCRIPTION, execution.getCurrentActivityName(), url, url));

      teamTaskRepository.save(teamTask);

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String processTitle(String title, FullContext wkfContext) {

    Matcher machMatcher = FIELD_PATTERN.matcher(title);

    while (machMatcher.find()) {
      String field = machMatcher.group();
      Object value = wkfService.evalExpression(wkfContext, field);
      if (value == null) {
        value = "";
      }
      title = title.replace(field, value.toString());
    }

    return title;
  }

  @Override
  public FullContext getModelCtx(WkfTaskConfig wkfTaskConfig, DelegateExecution execution)
      throws ClassNotFoundException {

    String modelName = null;
    Class<? extends Model> modelClass = null;
    if (wkfTaskConfig.getModelName() != null) {
      modelName = wkfTaskConfig.getModelName();
      modelClass =
          (Class<? extends Model>)
              Class.forName(
                  Beans.get(MetaModelRepository.class).findByName(modelName).getFullName());
    } else if (wkfTaskConfig.getJsonModelName() != null) {
      modelName = wkfTaskConfig.getJsonModelName();
      modelClass = MetaJsonRecord.class;
    } else {
      return null;
    }

    String varName = wkfService.getVarName(modelName);
    Object id = execution.getVariable(varName + "Id");
    FullContext wkfContext = null;
    if (id != null && id instanceof Long) {
      Model record = JPA.find(modelClass, Long.parseLong(id.toString()));
      wkfContext = new FullContext(record);
    }

    return wkfContext;
  }

  private LocalDate getDeadLineDate(String deadLineFieldPath, FullContext wkfContext) {

    LocalDate date = null;
    if (deadLineFieldPath.equals("today")) {
      date = LocalDate.now();
    } else if (wkfContext != null) {
      date = (LocalDate) wkfService.evalExpression(wkfContext, deadLineFieldPath);
    }

    return date;
  }

  @Override
  public User getUser(String userPath, FullContext wkfContext) {

    User user = null;
    if (userPath.equals("currentUser")) {
      user = AuthUtils.getUser();
    } else if (wkfContext != null) {
      FullContext userCtx = (FullContext) wkfService.evalExpression(wkfContext, userPath);
      if (userCtx != null) {
        user = (User) userCtx.getTarget();
        if (user != null) {
          user = Beans.get(UserRepository.class).find(user.getId());
        }
      }
    } else {
      user = userRepository.findByCode(userPath);
    }

    return user;
  }

  private String getUserPath(WkfTaskConfig wkfTaskConfig, String processDefinitionId) {

    String userPath = wkfTaskConfig.getUserPath();
    if (userPath == null) {
      WkfProcessConfig processConfig =
          wkfProcessConfigRepository
              .all()
              .filter(
                  "self.wkfProcess.processId = ?1 "
                      + "AND self.wkfProcess.wkfModel.id = ?2 "
                      + "AND (?3 is not null AND self.metaModel.fullName = ?3) "
                      + "OR (?4 is not null AND self.metaJsonModel.name = ?4)",
                  processDefinitionId,
                  wkfTaskConfig.getWkfModel().getId(),
                  wkfTaskConfig.getModelName(),
                  wkfTaskConfig.getJsonModelName())
              .fetchOne();
      if (processConfig != null) {
        userPath = processConfig.getUserDefaultPath();
      }
    }

    return userPath;
  }
}
