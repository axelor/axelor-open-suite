package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.businessproject.service.ProjectFrameworkContractService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.studio.db.AppBusinessProject;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ProjectTaskComputeServiceImpl implements ProjectTaskComputeService {

  protected UnitConversionForProjectService unitConversionForProjectService;
  protected ProjectFrameworkContractService projectFrameworkContractService;
  protected AppBusinessProjectService appBusinessProjectService;
  public static final int BIG_DECIMAL_SCALE = 2;

  @Inject
  public ProjectTaskComputeServiceImpl(
      UnitConversionForProjectService unitConversionForProjectService,
      ProjectFrameworkContractService projectFrameworkContractService,
      AppBusinessProjectService appBusinessProjectService) {
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.projectFrameworkContractService = projectFrameworkContractService;
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  public void computeBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException {
    if (projectTask == null
        || oldTimeUnit == null
        || projectTask.getTimeUnit() == null
        || projectTask.getProject() == null) {
      return;
    }

    projectTask.setBudgetedTime(
        unitConversionForProjectService.convert(
            oldTimeUnit,
            projectTask.getTimeUnit(),
            projectTask.getBudgetedTime(),
            BIG_DECIMAL_SCALE,
            projectTask.getProject()));
  }

  @Override
  public BigDecimal computeSoldTime(ProjectTask projectTask) {
    if (projectTask == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal budgetedTime = projectTask.getBudgetedTime();
    if (budgetedTime.signum() == 0) {
      return budgetedTime;
    }

    ProjectTaskCategory projectTaskCategory = projectTask.getProjectTaskCategory();
    if (projectTaskCategory != null
        && (projectTaskCategory.getSaleCoefficient() >= 0
            || projectTaskCategory.getRiskCoefficient() >= 0)) {
      return budgetedTime.multiply(
          BigDecimal.valueOf(
              Math.max(0, projectTaskCategory.getSaleCoefficient())
                  + Math.max(0, projectTaskCategory.getRiskCoefficient())));
    }
    Project project = projectTask.getProject();
    if (project != null
        && (project.getSaleCoefficient() >= 0 || project.getRiskCoefficient() >= 0)) {
      return budgetedTime.multiply(
          BigDecimal.valueOf(
              Math.max(0, project.getSaleCoefficient())
                  + Math.max(0, project.getRiskCoefficient())));
    }

    AppBusinessProject appBusinessProject = appBusinessProjectService.getAppBusinessProject();
    if (appBusinessProject != null
        && (appBusinessProject.getSaleCoefficient() >= 0
            || appBusinessProject.getRiskCoefficient() >= 0)) {
      return budgetedTime.multiply(
          BigDecimal.valueOf(
              Math.max(0, appBusinessProject.getSaleCoefficient())
                  + Math.max(0, appBusinessProject.getRiskCoefficient())));
    }

    return budgetedTime;
  }

  @Override
  public void computeQuantity(ProjectTask projectTask) throws AxelorException {
    if (projectTask == null
        || projectTask.getInvoicingUnit() == null
        || projectTask.getTimeUnit() == null) {
      return;
    }

    projectTask.setQuantity(
        unitConversionForProjectService.convert(
            projectTask.getTimeUnit(),
            projectTask.getInvoicingUnit(),
            projectTask.getUpdatedTime(),
            BIG_DECIMAL_SCALE,
            projectTask.getProject()));
  }

  @Override
  public void computeFinancialDatas(ProjectTask projectTask) throws AxelorException {
    if (projectTask == null || projectTask.getProduct() == null) {
      return;
    }
    Map<String, Object> productDatas =
        projectFrameworkContractService.getProductDataFromContract(projectTask);
    Product product = projectTask.getProduct();
    Unit productUnit = Optional.of(product).map(Product::getSalesUnit).orElse(product.getUnit());

    if (productDatas.get("unitPrice") != null) {
      projectTask.setUnitPrice(
          unitConversionForProjectService.convert(
              projectTask.getInvoicingUnit(),
              productUnit,
              (BigDecimal) productDatas.get("unitPrice"),
              BIG_DECIMAL_SCALE,
              projectTask.getProject()));
    }
    if (productDatas.get("unitCost") != null) {
      projectTask.setUnitCost(
          unitConversionForProjectService.convert(
              projectTask.getInvoicingUnit(),
              productUnit,
              (BigDecimal) productDatas.get("unitCost"),
              BIG_DECIMAL_SCALE,
              projectTask.getProject()));
    }
  }
}
