package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.google.inject.Inject;

public class AnalyticJournalControlServiceImpl implements AnalyticJournalControlService {

  protected AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public AnalyticJournalControlServiceImpl(AnalyticMoveLineRepository moveRepository) {
    this.analyticMoveLineRepository = moveRepository;
  }

  @Override
  public Boolean isInAnalyticMoveLine(AnalyticJournal analyticJournal) {
    return analyticMoveLineRepository
            .all()
            .filter("self.analyticJournal = ?", analyticJournal)
            .count()
        > 0;
  }
}
