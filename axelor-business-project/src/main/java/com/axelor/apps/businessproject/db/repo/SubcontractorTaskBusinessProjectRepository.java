package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.businessproject.db.SubcontractorTask;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class SubcontractorTaskBusinessProjectRepository extends SubcontractorTaskRepository {

  protected ProjectStatusChangeService projectStatusChangeService;

  @Inject
  public SubcontractorTaskBusinessProjectRepository(
      ProjectStatusChangeService projectStatusChangeService) {
    this.projectStatusChangeService = projectStatusChangeService;
  }

  @Override
  public SubcontractorTask save(SubcontractorTask subcontractorTask) {

    try {
      subcontractorTask = super.save(subcontractorTask);
      projectStatusChangeService.updateProjectStatus(subcontractorTask.getProject());
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e);
    }

    return subcontractorTask;
  }

  @Override
  public void remove(SubcontractorTask subcontractorTask) {
    try {
      projectStatusChangeService.updateProjectStatus(subcontractorTask.getProject());
      super.remove(subcontractorTask);
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e);
    }
  }
}
