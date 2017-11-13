package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;

public interface ContractVersionService {

	/**
	 * Waiting version
	 *
	 * @param version
	 * @param date
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	void waiting(ContractVersion version, LocalDate date);

	/**
	 * Ongoing version
	 *
	 * @param version
	 * @param date
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	void ongoing(ContractVersion version, LocalDate date) throws AxelorException;

	/**
	 * terminate version
	 *
	 * @param version
	 * @param date
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	void terminate(ContractVersion version, LocalDate date);

}
