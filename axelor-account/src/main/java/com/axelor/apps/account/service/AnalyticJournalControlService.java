package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;

public interface AnalyticJournalControlService {

  /**
   * This method checks if analyticJournal is already use in a {@link AnalyticMoveLine}
   *
   * @param analyticJournal
   */
  Boolean isInAnalyticMoveLine(AnalyticJournal analyticJournal);
}
