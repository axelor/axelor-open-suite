package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;

public interface JournalControlService {

  /**
   * This method checks if journal is linked to any {@link Move}
   *
   * @param journal
   * @return true if you journal linked to a Move, else false
   */
  boolean isLinkedToMove(Journal journal);
}
