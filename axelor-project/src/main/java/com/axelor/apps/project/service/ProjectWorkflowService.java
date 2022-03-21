package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.exception.AxelorException;

public interface ProjectWorkflowService {

  void startProject(Project project) throws AxelorException;

  void finishProject(Project project) throws AxelorException;

  void cancelProject(Project project) throws AxelorException;

  void backToNewProject(Project project) throws AxelorException;
}
