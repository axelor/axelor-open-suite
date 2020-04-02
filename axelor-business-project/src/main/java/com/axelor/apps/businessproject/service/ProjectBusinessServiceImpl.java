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

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class ProjectBusinessServiceImpl extends ProjectServiceImpl
    implements ProjectBusinessService {

  @Inject protected AppBusinessProjectService appBusinessProjectService;

  @Inject protected ProjectRepository projectRepo;

  @Inject
  public ProjectBusinessServiceImpl(
      ProjectPlanningRepository projectPlanningRepo, ProjectRepository projectRepository) {
    super(projectPlanningRepo, projectRepository);
  }

  @Override
  @Transactional
  public SaleOrder generateQuotation(Project project) throws AxelorException {
    SaleOrder order = Beans.get(SaleOrderCreateService.class).createSaleOrder(project.getCompany());
    order.setClientPartner(project.getClientPartner());
    return Beans.get(SaleOrderRepository.class).save(order);
  }

  /**
   * Generate project form SaleOrder and set bi-directional.
   *
   * @param saleOrder The order of origin.
   * @return The project generated.
   */
  @Override
  public Project generateProject(SaleOrder saleOrder) {
    Project project =
        this.generateProject(
            null,
            saleOrder.getFullName() + "_project",
            saleOrder.getSalemanUser(),
            saleOrder.getCompany(),
            saleOrder.getClientPartner());
    project.setSaleOrder(saleOrder);
    saleOrder.setProject(project);
    return project;
  }

  @Override
  public Project generateProject(
      Project parentProject,
      String fullName,
      User assignedTo,
      Company company,
      Partner clientPartner) {
    Project project =
        super.generateProject(parentProject, fullName, assignedTo, company, clientPartner);
    project.addMembersUserSetItem(assignedTo);
    project.setImputable(true);
    project.setProjInvTypeSelect(ProjectRepository.INVOICING_TYPE_NONE);
    if (parentProject != null) {
      project.setProjInvTypeSelect(parentProject.getProjInvTypeSelect());
    }
    Product product =
        appBusinessProjectService.getAppBusinessProject().getProductInvoicingProject();
    if (product != null) {
      project.setProduct(product);
      project.setQty(BigDecimal.ONE);
      project.setPrice(product.getPurchasePrice());
      project.setUnit(product.getUnit());
      project.setExTaxTotal(product.getPurchasePrice());
    }
    return project;
  }

  @Override
  public Project generatePhaseProject(SaleOrderLine saleOrderLine, Project parent) {
    Project project =
        generateProject(
            parent,
            saleOrderLine.getFullName(),
            saleOrderLine.getSaleOrder().getSalemanUser(),
            parent.getCompany(),
            parent.getClientPartner());
    project.setProduct(saleOrderLine.getProduct());
    project.setQty(saleOrderLine.getQty());
    project.setPrice(saleOrderLine.getPrice());
    project.setUnit(saleOrderLine.getUnit());
    project.setExTaxTotal(saleOrderLine.getCompanyExTaxTotal());
    project.setProjectTypeSelect(ProjectRepository.TYPE_PHASE);
    saleOrderLine.setProject(project);
    return project;
  }

  /**
   * Manages invoice lines for project dashlets
   *
   * @param invoiceLine InvoiceLine to add or remove
   * @param project Project to add or remove the invoice line
   */
  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void manageInvoiceLine(InvoiceLine invoiceLine, Project project) {
    List<InvoiceLine> invoiceLines = project.getInvoiceLineList();
    if (invoiceLines.contains(invoiceLine)) {
      project.removeInvoiceLineListItem(invoiceLine);
    } else {
      project.addInvoiceLineListItem(invoiceLine);
    }

    projectRepo.save(project);
  }
}
