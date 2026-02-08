package com.axelor.apps.businessproject.service.statuschange;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.project.db.Project;

public interface ProjectStatusChangeService {

  /**
   * Directly set a project's status to "In Progress"
   *
   * @param project target project
   * @throws AxelorAlertException Thrown when the status the project should be set to can not be
   *     found.
   */
  void setInProgressStatus(Project project) throws AxelorAlertException;

  /**
   * Directly set a project's status to "To Validate"
   *
   * @param project Target project
   * @throws AxelorAlertException Thrown when the status the project should be set to can not be
   *     found.
   */
  void setToValidateStatus(Project project) throws AxelorAlertException;

  void revertStatusForUnReportedTask(Project project) throws AxelorAlertException;

  /**
   * Directly set a project's status to "To Invoice"
   *
   * @param project Target project
   * @throws AxelorAlertException Thrown when the status the project should be set to can not be
   *     found.
   */
  void setToInvoiceStatus(Project project) throws AxelorAlertException;

  /**
   * Directly set a project's status to "Invoiced"
   *
   * @param project Target project
   * @throws AxelorAlertException Thrown when the status the project should be set to can not be
   *     found.
   */
  void setInvoicedStatus(Project project) throws AxelorAlertException;

  /**
   * Directly sets a project's status to paid
   *
   * @param project Target project
   * @throws AxelorAlertException Thrown when the status the project should be set to can not be
   *     found.
   */
  void setPaidStatus(Project project) throws AxelorAlertException;

  /**
   * Updates the status of a project based on the current state of the system. Do not use this
   * method to change a project's staus to Invoiced or Paid. Rather use the setInvoicedStatus or
   * setPaidStatus methods for that.
   *
   * @param project project for which to update the status
   * @throws AxelorAlertException Thrown when the status the project should be set to can not be
   *     found.
   */
  void updateProjectStatus(Project project) throws AxelorAlertException;
}
