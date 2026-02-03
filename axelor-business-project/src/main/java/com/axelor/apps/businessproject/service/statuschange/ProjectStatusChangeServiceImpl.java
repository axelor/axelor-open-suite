package com.axelor.apps.businessproject.service.statuschange;

import static com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage.PROJECT_BUSINESS_PROJECT_PROJECT_STATUS_NOT_FOUND;
import static com.axelor.apps.businessproject.service.statuschange.TaskStatusChangeServiceImpl.*;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.db.repo.ProjectStatusBusinessProjectRepository;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.taskreport.TaskMemberReportService;
import com.axelor.apps.businessproject.service.taskreport.TaskMemberReportServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectStatusChangeServiceImpl implements ProjectStatusChangeService {
  protected final Logger log = LoggerFactory.getLogger(ProjectStatusChangeServiceImpl.class);

  // Project Statuses
  public static final String PROJECT_STATUS_NEW = "New";
  public static final String PROJECT_STATUS_IN_PROGRESS = "In Progress";
  public static final String PROJECT_STATUS_TO_VALIDATE = "To Validate";
  public static final String PROJECT_STATUS_TO_INVOICE = "To Invoice";
  public static final String PROJECT_STATUS_INVOICED = "Invoiced";
  public static final String PROJECT_STATUS_PAID = "Paid";

  protected ProjectStatusBusinessProjectRepository projectStatusRepository;
  protected ProjectRepository projectRepo;
  protected TaskMemberReportService taskMemberReportRepo =
      Beans.get(TaskMemberReportServiceImpl.class);

  @Inject
  public ProjectStatusChangeServiceImpl(
      ProjectStatusBusinessProjectRepository projectStatusRepository,
      ProjectRepository projectRepo) {
    this.projectStatusRepository = projectStatusRepository;
    this.projectRepo = projectRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProjectStatus(Project project) throws AxelorAlertException {
    if (project == null) return;
    initProjectStatus(project);

    // If a project's status is one of the Completed statuses then we only move forward
    if (project.getProjectStatus().getIsCompleted()) return;

    String appropriateStatus = determineAppropriateStatus(project);

    if (!Objects.equals(appropriateStatus, project.getProjectStatus().getName())) {
      setStatus(project, appropriateStatus);
    }
  }

  @Override
  public void setInProgressStatus(Project project) throws AxelorAlertException {
    setStatus(project, PROJECT_STATUS_IN_PROGRESS);
  }

  @Override
  public void setToValidateStatus(Project project) throws AxelorAlertException {
    setStatus(project, PROJECT_STATUS_TO_VALIDATE);
  }

  @Override
  public void revertStatusForUnReportedTask(Project project) throws AxelorAlertException {
    setStatus(project, PROJECT_STATUS_IN_PROGRESS);
  }

  @Override
  public void setToInvoiceStatus(Project project) throws AxelorAlertException {
    if (Beans.get(ProjectBusinessService.class).readyToInvoice(project)) {
      setStatus(project, PROJECT_STATUS_TO_INVOICE);
    }
  }

  @Override
  public void setInvoicedStatus(Project project) throws AxelorAlertException {
    if (project == null) return;
    initProjectStatus(project);

    if (PROJECT_STATUS_PAID.equals(project.getProjectStatus().getName())) return;

    setStatus(project, PROJECT_STATUS_INVOICED);
  }

  @Override
  public void setPaidStatus(Project project) throws AxelorAlertException {
    setStatus(project, PROJECT_STATUS_PAID);
  }

  protected void setStatus(Project project, String statusName) throws AxelorAlertException {
    if (project == null) return;

    initProjectStatus(project);
    ProjectStatus status = getProjectStatus(statusName);
    String currentStatusName = project.getProjectStatus().getName();
    project.setProjectStatus(status);
    projectRepo.save(project);
    log.info(
        "Project {} status changed from {} to {}",
        project.getCode(),
        currentStatusName,
        statusName);
  }

  protected void initProjectStatus(Project project) throws AxelorAlertException {
    if (project == null) return;

    if (project.getProjectStatus() == null) {
      ProjectStatus newStatus = getProjectStatus(PROJECT_STATUS_NEW);
      project.setProjectStatus(newStatus);
    }
  }

  protected ProjectStatus getProjectStatus(String statusName) throws AxelorAlertException {
    ProjectStatus status = projectStatusRepository.findByNameIgnoreCase(statusName);
    if (status == null) {
      log.warn("Project status '{}' not found in system", statusName);
      throw new AxelorAlertException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PROJECT_BUSINESS_PROJECT_PROJECT_STATUS_NOT_FOUND),
          statusName);
    }
    return status;
  }

  protected String determineAppropriateStatus(Project project) {
    // Check the hierachy downward to determine the right status for the project

    // Check if it is ready to invoice
    if (Beans.get(ProjectBusinessService.class).readyToInvoice(project)) {
      return PROJECT_STATUS_TO_INVOICE;
    }

    // Check if project is ready to be set To Validate
    if (readyForValidation(project)) {
      return PROJECT_STATUS_TO_VALIDATE;
    }

    // Check if project is ready to be set In Progress
    if (readyForInProgress(project)) {
      return PROJECT_STATUS_IN_PROGRESS;
    }

    // Default Status New
    return PROJECT_STATUS_NEW;
  }

  protected boolean readyForValidation(Project project) {
    if (project == null || project.getProjectTaskList() == null) {
      return false;
    }

    return project.getProjectTaskList().stream()
        .allMatch(task -> Beans.get(TaskMemberReportService.class).hasTaskMemberReport(task));
  }

  protected boolean readyForInProgress(Project project) {
    if (project == null || project.getProjectTaskList() == null) {
      return false;
    }

    // Check if any task is in Progress, Feedback, or Done
    return project.getProjectTaskList().stream()
        .anyMatch(
            task -> {
              String status = task.getStatus().getName();
              return TASK_STATUS_IN_PROGRESS.equals(status)
                  || TASK_STATUS_FEEDBACK.equals(status)
                  || TASK_STATUS_DONE.equals(status);
            });
  }

  protected boolean isInvoicedOrPaid(Project project) {
    if (project == null) return false;

    String currentStatusName = project.getProjectStatus().getName();

    return PROJECT_STATUS_INVOICED.equals(currentStatusName)
        || PROJECT_STATUS_PAID.equals(currentStatusName);
  }
}
