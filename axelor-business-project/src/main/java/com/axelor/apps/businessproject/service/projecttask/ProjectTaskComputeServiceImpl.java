package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.businessproject.service.ProjectFrameworkContractService;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ProjectTaskComputeServiceImpl implements ProjectTaskComputeService {

  protected UnitConversionForProjectService unitConversionForProjectService;
  protected ProjectFrameworkContractService projectFrameworkContractService;
  public static final int COMPUTE_SCALE = 5;

  @Inject
  public ProjectTaskComputeServiceImpl(
      UnitConversionForProjectService unitConversionForProjectService,
      ProjectFrameworkContractService projectFrameworkContractService) {
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.projectFrameworkContractService = projectFrameworkContractService;
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
            COMPUTE_SCALE,
            projectTask.getProject()));
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
            COMPUTE_SCALE,
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
              COMPUTE_SCALE,
              projectTask.getProject()));
    }
    if (productDatas.get("unitCost") != null) {
      projectTask.setUnitCost(
          unitConversionForProjectService.convert(
              projectTask.getInvoicingUnit(),
              productUnit,
              (BigDecimal) productDatas.get("unitCost"),
              COMPUTE_SCALE,
              projectTask.getProject()));
    }
  }
}
