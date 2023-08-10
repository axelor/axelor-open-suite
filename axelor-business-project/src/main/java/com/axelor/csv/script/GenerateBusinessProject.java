/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectGeneratorType;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GenerateBusinessProject {
  protected static final List<String> taskByLineSaleOrderIds = List.of("13");
  protected static final List<String> taskTemplateSaleOrderIds = List.of("14");

  protected final ProjectRepository projectRepository;
  protected final ProjectBusinessService projectBusinessService;

  @Inject
  public GenerateBusinessProject(
      ProjectRepository projectRepository, ProjectBusinessService projectBusinessService) {
    this.projectRepository = projectRepository;
    this.projectBusinessService = projectBusinessService;
  }

  public Object generateBusinessProject(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof SaleOrder;

    SaleOrder saleOrder = (SaleOrder) bean;

    ProjectGeneratorType type = selectFactoryType(saleOrder);

    ProjectGeneratorFactory projectGeneratorFactory =
        ProjectGeneratorFactory.getFactory(selectFactoryType(saleOrder));

    Project project = projectGeneratorFactory.generate(saleOrder, saleOrder.getUpdatedOn());

    project.setIsShowTimeSpent(true);
    project.setSpentTimeCostComputationMethod(ProjectRepository.COMPUTATION_METHOD_EMPLOYEE);
    project.setImportId("demo_project_" + saleOrder.getImportId());
    fillProjectTasks(saleOrder, project, type);

    projectBusinessService.computeProjectTotals(project);

    return saleOrder;
  }

  protected ProjectGeneratorType selectFactoryType(SaleOrder saleOrder) throws AxelorException {
    if (taskByLineSaleOrderIds.contains(saleOrder.getImportId()))
      return ProjectGeneratorType.TASK_BY_LINE;
    if (taskTemplateSaleOrderIds.contains(saleOrder.getImportId()))
      return ProjectGeneratorType.TASK_TEMPLATE;
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BusinessProjectExceptionMessage.FACTORY_NO_FOUND));
  }

  protected void fillProjectTasks(SaleOrder saleOrder, Project project, ProjectGeneratorType type)
      throws AxelorException {
    switch (type) {
      case TASK_BY_LINE:
        fillProjectTasksNameTaskByLine(saleOrder, project);
        break;
      case TASK_TEMPLATE:
        fillProjectTasksNameTaskTemplate(saleOrder, project);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BusinessProjectExceptionMessage.FACTORY_NO_FOUND));
    }
  }

  protected void fillProjectTasksNameTaskByLine(SaleOrder saleOrder, Project project) {
    int i = 0;
    for (ProjectTask projectTask : project.getProjectTaskList()) {
      StringBuilder importId = new StringBuilder();
      importId.append("demo_project_task_");
      importId.append(saleOrder.getImportId());
      importId.append("_");
      importId.append(i);
      projectTask.setImportId(importId.toString());

      String oldTaskName = projectTask.getName();
      projectTask.setName(oldTaskName.substring(0, oldTaskName.length() - 5));

      i++;
    }
  }

  protected void fillProjectTasksNameTaskTemplate(SaleOrder saleOrder, Project project) {
    int i = 0;
    int j = 0;
    for (ProjectTask projectTask : project.getProjectTaskList()) {
      StringBuilder importId = new StringBuilder();
      if (Objects.isNull(projectTask.getParentTask())) {
        importId.append("demo_project_task_");
        importId.append(saleOrder.getImportId());
        importId.append("_");
        importId.append(i);
        i++;
        j = 0;
      } else {
        importId.append(projectTask.getParentTask().getImportId());
        importId.append("_sub_");
        importId.append(j);
        j++;
      }
      projectTask.setImportId(importId.toString());
    }
  }
}
