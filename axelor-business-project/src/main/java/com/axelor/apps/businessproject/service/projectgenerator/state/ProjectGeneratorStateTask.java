package com.axelor.apps.businessproject.service.projectgenerator.state;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.TeamTaskBusinessService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class ProjectGeneratorStateTask extends ProjectGeneratorStateAlone
    implements ProjectGeneratorState {

  protected TeamTaskBusinessService teamTaskBusinessService;
  protected TeamTaskRepository teamTaskRepository;

  @Inject
  public ProjectGeneratorStateTask(
      ProjectBusinessService projectBusinessService,
      ProjectRepository projectRepository,
      TeamTaskBusinessService teamTaskBusinessService,
      TeamTaskRepository teamTaskRepository) {
    super(projectBusinessService, projectRepository);
    this.teamTaskBusinessService = teamTaskBusinessService;
    this.teamTaskRepository = teamTaskRepository;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Project generate(SaleOrder saleOrder) {
    Project project = super.generate(saleOrder);
    project.setIsBusinessProject(true);
    return project;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public ActionViewBuilder fill(Project project, SaleOrder saleOrder) {
    List<TeamTask> tasks = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Product product = saleOrderLine.getProduct();
      if (ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())
          && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE) {
        TeamTask task =
            teamTaskBusinessService.create(saleOrderLine, project, project.getAssignedTo());
        teamTaskRepository.save(task);
        tasks.add(task);
      }
    }
    return ActionView.define(String.format("Task%s generated", (tasks.size() > 1 ? "s" : "")))
        .model(TeamTask.class.getName())
        .add("grid", "team-task-grid")
        .add("form", "team-task-form")
        .domain(String.format("self.id in (%s)", StringTool.getIdListString(tasks)));
  }
}
