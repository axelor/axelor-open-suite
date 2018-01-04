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

import java.time.LocalDate;
import java.util.List;

import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AccountBlockingService extends BlockingService {

	private LocalDate today;

	@Inject
	public AccountBlockingService() {
		this.today = Beans.get(AppBaseService.class).getTodayDate();
	}

	
	/**
	 * Is a blocking in the future ?
	 *
	 * @return
	 */
	public boolean isBlockingInFuture(List<Blocking> blockings){
        if (blockings != null) {
            for (Blocking blocking : blockings) {
                if (blocking.getBlockingToDate().isAfter(today)) {
                    return true;
                }
            }
        }
        return false;
	}


	/**
	 * Is the partner direct debit blocked ?
	 *
	 * @return
	 */
	public boolean isDebitBlockingBlocking(Partner partner, Company company){
        return this.isBlockingInFuture(this.getBlockings(partner, company, BlockingRepository.DEBIT_BLOCKING));
	}


	/**
	 * Is the partner reimbursement blocked ?
	 *
	 * @return
	 */
	public boolean isReimbursementBlocking(Partner partner, Company company){
		return this.isBlockingInFuture(this.getBlockings(partner, company, BlockingRepository.REIMBURSEMENT_BLOCKING));
	}

}
