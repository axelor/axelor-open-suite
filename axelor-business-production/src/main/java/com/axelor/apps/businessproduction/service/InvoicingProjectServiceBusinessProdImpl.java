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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.inject.Beans;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

public class InvoicingProjectServiceBusinessProdImpl extends InvoicingProjectService {

  @Override
  public void setLines(InvoicingProject invoicingProject, Project project, int counter) {

    if (counter > ProjectServiceImpl.MAX_LEVEL_OF_PROJECT) {
      return;
    }
    counter++;

    this.fillLines(invoicingProject, project);
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
                      "self.productionOrder.project = ?1 AND (self.realStartDateT < ?2)",
                      project,
                      deadlineDateToDateTime)
                  .fetch());
    } else {
      invoicingProject
          .getManufOrderSet()
          .addAll(
              Beans.get(ManufOrderRepository.class)
                  .all()
                  .filter("self.productionOrder.project = ?1", project)
                  .fetch());
    }
  }

  @Override
  public void clearLines(InvoicingProject invoicingProject) {

    super.clearLines(invoicingProject);
    invoicingProject.setManufOrderSet(new HashSet<ManufOrder>());
  }

  @Override
  public int countToInvoice(Project project) {

    int toInvoiceCount = super.countToInvoice(project);

    int productionOrderCount =
        (int)
            Beans.get(ManufOrderRepository.class)
                .all()
                .filter("self.productionOrder.project = ?1", project)
                .count();
    toInvoiceCount += productionOrderCount;

    return toInvoiceCount;
  }
}
