/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class AccountingReportManagementRepository extends AccountingReportRepository {

  @Inject protected AccountingReportService accountingReportService;

  @Override
  public AccountingReport save(AccountingReport accountingReport) {
    try {

      if (accountingReport.getRef() == null) {

        String seq = accountingReportService.getSequence(accountingReport);
        accountingReportService.setSequence(accountingReport, seq);
      }

      return super.save(accountingReport);
    } catch (Exception e) {
      JPA.em().getTransaction().rollback();
      JPA.runInTransaction(() -> TraceBackService.trace(e));
      JPA.em().getTransaction().begin();
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  @Override
  public AccountingReport copy(AccountingReport entity, boolean deep) {

    AccountingReport copy = super.copy(entity, deep);

    copy.setRef(null);
    copy.setStatusSelect(this.STATUS_DRAFT);
    copy.setPublicationDateTime(null);
    copy.setTotalDebit(BigDecimal.ZERO);
    copy.setTotalCredit(BigDecimal.ZERO);
    copy.setBalance(BigDecimal.ZERO);

    return copy;
  }
}
