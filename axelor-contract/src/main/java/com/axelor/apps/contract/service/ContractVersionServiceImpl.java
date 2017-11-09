package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;

public class ContractVersionServiceImpl extends ContractVersionRepository implements ContractVersionService {

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
			throw new AxelorException("Please fill the first period end date and the invoice frequency.", IException.CONFIGURATION_ERROR);
		}

		version.setActivationDate(date);
		version.setActivatedBy(AuthUtils.getUser());
		version.setStatusSelect(ONGOING_VERSION);

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
