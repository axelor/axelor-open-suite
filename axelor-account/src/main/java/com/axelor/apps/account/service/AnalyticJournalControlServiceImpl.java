/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.repo.AnalyticJournalRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.Map;
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

    StringBuilder query = new StringBuilder("self.code = :code AND self.company = :company");
    Map<String, Object> params = new HashMap<>();

    if (analyticJournal.getCode() != null && analyticJournal.getCompany() != null) {
      params.put("code", analyticJournal.getCode());
      params.put("company", analyticJournal.getCompany());
      if (analyticJournal.getId() != null) {
        query.append(" AND self.id != :analyticJournalId");
        params.put("analyticJournalId", analyticJournal.getId());
      }

      if (analyticJournalRepository.all().filter(query.toString()).bind(params).count() > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
            I18n.get(AccountExceptionMessage.NOT_UNIQUE_NAME_ANALYTIC_JOURNAL),
            analyticJournal.getCompany().getName());
      }
    }
  }

  @Override
  public boolean isInAnalyticMoveLine(AnalyticJournal analyticJournal) {
    return analyticMoveLineRepository
            .all()
            .filter("self.analyticJournal = ?", analyticJournal)
            .count()
        > 0;
  }

  @Override
  @Transactional
  public void toggleStatusSelect(AnalyticJournal analyticJournal) {
    if (analyticJournal != null) {
      if (analyticJournal.getStatusSelect() == AnalyticJournalRepository.STATUS_INACTIVE) {
        analyticJournal = activate(analyticJournal);
      } else {
        analyticJournal = desactivate(analyticJournal);
      }
      analyticJournalRepository.save(analyticJournal);
    }
  }

  protected AnalyticJournal activate(AnalyticJournal analyticJournal) {
    analyticJournal.setStatusSelect(AnalyticJournalRepository.STATUS_ACTIVE);
    return analyticJournal;
  }

  protected AnalyticJournal desactivate(AnalyticJournal analyticJournal) {
    analyticJournal.setStatusSelect(AnalyticJournalRepository.STATUS_INACTIVE);
    return analyticJournal;
  }
}
