package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.businessproject.db.ExtraExpenseLine;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class ExtraExpenseLineBusinessProjectRepository extends ExtraExpenseLineRepository {

  protected ProjectStatusChangeService projectStatusChangeService;

  @Inject
  public ExtraExpenseLineBusinessProjectRepository(
      ProjectStatusChangeService projectStatusChangeService) {
    this.projectStatusChangeService = projectStatusChangeService;
  }

  @Override
  public ExtraExpenseLine save(ExtraExpenseLine extraExpenseLine) {

    try {
      extraExpenseLine = super.save(extraExpenseLine);
      projectStatusChangeService.updateProjectStatus(extraExpenseLine.getProject());
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e);
    }

    return extraExpenseLine;
  }

  @Override
  public void remove(ExtraExpenseLine extraExpenseLine) {
    try {
      projectStatusChangeService.updateProjectStatus(extraExpenseLine.getProject());
      super.remove(extraExpenseLine);
    } catch (AxelorAlertException e) {
      throw new PersistenceException(e);
    }
  }
}
