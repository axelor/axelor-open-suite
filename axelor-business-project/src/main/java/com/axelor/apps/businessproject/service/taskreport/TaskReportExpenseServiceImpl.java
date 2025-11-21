package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.businessproject.db.ExtraExpenseLine;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.ExtraExpenseLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskReportExpenseServiceImpl implements TaskReportExpenseService {

  private static final Logger log = LoggerFactory.getLogger(TaskReportExpenseServiceImpl.class);

  // Product codes
  public static final String CODE_TRAVEL_EXPENSES = "11KM";
  public static final String CODE_TOOLS_USAGE = "TOOLUSAGE";
  public static final String CODE_DIRT_ALLOWANCE = "DIRTALLOWANCE";

  protected ProductRepository productRepo;
  protected ExtraExpenseLineRepository extraExpenseLineRepo;

  @Inject
  public TaskReportExpenseServiceImpl(
      ProductRepository productRepo, ExtraExpenseLineRepository extraExpenseLineRepo) {
    this.productRepo = productRepo;
    this.extraExpenseLineRepo = extraExpenseLineRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<ExtraExpenseLine> createOrUpdateExtraExpenseLinesFromTaskReport(
      TaskReport taskReport) {
    Project project = taskReport.getProject();
    if (project == null) {
      log.warn("TaskReport has no project assigned.");
      return new ArrayList<>();
    }

    // Get first project task
    if (project.getProjectTaskList() == null || project.getProjectTaskList().isEmpty()) {
      log.warn("Project has no tasks. Cannot create expense lines.");
      return new ArrayList<>();
    }
    ProjectTask projectTask = project.getProjectTaskList().get(0);

    // Map of product codes to boolean flags from TaskReport
    Map<String, Boolean> requiredExpenses = new HashMap<>();
    requiredExpenses.put(CODE_TRAVEL_EXPENSES, taskReport.getTravelExpenses());
    requiredExpenses.put(CODE_TOOLS_USAGE, taskReport.getToolsUsage());
    requiredExpenses.put(CODE_DIRT_ALLOWANCE, taskReport.getDirtAllowance());

    if (taskReport.getExtraExpenseLineList() == null) {
      taskReport.setExtraExpenseLineList(new ArrayList<>());
    }

    // Remove lines that are no longer needed
    Iterator<ExtraExpenseLine> iterator = taskReport.getExtraExpenseLineList().iterator();
    while (iterator.hasNext()) {
      ExtraExpenseLine line = iterator.next();
      String productCode =
          line.getExpenseProduct() != null ? line.getExpenseProduct().getCode() : null;

      if (productCode != null
          && requiredExpenses.containsKey(productCode)
          && !requiredExpenses.get(productCode)) {
        iterator.remove();
        if (line.getId() != null) {
          extraExpenseLineRepo.remove(line);
        }
      }
    }

    // Get existing product codes
    List<String> existingProductCodes = new ArrayList<>();
    for (ExtraExpenseLine line : taskReport.getExtraExpenseLineList()) {
      if (line.getExpenseProduct() != null && line.getExpenseProduct().getCode() != null) {
        existingProductCodes.add(line.getExpenseProduct().getCode());
      }
    }

    // Add new lines for required expenses that don't exist yet
    if (taskReport.getTravelExpenses() && !existingProductCodes.contains(CODE_TRAVEL_EXPENSES)) {
      addExpenseLine(taskReport, project, projectTask, CODE_TRAVEL_EXPENSES, "Travel Expenses");
    }

    if (taskReport.getToolsUsage() && !existingProductCodes.contains(CODE_TOOLS_USAGE)) {
      addExpenseLine(taskReport, project, projectTask, CODE_TOOLS_USAGE, "Tools Usage");
    }

    if (taskReport.getDirtAllowance() && !existingProductCodes.contains(CODE_DIRT_ALLOWANCE)) {
      addExpenseLine(taskReport, project, projectTask, CODE_DIRT_ALLOWANCE, "Dirt Allowance");
    }

    log.info("Total extra expense lines: {}", taskReport.getExtraExpenseLineList().size());
    return taskReport.getExtraExpenseLineList();
  }

  private Product findExpenseProduct(String productCode) {
    Product product = productRepo.findByCode(productCode);

    if (product == null) {
      log.warn("Product not found for code: {}", productCode);
      return null;
    }

    if (!Boolean.TRUE.equals(product.getExpense())) {
      log.warn("Product {} is not marked as expense type", productCode);
      return null;
    }

    return product;
  }

  private void addExpenseLine(
      TaskReport taskReport,
      Project project,
      ProjectTask projectTask,
      String productCode,
      String comments) {

    Product product = findExpenseProduct(productCode);
    if (product == null) {
      return;
    }

    ExtraExpenseLine line = new ExtraExpenseLine();
    line.setTaskReport(taskReport);
    line.setProject(project);
    line.setProjectTask(projectTask);
    line.setExpenseProduct(product);
    line.setToInvoice(true);
    line.setInvoiced(false);
    line.setTotalAmount(product.getSalePrice() != null ? product.getSalePrice() : BigDecimal.ZERO);
    line.setComments(comments);

    taskReport.addExtraExpenseLineListItem(line);
  }
}
