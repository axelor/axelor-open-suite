package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;

public class ProjectWorkflowServiceImpl implements ProjectWorkflowService {

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void startProject(Project project) throws AxelorException {
    if (project.getStatusSelect() == null
        || project.getStatusSelect() != ProjectRepository.STATE_NEW) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PROJECT_START_WRONG_STATUS));
    }
    project.setStatusSelect(ProjectRepository.STATE_IN_PROGRESS);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void finishProject(Project project) throws AxelorException {
    if (project.getStatusSelect() == null
        || project.getStatusSelect() != ProjectRepository.STATE_IN_PROGRESS) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PROJECT_FINISH_WRONG_STATUS));
    }
    project.setStatusSelect(ProjectRepository.STATE_FINISHED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void cancelProject(Project project) throws AxelorException {
    if (project.getStatusSelect() == null
        || project.getStatusSelect() == ProjectRepository.STATE_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PROJECT_CANCEL_WRONG_STATUS));
    }
    project.setStatusSelect(ProjectRepository.STATE_CANCELED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void backToNewProject(Project project) throws AxelorException {
    if (project.getStatusSelect() == null
        || project.getStatusSelect() != ProjectRepository.STATE_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PROJECT_BACK_NEW_WRONG_STATUS));
    }
    project.setStatusSelect(ProjectRepository.STATE_NEW);
  }
}
