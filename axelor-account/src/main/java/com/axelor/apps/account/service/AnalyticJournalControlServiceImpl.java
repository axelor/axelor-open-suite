package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.repo.AnalyticJournalRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;

public class AnalyticJournalControlServiceImpl implements AnalyticJournalControlService {

  protected AnalyticJournalRepository analyticJournalRepository;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public AnalyticJournalControlServiceImpl(
      AnalyticJournalRepository analyticJournalRepository,
      AnalyticMoveLineRepository moveRepository) {
    this.analyticJournalRepository = analyticJournalRepository;
    this.analyticMoveLineRepository = moveRepository;
  }

  @Override
  public void controlDuplicateCode(AnalyticJournal analyticJournal) throws AxelorException {
    Objects.requireNonNull(analyticJournal);
    if (analyticJournal.getCode() != null
        && analyticJournal.getCompany() != null
        && analyticJournalRepository
                .all()
                .filter(
                    "self.code = ?1 AND self.company = ?2",
                    analyticJournal.getCode(),
                    analyticJournal.getCompany())
                .fetchStream()
                .filter(aJournal -> aJournal.getId() != analyticJournal.getId())
                .count()
            > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
          I18n.get(IExceptionMessage.NOT_UNIQUE_NAME_ANALYTIC_JOURNAL),
          analyticJournal.getCompany().getName());
    }
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
