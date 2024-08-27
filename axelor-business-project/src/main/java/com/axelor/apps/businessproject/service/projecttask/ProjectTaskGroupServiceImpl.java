package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProjectTaskGroupServiceImpl implements ProjectTaskGroupService {

  protected ProjectTaskRecordService projectTaskRecordService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;

  @Inject
  public ProjectTaskGroupServiceImpl(
      ProjectTaskRecordService projectTaskRecordService,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService) {
    this.projectTaskRecordService = projectTaskRecordService;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
  }

  @Override
  public Map<String, Object> updateBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException {
    projectTaskRecordService.computeBudgetedTime(projectTask, oldTimeUnit);

    Map<String, Object> valuesMap = new HashMap<>(updateSoldTime(projectTask));

    valuesMap.put("budgetedTime", projectTask.getBudgetedTime());

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateSoldTime(ProjectTask projectTask) throws AxelorException {
    projectTask.setSoldTime(projectTask.getBudgetedTime());

    Map<String, Object> valuesMap = new HashMap<>(updateUpdatedTime(projectTask));

    valuesMap.put("soldTime", projectTask.getSoldTime());

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateUpdatedTime(ProjectTask projectTask) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    projectTask.setUpdatedTime(projectTask.getSoldTime());
    valuesMap.put("updatedTime", projectTask.getUpdatedTime());

    if (Objects.equals(ProjectTaskRepository.INVOICING_TYPE_PACKAGE, projectTask.getInvoicingType())
        && !projectTask.getInvoiced()
        && projectTask.getSaleOrderLine() == null
        && !projectTask.getIsTaskRefused()) {
      valuesMap.putAll(updateQuantity(projectTask));
    }

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateQuantity(ProjectTask projectTask) throws AxelorException {
    projectTaskRecordService.computeQuantity(projectTask);

    Map<String, Object> valuesMap = new HashMap<>(updateFinancialDatas(projectTask));
    valuesMap.put("quantity", projectTask.getQuantity());

    return valuesMap;
  }

  @Override
  public Map<String, Object> updateFinancialDatas(ProjectTask projectTask) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();
    projectTaskRecordService.computeFinancialDatas(projectTask);
    projectTaskBusinessProjectService.updateDiscount(projectTask);
    projectTaskBusinessProjectService.compute(projectTask);

    valuesMap.put("unitPrice", projectTask.getUnitPrice());
    valuesMap.put("unitCost", projectTask.getUnitCost());
    valuesMap.put("discountTypeSelect", projectTask.getDiscountTypeSelect());
    valuesMap.put("discountAmount", projectTask.getDiscountAmount());
    valuesMap.put("priceDiscounted", projectTask.getPriceDiscounted());
    valuesMap.put("exTaxTotal", projectTask.getExTaxTotal());
    valuesMap.put("totalCosts", projectTask.getTotalCosts());

    return valuesMap;
  }
}
