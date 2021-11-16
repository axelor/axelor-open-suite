package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.google.inject.Inject;

public class JournalControlServiceImpl implements JournalControlService {

  protected MoveRepository moveRepository;

  @Inject
  public JournalControlServiceImpl(MoveRepository journalRepository) {
    this.moveRepository = journalRepository;
  }

  @Override
  public boolean isLinkedToMove(Journal journal) {

    return moveRepository.all().filter("self.journal = ?", journal).count() > 0;
  }
}
