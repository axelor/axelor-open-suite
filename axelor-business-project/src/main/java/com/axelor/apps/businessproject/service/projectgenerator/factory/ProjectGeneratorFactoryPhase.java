package com.axelor.apps.businessproject.service.projectgenerator.factory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectGeneratorFactoryPhase implements ProjectGeneratorFactory {

  private ProjectBusinessService projectBusinessService;
  private ProjectRepository projectRepository;
  private SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public ProjectGeneratorFactoryPhase(
      ProjectBusinessService projectBusinessService,
      ProjectRepository projectRepository,
      SaleOrderLineRepository saleOrderLineRepository) {
    this.projectBusinessService = projectBusinessService;
    this.projectRepository = projectRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
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
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public ActionViewBuilder fill(Project project, SaleOrder saleOrder, LocalDateTime startDate) {
    List<Project> projects = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Product product = saleOrderLine.getProduct();
      if (ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())
          && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE) {
        Project phase = projectBusinessService.generatePhaseProject(saleOrderLine, project);
        phase.setFromDate(startDate);
        saleOrderLineRepository.save(saleOrderLine);
        projects.add(phase);
      }
    }
    return ActionView.define(String.format("Project%s generated", (projects.size() > 1 ? "s" : "")))
        .model(Project.class.getName())
        .add("grid", "project-grid")
        .add("form", "project-form")
        .domain(String.format("self.id in (%s)", StringTool.getIdListString(projects)));
  }
}
