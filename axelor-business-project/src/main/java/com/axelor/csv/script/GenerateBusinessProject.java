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
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectGeneratorType;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.Map;

public class GenerateBusinessProject {

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

    ProjectGeneratorFactory projectGeneratorFactory =
        ProjectGeneratorFactory.getFactory(ProjectGeneratorType.TASK_BY_LINE);

    Project project = projectGeneratorFactory.generate(saleOrder, saleOrder.getUpdatedOn());

    project.setIsShowTimeSpent(true);
    project.setSpentTimeCostComputationMethod(ProjectRepository.COMPUTATION_METHOD_EMPLOYEE);
    project.setImportId("demo_project_" + saleOrder.getImportId());

    for (int i = 0; i < project.getProjectTaskList().size(); i++) {
      StringBuilder importId = new StringBuilder();
      importId.append("demo_project_task_");
      importId.append(saleOrder.getImportId());
      importId.append("_");
      importId.append(i);

      String oldTaskName = project.getProjectTaskList().get(i).getName();
      project.getProjectTaskList().get(i).setImportId(importId.toString());
      project.getProjectTaskList().get(i).setName(oldTaskName.substring(0, oldTaskName.length() - 5));
    }

    projectBusinessService.computeProjectTotals(project);

    return saleOrder;
  }
}
