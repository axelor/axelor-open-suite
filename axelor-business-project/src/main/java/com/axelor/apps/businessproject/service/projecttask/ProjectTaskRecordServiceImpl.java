package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.businessproject.service.ProjectFrameworkContractService;
import com.axelor.apps.businessproject.service.UnitProjectToolService;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ProjectTaskRecordServiceImpl implements ProjectTaskRecordService {

  protected UnitProjectToolService unitProjectToolService;
  protected ProjectFrameworkContractService projectFrameworkContractService;

  @Inject
  public ProjectTaskRecordServiceImpl(
      UnitProjectToolService unitProjectToolService,
      ProjectFrameworkContractService projectFrameworkContractService) {
    this.unitProjectToolService = unitProjectToolService;
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

    BigDecimal numberHoursADay =
        unitProjectToolService.getNumberHoursADay(projectTask.getProject());

    projectTask.setBudgetedTime(
        unitProjectToolService.getConvertedTime(
            projectTask.getBudgetedTime(),
            oldTimeUnit,
            projectTask.getTimeUnit(),
            numberHoursADay));
  }

  @Override
  public void computeQuantity(ProjectTask projectTask) throws AxelorException {
    if (projectTask == null
        || projectTask.getInvoicingUnit() == null
        || projectTask.getTimeUnit() == null) {
      return;
    }

    BigDecimal numberHoursADay =
        unitProjectToolService.getNumberHoursADay(projectTask.getProject());

    projectTask.setQuantity(
        unitProjectToolService.getConvertedTime(
            projectTask.getUpdatedTime(),
            projectTask.getTimeUnit(),
            projectTask.getInvoicingUnit(),
            numberHoursADay));
  }

  @Override
  public void computeFinancialDatas(ProjectTask projectTask) throws AxelorException {
    if (projectTask == null || projectTask.getProduct() == null) {
      return;
    }
    Map<String, Object> productDatas =
        projectFrameworkContractService.getProductDataFromContract(projectTask);
    BigDecimal numberHoursADay =
        unitProjectToolService.getNumberHoursADay(projectTask.getProject());
    Product product = projectTask.getProduct();
    Unit productUnit = Optional.of(product).map(Product::getSalesUnit).orElse(product.getUnit());

    if (productDatas.get("unitPrice") != null) {
      projectTask.setUnitPrice(
          unitProjectToolService.getConvertedTime(
              (BigDecimal) productDatas.get("unitPrice"),
              projectTask.getInvoicingUnit(),
              productUnit,
              numberHoursADay));
    }
    if (productDatas.get("unitCost") != null) {
      projectTask.setUnitCost(
          unitProjectToolService.getConvertedTime(
              (BigDecimal) productDatas.get("unitCost"),
              projectTask.getInvoicingUnit(),
              productUnit,
              numberHoursADay));
    }
  }
}
