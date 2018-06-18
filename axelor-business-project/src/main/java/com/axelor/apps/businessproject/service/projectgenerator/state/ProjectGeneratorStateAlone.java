package com.axelor.apps.businessproject.service.projectgenerator.state;

import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;

public class ProjectGeneratorStateAlone implements ProjectGeneratorState {

  protected ProjectBusinessService projectBusinessService;
  protected ProjectRepository projectRepository;

  @Inject
  public ProjectGeneratorStateAlone(
      ProjectBusinessService projectBusinessService, ProjectRepository projectRepository) {
    this.projectBusinessService = projectBusinessService;
    this.projectRepository = projectRepository;
  }

  @Override
  public Project generate(SaleOrder saleOrder) {
    Project project = projectBusinessService.generateProject(saleOrder);
    project.setIsProject(false);
    project.setIsBusinessProject(true);
    return projectRepository.save(project);
  }
}
