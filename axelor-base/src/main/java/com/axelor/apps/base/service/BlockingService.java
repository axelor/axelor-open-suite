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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import org.joda.time.LocalDate;

public class BlockingService {

  private LocalDate today;

  @Inject
  public BlockingService() {

    this.today = Beans.get(GeneralService.class).getTodayDate();
  }

  public Blocking getBlocking(Partner partner, Company company) {

    if (partner != null && company != null && partner.getBlockingList() != null) {
      for (Blocking blocking : partner.getBlockingList()) {
        if (blocking.getCompany().equals(company)) {
          return blocking;
        }
      }
    }

    return null;
  }
}
