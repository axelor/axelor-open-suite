package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.exception.AxelorException;

public interface AnalyticJournalControlService {

  /**
   * This methods check if name of analyticJournal for a company is already in database.
   *
   * @param analyticJournal
   * @throws AxelorException
   */
  void controlDuplicateCode(AnalyticJournal analyticJournal) throws AxelorException;
}
