package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticTemplateService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BusinessProjectServiceImpl implements BusinessProjectService {

  protected PartnerPriceListService partnerPriceListService;
  protected ProjectAnalyticTemplateService projectAnalyticTemplateService;
  protected ProjectRepository projectRepository;

  @Inject
  public BusinessProjectServiceImpl(
      PartnerPriceListService partnerPriceListService,
      ProjectAnalyticTemplateService projectAnalyticTemplateService,
      ProjectRepository projectRepository) {
    this.partnerPriceListService = partnerPriceListService;
    this.projectAnalyticTemplateService = projectAnalyticTemplateService;
    this.projectRepository = projectRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setAsBusinessProject(Project project, Company company, Partner clientPartner)
      throws AxelorException {
    project.setIsBusinessProject(true);
    project.setCompany(company);
    project.setClientPartner(clientPartner);
    project = computePartnerData(project, clientPartner);
    projectRepository.save(project);
  }

  @Override
  public Project computePartnerData(Project project, Partner partner) throws AxelorException {
    if (partner == null || project == null) {
      return project;
    }

    project.setAnalyticDistributionTemplate(
        projectAnalyticTemplateService.getDefaultAnalyticDistributionTemplate(project));
    project.setCurrency(partner.getCurrency());
    project.setPriceList(
        partnerPriceListService.getDefaultPriceList(partner, PriceListRepository.TYPE_SALE));
    return project;
  }
}
