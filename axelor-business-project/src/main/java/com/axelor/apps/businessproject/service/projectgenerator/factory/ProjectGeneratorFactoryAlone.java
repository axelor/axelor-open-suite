package com.axelor.apps.businessproject.service.projectgenerator.factory;

import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class ProjectGeneratorFactoryAlone implements ProjectGeneratorFactory {

  private ProjectBusinessService projectBusinessService;
  private ProjectRepository projectRepository;

  @Inject
  public ProjectGeneratorFactoryAlone(
      ProjectBusinessService projectBusinessService, ProjectRepository projectRepository) {
    this.projectBusinessService = projectBusinessService;
    this.projectRepository = projectRepository;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Project create(SaleOrder saleOrder) {
    Project project = projectBusinessService.generateProject(saleOrder);
    project.setIsProject(false);
    project.setIsBusinessProject(true);
    return projectRepository.save(project);
  }

  @Override
  public ActionViewBuilder fill(Project project, SaleOrder saleOrder, LocalDateTime localDateTime)
      throws AxelorException {
    throw new AxelorException(
        TraceBackRepository.TYPE_FUNCTIONNAL,
        I18n.get("You can't fill a project with the strategy Project Alone."));
  }
}
