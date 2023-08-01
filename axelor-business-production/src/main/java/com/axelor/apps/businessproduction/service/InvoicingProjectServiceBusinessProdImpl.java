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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBusinessProject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class InvoicingProjectServiceBusinessProdImpl extends InvoicingProjectService {

  @Override
  public void setLines(InvoicingProject invoicingProject, Project project, int counter) {

    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      super.setLines(invoicingProject, project, counter);
      return;
    }

    AppBusinessProject appBusinessProject = appBusinessProjectService.getAppBusinessProject();
    if (appBusinessProject.getAutomaticInvoicing()) {
      projectTaskBusinessProjectService.taskInvoicing(project, appBusinessProject);
      timesheetLineBusinessService.timsheetLineInvoicing(project);
    }

    if (counter > ProjectServiceImpl.MAX_LEVEL_OF_PROJECT) {
      return;
    }
    counter++;

    this.fillLines(invoicingProject, project);

    if (!invoicingProject.getConsolidatePhaseWhenInvoicing()) {
      return;
    }

    List<Project> projectChildrenList =
        Beans.get(ProjectRepository.class).all().filter("self.parentProject = ?1", project).fetch();

    for (Project projectChild : projectChildrenList) {
      this.setLines(invoicingProject, projectChild, counter);
    }
    return;
  }

  @Override
  public void fillLines(InvoicingProject invoicingProject, Project project) {
    super.fillLines(invoicingProject, project);

    AppProductionService appProductionService = Beans.get(AppProductionService.class);
    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      return;
    }

    if (invoicingProject.getManufOrderSet() == null) {
      invoicingProject.setManufOrderSet(new HashSet<ManufOrder>());
    }

    if (invoicingProject.getDeadlineDate() != null) {
      LocalDateTime deadlineDateToDateTime = invoicingProject.getDeadlineDate().atStartOfDay();
      invoicingProject
          .getManufOrderSet()
          .addAll(
              Beans.get(ManufOrderRepository.class)
                  .all()
                  .filter(
                      "self.productionOrderSet.project = ?1 AND (self.realStartDateT < ?2)",
                      project,
                      deadlineDateToDateTime)
                  .fetch());
    } else {
      invoicingProject
          .getManufOrderSet()
          .addAll(
              Beans.get(ManufOrderRepository.class)
                  .all()
                  .filter("self.productionOrderSet.project = ?1", project)
                  .fetch());
    }
  }

  @Override
  public void clearLines(InvoicingProject invoicingProject) {

    AppProductionService appProductionService = Beans.get(AppProductionService.class);
    super.clearLines(invoicingProject);
    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      return;
    }
    invoicingProject.setManufOrderSet(new HashSet<ManufOrder>());
  }

  @Override
  public int countToInvoice(Project project) {

    AppProductionService appProductionService = Beans.get(AppProductionService.class);
    int toInvoiceCount = super.countToInvoice(project);
    if (!appProductionService.isApp("production")
        || !appProductionService.getAppProduction().getManageBusinessProduction()) {
      return toInvoiceCount;
    }

    int productionOrderCount =
        (int)
            Beans.get(ManufOrderRepository.class)
                .all()
                .filter("self.productionOrderSet.project = ?1", project)
                .count();
    toInvoiceCount += productionOrderCount;

    return toInvoiceCount;
  }

  @Transactional
  @Override
  public InvoicingProject generateInvoicingProject(Project project, int consolidatePhaseSelect) {

    InvoicingProject invoicingProject =
        super.generateInvoicingProject(project, consolidatePhaseSelect);

    if (invoicingProject != null
        && invoicingProject.getId() == null
        && !CollectionUtils.isEmpty(invoicingProject.getManufOrderSet())) {
      return invoicingProjectRepo.save(invoicingProject);
    }

    return invoicingProject;
  }
}
