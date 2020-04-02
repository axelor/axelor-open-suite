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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.project.db.GenProjTypePerOrderLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.auth.db.AuditableModel;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderProjectService {

  private ProjectBusinessService projectBusinessService;
  private SaleOrderLineRepository saleOrderLineRepository;
  private TeamTaskRepository teamTaskRepository;
  private TeamTaskBusinessService teamTaskBusinessService;

  @Inject
  public SaleOrderProjectService(
      ProjectBusinessService projectBusinessService,
      SaleOrderLineRepository saleOrderLineRepository,
      TeamTaskRepository teamTaskRepository,
      TeamTaskBusinessService teamTaskService) {
    this.projectBusinessService = projectBusinessService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.teamTaskRepository = teamTaskRepository;
    this.teamTaskBusinessService = teamTaskService;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Project generateProject(SaleOrder saleOrder, String type) throws AxelorException {
    if (type == null) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.SALE_ORDER_NO_TYPE_GEN_PROJECT));
    }
    Project project = projectBusinessService.generateProject(saleOrder);
    project.setGenProjTypePerOrderLine(GenProjTypePerOrderLine.valueOf(type));
    project.setPriceList(saleOrder.getPriceList());
    switch (project.getGenProjTypePerOrderLine()) {
      case BUSINESS_PROJECT:
        project.setIsProject(false);
        project.setIsBusinessProject(true);
        break;
      case PHASE_BY_LINE:
        // willful absence of break; to execute the code of the case below and avoid duplication
      case TASK_BY_LINE:
        project.setIsProject(true);
        project.setIsBusinessProject(true);
        break;
      default:
        project.setIsProject(true);
        project.setIsBusinessProject(false);
    }
    if (project.getGenProjTypePerOrderLine() == GenProjTypePerOrderLine.PHASE_BY_LINE
        || project.getGenProjTypePerOrderLine() == GenProjTypePerOrderLine.TASK_BY_LINE) {
      generateProjectTypePerOrderLine(saleOrder);
    }
    return Beans.get(ProjectRepository.class).save(project);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<? extends AuditableModel> generateProjectTypePerOrderLine(SaleOrder saleOrder)
      throws AxelorException {
    if (saleOrder.getProject() == null) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.SALE_ORDER_NO_PROJECT));
    }
    List<? extends AuditableModel> models;
    switch (saleOrder.getProject().getGenProjTypePerOrderLine()) {
      case BUSINESS_PROJECT:
        throw new AxelorException(
            saleOrder.getProject(),
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.SALE_ORDER_BUSINESS_PROJECT));
      case PHASE_BY_LINE:
        models = generateProjectPhases(saleOrder);
        break;
      case TASK_BY_LINE:
        models = generateProjectTasks(saleOrder);
        break;
      default:
        throw new AxelorException(
            saleOrder.getProject(),
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.SALE_ORDER_NO_TYPE_GEN_PROJECT));
    }
    if (models.isEmpty()) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.SALE_ORDER_NO_LINES));
    }
    return models;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  private List<TeamTask> generateProjectTasks(SaleOrder saleOrder) {
    List<TeamTask> tasks = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Product product = saleOrderLine.getProduct();
      if (ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())
          && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE) {
        TeamTask task =
            teamTaskBusinessService.create(
                saleOrderLine, saleOrder.getProject(), saleOrder.getProject().getAssignedTo());
        teamTaskRepository.save(task);
        tasks.add(task);
      }
    }
    return tasks;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  private List<Project> generateProjectPhases(SaleOrder saleOrder) {
    List<Project> projects = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Product product = saleOrderLine.getProduct();
      if (ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())
          && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE) {
        Project project =
            projectBusinessService.generatePhaseProject(saleOrderLine, saleOrder.getProject());
        saleOrderLineRepository.save(saleOrderLine);
        projects.add(project);
      }
    }
    return projects;
  }
}
