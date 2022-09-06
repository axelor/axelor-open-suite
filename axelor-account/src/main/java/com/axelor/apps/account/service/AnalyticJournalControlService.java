package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.exception.AxelorException;

public interface AnalyticJournalControlService {

  /**
   * This method checks if name of analyticJournal for a company is already in database.
   *
   * @param analyticJournal
   * @throws AxelorException
   */
  void controlDuplicateCode(AnalyticJournal analyticJournal) throws AxelorException;

  /**
   * This method checks if analyticJournal is already use in a {@link AnalyticMoveLine}
   *
   * @param analyticJournal
   */
  boolean isInAnalyticMoveLine(AnalyticJournal analyticJournal);

  void toggleStatusSelect(AnalyticJournal analyticJournal);
}
