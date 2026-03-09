package com.axelor.apps.businessproject.service.statuschange;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.project.db.ProjectTask;

public interface TaskStatusChangeService {

  /**
   * Updates task status to Feedback when task report is saved
   *
   * @param taskReport The saved task report
   */
  void handleTaskReportSaved(TaskReport taskReport) throws AxelorAlertException;

  /**
   * Reverts the task status when task report is deleted
   *
   * @param taskReport The deleted task report
   */
  void handleTaskReportDeleted(TaskReport taskReport) throws AxelorAlertException;

  void changeTaskStatusToDone(ProjectTask task) throws AxelorAlertException;

  void revertTaskStatusOnTimesheetLineCancel(ProjectTask task) throws AxelorAlertException;

  void setTaskStatusFeedback(ProjectTask task) throws AxelorAlertException;
}
