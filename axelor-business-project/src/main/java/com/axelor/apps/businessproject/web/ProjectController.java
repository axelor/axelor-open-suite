/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.dms.DMSFileService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.*;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticTemplateService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.apps.businessproject.translation.ITranslation;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProjectController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected ProjectFilesService projectFilesService;
  @Inject protected ProjectBusinessService projectService;
  @Inject protected DMSFileService dmsFileService;
  @Inject protected MetaFileRepository metaFileRepository;
  @Inject protected TaskReportService taskReportService;

  public void generateQuotation(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      SaleOrder order = Beans.get(ProjectBusinessService.class).generateQuotation(project);
      response.setView(
          ActionView.define(I18n.get("Sale quotation"))
              .model(SaleOrder.class.getName())
              .add("form", "sale-order-form")
              .param("forceTitle", "true")
              .context("_showRecord", String.valueOf(order.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generatePurchaseQuotation(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getId() != null) {
      response.setView(
          ActionView.define(I18n.get("Purchase Order"))
              .model(PurchaseOrder.class.getName())
              .add("form", "purchase-order-form")
              .add("grid", "purchase-order-quotation-grid")
              .param("search-filters", "purchase-order-filters")
              .context("_project", Beans.get(ProjectRepository.class).find(project.getId()))
              .map());
    }
  }

  public void countToInvoice(ActionRequest request, ActionResponse response) {

    Project project = request.getContext().asType(Project.class);

    int toInvoiceCount = 0;
    if (project.getId() != null) {
      toInvoiceCount = Beans.get(InvoicingProjectService.class).countToInvoice(project);
    }

    response.setValue("$toInvoiceCounter", toInvoiceCount);
  }

  public void showInvoicingProjects(ActionRequest request, ActionResponse response) {

    Project project = request.getContext().asType(Project.class);
    project = Beans.get(ProjectRepository.class).find(project.getId());
    InvoicingProjectService invoicingProjectService = Beans.get(InvoicingProjectService.class);
    InvoicingProjectRepository invoicingProjectRepository =
        Beans.get(InvoicingProjectRepository.class);

    InvoicingProject invoicingProject =
        Beans.get(InvoicingProjectRepository.class)
            .all()
            .filter("self.project.id = ?", project.getId())
            .fetchOne();

    ActionView.ActionViewBuilder view =
        ActionView.define(I18n.get("Invoice Business Project"))
            .model(InvoicingProject.class.getName())
            .add("form", "invoicing-project-form")
            .param("forceEdit", "true");

    if (invoicingProject != null) {
      // Open or update existing invoicing project
      invoicingProject = invoicingProjectService.refreshInvoicingProject(invoicingProject, project);
      view.context("_showRecord", invoicingProject.getId());
    } else {
      // Creating new invoicing project : pre-fill with project
      view.context("_project", project);
    }

    response.setView(view.map());
  }

  @ErrorException
  public void getPartnerData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    Partner partner = project.getClientPartner();

    if (partner == null) return;

    project = Beans.get(BusinessProjectService.class).computePartnerData(project, partner);

    List<PartnerAddress> partnerAddresses = partner.getPartnerAddressList();
    Address deliveryAddress = null;

    if (project != null) {
      if (partnerAddresses != null && !partnerAddresses.isEmpty()) {
        // get a delivery address marked as default
        deliveryAddress =
            partnerAddresses.stream()
                .filter(pa -> pa.getIsDeliveryAddr() && pa.getIsDefaultAddr())
                .map(PartnerAddress::getAddress)
                .findFirst()
                .orElse(null);

        // if no addr is marked as default we just get a random delivery addr
        if (deliveryAddress == null) {
          deliveryAddress =
              partnerAddresses.stream()
                  .filter(PartnerAddress::getIsDeliveryAddr)
                  .map(PartnerAddress::getAddress)
                  .findFirst()
                  .orElse(null);
        }
      }

      response.setValue("analyticDistributionTemplate", project.getAnalyticDistributionTemplate());
      response.setValue("currency", project.getCurrency());
      response.setValue("priceList", project.getPriceList());
      response.setValue("customerAddress", deliveryAddress);
      response.setValue(
          "contactPartner",
          partner.getContactPartnerSet().size() == 1
              ? partner.getContactPartnerSet().iterator().next()
              : null);
    }
  }

  public void computeProjectTotals(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    ProjectBusinessService projectBusinessService = Beans.get(ProjectBusinessService.class);
    projectBusinessService.computeProjectTotals(project);

    List<String> projectTaskList = projectBusinessService.checkPercentagesOver1000OnTasks(project);
    if (projectTaskList.isEmpty()) {
      response.setNotify(I18n.get(BusinessProjectExceptionMessage.PROJECT_UPDATE_TOTALS_SUCCESS));
    } else {
      response.setAlert(
          String.format(
                  I18n.get(ITranslation.PROJECT_TASK_FOLLOW_UP_VALUES_TOO_HIGH), projectTaskList)
              + I18n.get(BusinessProjectExceptionMessage.PROJECT_UPDATE_TOTALS_SUCCESS));
    }
    response.setReload(true);
  }

  public void getProjectTimeFollowUpData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_REPORT_NO_ID_FOUND);
    }
    Map<String, Object> data =
        Beans.get(ProjectBusinessService.class)
            .processRequestToDisplayTimeReporting(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void getProjectFinancialFollowUpData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_REPORT_NO_ID_FOUND);
    }

    Map<String, Object> data =
        Beans.get(ProjectBusinessService.class)
            .processRequestToDisplayFinancialReporting(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void getProjectHistoryData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_REPORT_NO_ID_FOUND);
    }
    Map<String, Object> data =
        Beans.get(ProjectHistoryService.class)
            .processRequestToDisplayProjectHistory(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void checkProjectState(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    project = JPA.find(Project.class, project.getId());

    Integer closingProjectRuleSelect =
        Beans.get(AppBusinessProjectService.class)
            .getAppBusinessProject()
            .getClosingProjectRuleSelect();

    String errorMessage =
        Beans.get(BusinessProjectClosingControlService.class).checkProjectState(project);
    if (errorMessage.isEmpty()) {
      return;
    }
    if (closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_BLOCKING) {
      response.setError(errorMessage, null, null, "action-refresh-record");
    } else if (closingProjectRuleSelect
        == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_NON_BLOCKING) {
      response.setAlert(errorMessage, null, null, null, "action-refresh-record");
    }
  }

  public void setAnalyticDistributionTemplateRequired(
      ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);

      response.setAttr(
          "analyticDistributionTemplate",
          "required",
          Beans.get(ProjectAnalyticTemplateService.class)
              .isAnalyticDistributionTemplateRequired(project));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void getAnalyticDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      response.setValue(
          "analyticDistributionTemplate",
          Beans.get(ProjectAnalyticTemplateService.class)
              .getDefaultAnalyticDistributionTemplate(project));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @ErrorException
  public void convertProject(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Object projectId = request.getContext().get("_projectId");
    if (projectId == null) {
      return;
    }

    Project project = Beans.get(ProjectRepository.class).find(Long.valueOf(projectId.toString()));
    Company company = (Company) request.getContext().get("company");
    Partner clientPartner = (Partner) request.getContext().get("clientPartner");
    if (clientPartner == null || company == null) {
      response.setError(
          I18n.get(
              BusinessProjectExceptionMessage
                  .PROJECT_BUSINESS_PROJECT_MISSING_CLIENT_PARTNER_COMPANY));
    }

    Beans.get(BusinessProjectService.class).setAsBusinessProject(project, company, clientPartner);

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Business project"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "business-project-form")
            .add("kanban", "project-kanban")
            .param("search-filters", "project-project-filters")
            .context("_showRecord", project.getId())
            .domain("self.isBusinessProject = true");
    response.setCanClose(true);
    response.setView(builder.map());
  }

  public void findProjectFiles(ActionRequest request, ActionResponse response) {
    Long id = (Long) request.getContext().get("id");
    String model = (String) request.getContext().get("_model");
    if (id == null || model == null) {
      logger.debug("No model found");
      response.setAlert(I18n.get("Please save before viewing project files"));
      return;
    }

    Project project = Beans.get(ProjectBusinessService.class).findProjectFromModel(model, id);
    if (project == null) {
      logger.debug("No project found");
      response.setAlert(I18n.get("No project found"));
      return;
    }

    // If the home does not exist we create it as this ensures that after uploading
    // a file, it should reuse the domain filter so it shows the uploaded file directly and
    // does not require closing and opening back
    DMSFile home = dmsFileService.findOrCreateHome(project);

    if (home != null) {
      response.setView(
          ActionView.define(I18n.get("Project Files") + " - " + project.getFullName())
              .model(DMSFile.class.getName())
              .add("grid", "custom-mgm-project-files-grid")
              .add("form", "dms-file-form")
              .domain(
                  "self.isDirectory = false AND  (self.parent.id = :parentId OR self.parent.parent.id = :parentId)")
              .param("popup", "true")
              .context("_modelIdForAttachment", id)
              .context("_modelForAttachment", model)
              .context("parentId", home.getId())
              .map());
    } else {
      response.setAlert(I18n.get("Could not create a home folder for this project"));
    }
  }

  /**
   * Automatically opens the project task form if the context in which it's called is that of a
   * project which has no task
   */
  public void openTaskFormIfEmpty(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);

    if (project.getId() != null) {
      project = Beans.get(ProjectRepository.class).find(project.getId());
      long taskCount =
          project.getProjectTaskList().stream().filter(task -> !task.getIsTemplate()).count();

      if (taskCount == 0) {
        response.setView(
            ActionView.define("action-view-business-project-task-new-task-form")
                .model(ProjectTask.class.getName())
                .add("form", "custom-mgm-business-project-task-template-form")
                .param("popup", "reload")
                .param("show-toolbar", "false")
                .param("show-confirm", "true")
                .param("popup-save", "true")
                .context("_projectId", project.getId())
                .context("_typeSelect", ProjectTaskRepository.TYPE_TASK)
                .map());
      }
    }
  }

  public void setDefaultInvoiceFlags(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getId() == null) {
      return;
    }

    if (!Boolean.TRUE.equals(project.getIsInvoicingExpenses())) {
      response.setValue("isInvoicingExpenses", true);
    }

    if (!Boolean.TRUE.equals(project.getIsInvoicingTimesheet())) {
      response.setValue("isInvoicingTimesheet", true);
    }

    if (!Boolean.TRUE.equals(project.getIsInvoicingPurchases())) {
      response.setValue("isInvoicingPurchases", true);
    }
  }

  public void attachMetaFileToModel(ActionRequest request, ActionResponse response) {
    try {
      DMSFile dmsFile = request.getContext().asType(DMSFile.class);

      if (dmsFile.getMetaFile() == null) {
        response.setError(I18n.get("Please upload a file"));
        return;
      }

      Long modelId = ((Number) request.getContext().get("_modelIdForAttachment")).longValue();
      String model = (String) request.getContext().get("_modelForAttachment");

      boolean uploadDuplicateFile =
          Boolean.TRUE.equals(request.getContext().get("_uploadOnDuplicateFile"));

      Project project = projectService.findProjectFromModel(model, modelId);
      DMSFile projectFilesHome = dmsFileService.findOrCreateHome(project);

      MetaFile metaFile = metaFileRepository.find(dmsFile.getMetaFile().getId());
      String originalFileName = metaFile.getFileName();

      if (!uploadDuplicateFile) {
        if (projectFilesService.fileExistsInProjectFiles(projectFilesHome, originalFileName)) {
          response.setValue("_fileAlreadyExists", true);
          response.setValue("_metaFileId", metaFile.getId());
          return;
        }
      }

      if (uploadDuplicateFile) {
        metaFile = projectFilesService.renameMetaFileToAvailableName(metaFile, projectFilesHome);
      }

      projectFilesService.attachMetaFileToModel(modelId, model, metaFile);

      response.setCanClose(true);
      response.setNotify(I18n.get("File uploaded successfully"));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelFileUpload(ActionRequest request, ActionResponse response) {
    try {
      Number metaFileContextId = (Number) request.getContext().get("_metaFileId");

      if (metaFileContextId != null) {
        Long metaFileId = metaFileContextId.longValue();
        MetaFile metaFile = metaFileRepository.find(metaFileId);
        projectFilesService.cancelFileUpload(metaFile);
      }
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void syncTaskReportWithProject(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);

    if (project == null) {
      return;
    }

    projectService.syncTaskReportToProject(project);
  }

  public void showProjectSummary(ActionRequest request, ActionResponse response) {
    Project project = getProjectFromRequest(request);
    if (project == null) return;

    TaskReport taskReport = taskReportService.getTaskReport(project);

    String taskCount =
        taskReport != null ? taskReportService.getReportedTaskCount(taskReport) : "0/0";

    BigDecimal totalHours = BigDecimal.ZERO;

    long dirtAllowanceCount = 0;

    if (taskReport != null && taskReport.getTaskMemberReports() != null) {
      totalHours = taskReport.getTotalWorkHours();
      for (TaskMemberReport tmr : taskReport.getTaskMemberReports()) {
        if (Boolean.TRUE.equals(tmr.getDirtAllowance())) {
          dirtAllowanceCount++;
        }
      }
    }

    long expenseCount = projectService.getProjectExpenseCount(project);

    response.setValue("$totalTasksReported", taskCount);
    response.setValue("$totalHoursReported", totalHours);
    response.setValue("$totalDirtAllowance", dirtAllowanceCount);
    response.setValue("$totalExpensesReported", expenseCount);
  }

  public void updateProjectStatus(ActionRequest request, ActionResponse response) {
    Project project = getProjectFromRequest(request);

    try {
      Beans.get(ProjectStatusChangeService.class).updateProjectStatus(project);
      response.setReload(true);
    } catch (AxelorAlertException e) {
      TraceBackService.trace(response, e);
    }
  }

  private Project getProjectFromRequest(ActionRequest request) {
    Project project = request.getContext().asType(Project.class);

    if (project == null || project.getId() == null) return null;
    return Beans.get(ProjectRepository.class).find(project.getId());
  }
}
