package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.exception.AxelorException;

public interface ProjectWorkflowService {

  /**
   * Set the project status to in progress.
   *
   * @param project
   * @throws AxelorException if the project wasn't new.
   */
  void startProject(Project project) throws AxelorException;

  /**
   * Set the project status to finished.
   *
   * @param project
   * @throws AxelorException if the project wasn't in progress.
   */
  void finishProject(Project project) throws AxelorException;

  /**
   * Set the project status to canceled.
   *
   * @param project
   * @throws AxelorException if the project was already canceled.
   */
  void cancelProject(Project project) throws AxelorException;

  /**
   * Set the project status to new.
   *
   * @param project
   * @throws AxelorException if the project wasn't canceled.
   */
  void backToNewProject(Project project) throws AxelorException;
}
