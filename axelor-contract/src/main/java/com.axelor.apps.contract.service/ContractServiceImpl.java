package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;
import java.util.List;

public class ContractServiceImpl extends ContractRepository implements ContractService {

	@Inject
	protected ContractVersionService versionService;

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void activeContract(Contract contract, LocalDate date) {
		contract.setStartDate(date);
		contract.setStatusSelect(ACTIVE_CONTRACT);

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void waitingCurrentVersion(Contract contract, LocalDate date) {
		ContractVersion currentVersion = contract.getCurrentVersion();
		versionService.waiting(currentVersion, date);

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void ongoingCurrentVersion(Contract contract, LocalDate date) throws AxelorException {
		ContractVersion currentVersion = contract.getCurrentVersion();

		// Active the contract if not yet activated
		if(contract.getStatusSelect() != ContractRepository.ACTIVE_CONTRACT) {
			Beans.get(ContractService.class).activeContract(contract, date);
		}

		// Ongoing current version
		versionService.ongoing(currentVersion, date);

		// Inc contract version number
		contract.setVersionNumber(contract.getVersionNumber() + 1);

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void waitingNextVersion(Contract contract, LocalDate date) {
		ContractVersion version = contract.getNextVersion();
		versionService.waiting(version, date);

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void activeNextVersion(Contract contract, LocalDate date) throws AxelorException {
		ContractVersion currentVersion = contract.getCurrentVersion();

		// Terminate currentVersion
		versionService.terminate(currentVersion, date.minusDays(1));

		// Archive current version
		Beans.get(ContractService.class).archiveVersion(contract, date);

		if(contract.getCurrentVersion().getDoNotRenew()) {
			contract.setIsTacitRenewal(false);
		}

		// Ongoing current version
		Beans.get(ContractService.class).ongoingCurrentVersion(contract, date);

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void archiveVersion(Contract contract, LocalDate date) {
		ContractVersion currentVersion = contract.getCurrentVersion();
		ContractVersion nextVersion = contract.getNextVersion();

		contract.addVersionHistory(currentVersion);
		currentVersion.setContract(null);

		contract.setCurrentVersion(nextVersion);
		nextVersion.setContractNext(null);
		nextVersion.setContract(contract);

		contract.setNextVersion(null);

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void terminateContract(Contract contract, Boolean isManual, LocalDate date) {
		ContractVersion currentVersion = contract.getCurrentVersion();

		if(isManual) {
			contract.setTerminatedManually(isManual);
			contract.setTerminatedDate(date);
			contract.setTerminatedBy(AuthUtils.getUser());
		}

		contract.getCurrentInvoicePeriod().setIsLastPeriod(Boolean.TRUE);

		versionService.terminate(currentVersion, date);

		contract.setEndDate(date);
		contract.setStatusSelect(CLOSED_CONTRACT);

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void invoicingContract(Contract contract) {

	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void renewContract(Contract contract, LocalDate date) throws AxelorException {
		ContractVersion currentVersion = contract.getCurrentVersion();
		ContractVersion nextVersion = Beans.get(ContractVersionRepository.class).copy(currentVersion, true);

		// Terminate currentVersion
		versionService.terminate(currentVersion, date.minusDays(1));

		// Archive current version
		contract.addVersionHistory(currentVersion);
		currentVersion.setContract(null);

		// Set new version
		contract.setCurrentVersion(nextVersion);
		nextVersion.setContractNext(null);
		nextVersion.setContract(contract);

		// Ongoing current version : Don't call contract ongoingCurrentVersion because
		// in case of renewal, we don't need to inc contract version number.
		versionService.ongoing(currentVersion, date);

		contract.setLastRenewalDate(date);
		contract.setRenewalNumber(contract.getRenewalNumber() + 1);

		save(contract);
	}

	public List<Contract> getContractToTerminate(LocalDate date) {
		return all().filter("self.statusSelect = ?1 AND self.currentVersion.statusSelect = ?2 AND self.isTacitRenewal IS FALSE " +
						"AND (self.toClosed IS TRUE OR self.currentVersion.supposedEndDate >= ?3)",
				ACTIVE_CONTRACT, ContractVersionRepository.ONGOING_VERSION, date).fetch();
	}

	public List<Contract> getContractToRenew(LocalDate date) {
		return all().filter("self.statusSelect = ?1 AND self.isTacitRenewal IS TRUE AND self.toClosed IS FALSE " +
						"AND self.currentVersion.statusSelect = ?2 AND self.currentVersion.supposedEndDate >= ?3",
				ACTIVE_CONTRACT, ContractVersionRepository.ONGOING_VERSION, date).fetch();
	}

}
