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
package com.axelor.apps.contract.service;

import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;

public class ContractVersionServiceImpl extends ContractVersionRepository implements ContractVersionService {

	@Inject
	DurationService durationService;

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void waiting(ContractVersion version, LocalDate date) {
		version.setStatusSelect(WAITING_VERSION);

		save(version);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void ongoing(ContractVersion version, LocalDate date) throws AxelorException {
		if(version.getIsPeriodicInvoicing() && (version.getContract().getFirstPeriodEndDate() == null || version.getInvoicingFrequency() == null)) {
			throw new AxelorException(I18n.get("Please fill the first period end date and the invoice frequency."), IException.CONFIGURATION_ERROR);
		}

		version.setActivationDate(date);
		version.setActivatedBy(AuthUtils.getUser());
		version.setStatusSelect(ONGOING_VERSION);

		if (version.getVersion() >= 0 && version.getIsWithEngagement() && version.getEngagementStartFromVersion()) {
			Preconditions.checkNotNull(version.getContract(), I18n.get("No contract is associated to version."));
			version.getContract().setEngagementStartDate(date);
		}

		save(version);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void terminate(ContractVersion version, LocalDate date) {

		version.setEndDate(date);
		version.setStatusSelect(TERMINATED_VERSION);

		save(version);
	}

}
