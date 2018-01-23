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
import com.axelor.apps.base.db.StopReason;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;

import java.time.LocalDate;
import java.util.List;

public class BlockingService {

    protected LocalDate today;

    public BlockingService() {
        this.today = Beans.get(AppBaseService.class).getTodayDate();
    }

    /**
     * Checks if {@code partner} is blocked for the {@code blockingType}
     *
     * @param partner      Partner to check blocking
     * @param company      Company associated with the blocking
     * @param blockingType Type of blocking
     * @return blocking reason if partner is blocked for provided company and blocking type, null otherwise
     */
    public StopReason isBlocked(Partner partner, Company company, int blockingType) {
        List<Blocking> blockings = partner.getBlockingList();

        for (Blocking blocking : blockings) {
            if (blocking.getCompanySet().contains(company) && blocking.getBlockingSelect().equals(blockingType) && blocking.getBlockingToDate().compareTo(today) >= 0) {
                return blocking.getBlockingReason();
            }
        }

        return null;
    }
}
