/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import org.joda.time.LocalDate;

public class AccountBlockingService extends BlockingService {

  private LocalDate today;

  @Inject
  public AccountBlockingService() {

    this.today = Beans.get(GeneralService.class).getTodayDate();
  }

  /**
   * Le tiers est t'il bloqué en prélèvement
   *
   * @return
   */
  public boolean isDebitBlockingBlocking(Blocking blocking) {

    if (blocking != null && blocking.getDebitBlockingOk()) {

      if (blocking.getDebitBlockingToDate() != null
          && blocking.getDebitBlockingToDate().isBefore(today)) {
        return false;
      } else {
        return true;
      }
    }

    return false;
  }

  /**
   * Le tiers est t'il bloqué en prélèvement
   *
   * @return
   */
  public boolean isDebitBlockingBlocking(Partner partner, Company company) {

    return this.isDebitBlockingBlocking(this.getBlocking(partner, company));
  }

  /**
   * Le tiers est t'il bloqué en remboursement
   *
   * @return
   */
  public boolean isReminderBlocking(Blocking blocking) {

    if (blocking != null && blocking.getReimbursementBlockingOk()) {

      if (blocking.getReimbursementBlockingToDate() != null
          && blocking.getReimbursementBlockingToDate().isBefore(today)) {
        return false;
      } else {
        return true;
      }
    }

    return false;
  }

  /**
   * Le tiers est t'il bloqué en remboursement
   *
   * @return
   */
  public boolean isReminderBlocking(Partner partner, Company company) {

    return this.isReminderBlocking(this.getBlocking(partner, company));
  }
}
