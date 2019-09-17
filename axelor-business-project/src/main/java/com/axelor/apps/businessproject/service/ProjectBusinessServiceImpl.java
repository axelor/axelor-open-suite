/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectBusinessServiceImpl extends ProjectServiceImpl
    implements ProjectBusinessService {

  @Inject protected AppBusinessProjectService appBusinessProjectService;

  @Inject protected ProjectRepository projectRepo;

  @Inject protected PartnerService partnerService;

  @Inject protected AddressService addressService;

  @Inject
  public ProjectBusinessServiceImpl(ProjectRepository projectRepository) {
    super(projectRepository);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateQuotation(Project project) throws AxelorException {
    SaleOrder order = Beans.get(SaleOrderCreateService.class).createSaleOrder(project.getCompany());

    Partner customerPartner = project.getCustomerPartner();
    Partner contactPartner = project.getContactPartner();
    if (contactPartner == null && customerPartner.getContactPartnerSet().size() == 1) {
      contactPartner = customerPartner.getContactPartnerSet().iterator().next();
    }

    Company company = project.getCompany();

    order.setProject(projectRepo.find(project.getId()));
    order.setCustomerPartner(customerPartner);
    order.setContactPartner(contactPartner);
    order.setCompany(company);

    order.setMainInvoicingAddress(partnerService.getInvoicingAddress(customerPartner));
    order.setMainInvoicingAddressStr(
        addressService.computeAddressStr(order.getMainInvoicingAddress()));
    order.setDeliveryAddress(partnerService.getDeliveryAddress(customerPartner));
    order.setDeliveryAddressStr(addressService.computeAddressStr(order.getDeliveryAddress()));
    order.setIsNeedingConformityCertificate(customerPartner.getIsNeedingConformityCertificate());
    order.setCompanyBankDetails(
        Beans.get(AccountingSituationService.class)
            .getCompanySalesBankDetails(company, customerPartner));

    if (project.getCurrency() != null) {
      order.setCurrency(project.getCurrency());
    } else if (customerPartner.getCurrency() != null) {
      order.setCurrency(customerPartner.getCurrency());
    } else {
      order.setCurrency(company.getCurrency());
    }

    if (project.getPriceList() != null) {
      order.setPriceList(project.getPriceList());
    } else {
      order.setPriceList(
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(customerPartner, PriceListRepository.TYPE_SALE));
    }

    if (order.getPriceList() != null) {
      order.setHideDiscount(order.getPriceList().getHideDiscount());
    }

    if (customerPartner.getPaymentCondition() != null) {
      order.setPaymentCondition(customerPartner.getPaymentCondition());
    } else {
      if (company != null && company.getAccountConfig() != null) {
        order.setPaymentCondition(company.getAccountConfig().getDefPaymentCondition());
      }
    }

    if (customerPartner.getInPaymentMode() != null) {
      order.setPaymentMode(customerPartner.getInPaymentMode());
    } else {
      if (company != null && company.getAccountConfig() != null) {
        order.setPaymentMode(company.getAccountConfig().getInPaymentMode());
      }
    }

    if (order.getDuration() != null && order.getCreationDate() != null) {
      order.setEndOfValidityDate(
          Beans.get(DurationService.class)
              .computeDuration(order.getDuration(), order.getCreationDate()));
    }

    AppSupplychain appSupplychain = Beans.get(AppSupplychainService.class).getAppSupplychain();
    if (appSupplychain != null) {
      order.setShipmentMode(customerPartner.getShipmentMode());
      order.setFreightCarrierMode(customerPartner.getFreightCarrierMode());
      if (customerPartner.getFreightCarrierMode() != null) {
        order.setCarrierPartner(customerPartner.getFreightCarrierMode().getCarrierPartner());
      }
      Boolean interco =
          appSupplychain.getIntercoFromSale()
              && !order.getCreatedByInterco()
              && customerPartner != null
              && Beans.get(CompanyRepository.class)
                      .all()
                      .filter("self.partner = ?", customerPartner)
                      .fetchOne()
                  != null;
      order.setInterco(interco);
    }
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
    Project project = projectRepo.findByName(saleOrder.getFullName() + "_project");
    project =
        project == null
            ? this.generateProject(
                null,
                saleOrder.getFullName() + "_project",
                saleOrder.getSalespersonUser(),
                saleOrder.getCompany(),
                saleOrder.getCustomerPartner())
            : project;
    saleOrder.setProject(project);
    return project;
  }

  @Override
  public Project generateProject(
      Project parentProject,
      String fullName,
      User assignedTo,
      Company company,
      Partner customerPartner) {
    Project project =
        super.generateProject(parentProject, fullName, assignedTo, company, customerPartner);
    project.addMembersUserSetItem(assignedTo);
    project.setImputable(true);
    if (parentProject != null && parentProject.getTeamTaskInvoicing()) {
      project.setTeamTaskInvoicing(true);
      project.setInvoicingType(parentProject.getInvoicingType());
    }
    return project;
  }

  @Override
  public Project generatePhaseProject(SaleOrderLine saleOrderLine, Project parent) {
    Project project =
        generateProject(
            parent,
            saleOrderLine.getFullName(),
            saleOrderLine.getSaleOrder().getSalespersonUser(),
            parent.getCompany(),
            parent.getCustomerPartner());
    project.setProjectTypeSelect(ProjectRepository.TYPE_PHASE);
    saleOrderLine.setProject(project);
    return project;
  }

  @Override
  @Transactional
  public Project createProjectFromTemplate(
      ProjectTemplate projectTemplate, String projectCode, Partner customerPartner)
      throws AxelorException {

    Project project =
        super.createProjectFromTemplate(projectTemplate, projectCode, customerPartner);

    if (projectTemplate.getIsBusinessProject()) {
      project.setCurrency(customerPartner.getCurrency());
      if (customerPartner.getPartnerAddressList() != null
          && !customerPartner.getPartnerAddressList().isEmpty()) {
        project.setCustomerAddress(
            customerPartner.getPartnerAddressList().iterator().next().getAddress());
      }
      if (customerPartner.getSalePartnerPriceList() != null
          && customerPartner.getSalePartnerPriceList().getPriceListSet() != null
          && !customerPartner.getSalePartnerPriceList().getPriceListSet().isEmpty()) {
        project.setPriceList(
            customerPartner.getSalePartnerPriceList().getPriceListSet().iterator().next());
      }
      project.setTeamTaskInvoicing(projectTemplate.getTeamTaskInvoicing());
      project.setIsInvoicingExpenses(projectTemplate.getIsInvoicingExpenses());
      project.setIsInvoicingPurchases(projectTemplate.getIsInvoicingPurchases());
      project.setInvoicingType(projectTemplate.getInvoicingTypeSelect());
      project.setInvoicingComment(projectTemplate.getInvoicingComment());
      project.setIsBusinessProject(projectTemplate.getIsBusinessProject());
    }

    return project;
  }

  @Override
  public TeamTask createTask(TaskTemplate taskTemplate, Project project) {
    TeamTask task = super.createTask(taskTemplate, project);
    task.setTeamTaskInvoicing(taskTemplate.getTeamTaskInvoicing());
    return task;
  }
}
