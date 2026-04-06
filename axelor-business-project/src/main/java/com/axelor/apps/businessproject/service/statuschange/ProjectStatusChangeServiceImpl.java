package com.axelor.apps.businessproject.service.statuschange;

import static com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage.PROJECT_BUSINESS_PROJECT_PROJECT_STATUS_NOT_FOUND;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.db.repo.ProjectStatusBusinessProjectRepository;
import com.axelor.apps.businessproject.db.repo.SubcontractorTaskRepository;
import com.axelor.apps.businessproject.db.repo.TaskStatusBusinessProjectRepository;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectStatusChangeServiceImpl extends TaskStatusChangeServiceImpl
    implements ProjectStatusChangeService {
  protected final Logger log = LoggerFactory.getLogger(ProjectStatusChangeServiceImpl.class);

  // Project Statuses
  protected static final String PROJECT_STATUS_NEW = "New";
  protected static final String PROJECT_STATUS_IN_PROGRESS = "In Progress";
  protected static final String PROJECT_STATUS_TO_VALIDATE = "To Validate";
  protected static final String PROJECT_STATUS_TO_INVOICE = "To Invoice";
  protected static final String PROJECT_STATUS_INVOICED = "Invoiced";
  protected static final String PROJECT_STATUS_PAID = "Paid";

  protected ProjectStatusBusinessProjectRepository projectStatusRepository;
  protected ProjectRepository projectRepo;
  protected TaskStatusBusinessProjectRepository taskStatusRepo;
  protected ProjectTaskRepository projectTaskRepo;
  protected ProjectBusinessService projectService;
  protected AppProjectService appProjectService;

  @Inject
  public ProjectStatusChangeServiceImpl(
      ProjectStatusBusinessProjectRepository projectStatusRepository,
      ProjectRepository projectRepo,
      TaskStatusBusinessProjectRepository taskStatusRepo,
      ProjectTaskRepository projectTaskRepo,
      ProjectBusinessService projectService,
      AppProjectService appProjectService) {
    super(taskStatusRepo, projectTaskRepo, appProjectService);
    this.projectStatusRepository = projectStatusRepository;
    this.projectRepo = projectRepo;
    this.projectService = projectService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProjectStatus(Project project) throws AxelorAlertException {
    if (isAutomaticStatusManagementDisabled()) {
      log.debug("Automatic project status update is disabled in configuration.");
      return;
    }

    if (project == null) return;
    initProjectStatus(project);

    // If a project's status is one of the Completed statuses then we only move forward
    if (project.getProjectStatus().getIsCompleted()) return;

    // NOTE: The call order of the for to readyForReview and the determination of the project status
    // is important

    Boolean isReadyForReview = projectService.isProjectReadyForReview(project);
    if (!Objects.equals(project.getIsReadyForReview(), isReadyForReview)) {
      project.setIsReadyForReview(isReadyForReview);
      log.debug("project is ready for review: {}", project.getIsReadyForReview());
    }

    // If a confirmed project fails the ready to invoice condition, it for sure should not be
    // confirmed
    if (Boolean.TRUE.equals(project.getIsConfirmedForInvoicing())
        && !projectService.readyToInvoice(project)) {
      project.setIsConfirmedForInvoicing(Boolean.FALSE);
    }

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
    if (projectService.readyToInvoice(project)) {
      setStatus(project, PROJECT_STATUS_TO_INVOICE);
    }
  }

  @Override
  public void setInvoicedStatus(Project project) throws AxelorAlertException {
    if (isAutomaticStatusManagementDisabled()) {
      log.debug("Automatic status update is disabled in configuration.");
      return;
    }
    if (project == null) return;
    initProjectStatus(project);

    if (PROJECT_STATUS_PAID.equals(project.getProjectStatus().getName())) return;

    setStatus(project, PROJECT_STATUS_INVOICED);
  }

  @Override
  public void setPaidStatus(Project project) throws AxelorAlertException {
    if (isAutomaticStatusManagementDisabled()) {
      log.debug("Automatic status update is disabled in configuration.");
      return;
    }
    setStatus(project, PROJECT_STATUS_PAID);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void setStatus(Project project, String statusName) throws AxelorAlertException {
    if (project == null) return;

    initProjectStatus(project);
    ProjectStatus status = getProjectStatus(statusName);
    ProjectStatus currentStatusName = project.getProjectStatus();

    if (!Objects.equals(currentStatusName, status)) {
      project.setProjectStatus(status);
      projectRepo.save(project);
      log.info(
          "Project {} status changed from {} to {}",
          project.getCode(),
          currentStatusName.getName(),
          statusName);
    }
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

  protected String determineAppropriateStatus(Project project) throws AxelorAlertException {
    // Check the hierachy downward to determine the right status for the project

    // Check if it is ready to invoice
    if (projectService.readyToInvoice(project)) {
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

  /**
   * Determines if a project is ready for validation. A project is ready for validation if it has at
   * least one task or expense and all present tasks are reported and all present expenses are sent.
   * Or if it has already been marked as ready for review and not yet confirmed for invoicing,
   *
   * @param project the project to check
   * @return true if the project is ready for validation, false otherwise
   */
  protected boolean readyForValidation(Project project) {
    if (project == null) {
      return false;
    }

    boolean hasTaskOrExpense =
        projectService.hasTask(project) || projectService.hasExpense(project);

    boolean taskConditionMet =
        !projectService.hasTask(project) || projectService.allTaskReported(project);

    boolean expenseConditionMet =
        !projectService.hasExpense(project) || projectService.allExpensesSentOrValidated(project);

    boolean readyForReviewConditionMet =
        Boolean.TRUE.equals(project.getIsReadyForReview())
            && !Boolean.TRUE.equals(project.getIsConfirmedForInvoicing());

    return (hasTaskOrExpense && taskConditionMet && expenseConditionMet)
        || readyForReviewConditionMet;
  }

  /**
   * Determines if a project is ready to be in progress. For a project to be in progress, it must
   * have at least one billable item or an item of work
   *
   * @param project
   * @return
   * @throws AxelorAlertException
   */
  protected boolean readyForInProgress(Project project) throws AxelorAlertException {
    if (project == null) {
      return false;
    }

    boolean hasSubcontractorTasks =
        Beans.get(SubcontractorTaskRepository.class)
                .all()
                .filter("self.project.id = :projectId")
                .bind("projectId", project.getId())
                .count()
            > 0;

    return projectService.hasExtraExpenses(project)
        || projectService.hasTask(project)
        || hasSubcontractorTasks
        || projectService.hasExpense(project);
  }

  protected boolean isInvoicedOrPaid(Project project) {
    if (project == null) return false;

    String currentStatusName = project.getProjectStatus().getName();

    return PROJECT_STATUS_INVOICED.equals(currentStatusName)
        || PROJECT_STATUS_PAID.equals(currentStatusName);
  }
}
