package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.InvoicePeriod;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;

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

		if(version.getIsConsumptionManagement()) {
			generateConsumptionManagement(version);
		}

		generateInvoicePeriod(version, date);

		version.setActivationDate(date);
		version.setActivatedBy(AuthUtils.getUser());
		version.setStatusSelect(ONGOING_VERSION);

		save(version);
	}

	private void generateInvoicePeriod(ContractVersion version, LocalDate date) {
		InvoicePeriod period = new InvoicePeriod();
		period.setStatusSelect(1);

		// If this is the first period
		if(version.getContract().getPeriodNumber() < 1) {

			period.setStartDate(date);

			if(version.getIsPeriodicInvoicing()) {
				period.setEndDate(version.getContract().getFirstPeriodEndDate());
			}

		} else {
			InvoicePeriod lastPeriod = getLastInvoicePeriod(version);
			period.setStartDate(lastPeriod.getEndDate().plusDays(1));
			LocalDate theoriticalEndDate = computeEndDate(period.getStartDate(), version.getInvoicingFrequency());

			if(version.getSupposedEndDate() != null && (theoriticalEndDate.equals(version.getSupposedEndDate()) || theoriticalEndDate.compareTo(version.getSupposedEndDate()) > 0)) {
				period.setEndDate(version.getSupposedEndDate());
				period.setIsLastPeriod(Boolean.TRUE);
			} else {
				period.setEndDate(theoriticalEndDate);
			}

		}

		version.getContract().setCurrentInvoicePeriod(period);
	}

	private LocalDate computeEndDate(LocalDate date, Duration duration) {
		if(duration == null)  { return date; }

		switch (duration.getTypeSelect()) {
			case DurationRepository.TYPE_MONTH:
				return date.plusMonths(duration.getValue()).minusDays(1);
			case DurationRepository.TYPE_DAY:
				return date.plusDays(duration.getValue());
			default:
				return date;
		}
	}

	private InvoicePeriod getLastInvoicePeriod(ContractVersion version) {
		Collections.sort(version.getContract().getInvoicePeriodHistory(), new Comparator<InvoicePeriod>() {
			@Override
			public int compare(InvoicePeriod o1, InvoicePeriod o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});

		return version.getContract().getInvoicePeriodHistory().get(version.getContract().getInvoicePeriodHistory().size()-1);
	}

	private void generateConsumptionManagement(ContractVersion version) {
		if(ObjectUtils.isEmpty(version.getContractLineList())) { return; }

		for (ContractLine contractLine : version.getContractLineList()) {
			if(!contractLine.getIsConsumptionLine()) { continue; }

			ConsumptionLine line = new ConsumptionLine(contractLine);
			line.setQty(BigDecimal.ZERO);

			version.addConsumptionLineListItem(line);
		}
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void terminate(ContractVersion version, LocalDate date) {

		closeInvoicePeriod(version, date);

		version.setEndDate(date);
		version.setStatusSelect(TERMINATED_VERSION);

		save(version);
	}

	private void closeInvoicePeriod(ContractVersion version, LocalDate date) {
		InvoicePeriod period = version.getContract().getCurrentInvoicePeriod();
		period.setEndDate(date);

		if(version.getInvoicingMoment() == 2) {
			copyAllInPeriod(version, period);
			generateConsumptionManagement(version);
			if(version.getAutomaticInvoicing()) {
				//TODO: Generate invoice.
			}
		}

		version.getContract().addInvoicePeriodHistory(period);
		version.getContract().setCurrentInvoicePeriod(null);
		version.getContract().setPeriodNumber(version.getContract().getPeriodNumber() + 1);

	}

	private void copyAllInPeriod(ContractVersion version, InvoicePeriod period) {

		if(!ObjectUtils.isEmpty(version.getConsumptionLineList())) {
			for (ConsumptionLine consumptionLine : version.getConsumptionLineList()) {
				period.addConsumptionLineListItem(consumptionLine);
			}
			version.clearConsumptionLineList();
		}

		if(!ObjectUtils.isEmpty(version.getAdditionalBenefitList())) {
			for (ContractLine contractLine : version.getAdditionalBenefitList()) {
				period.addAdditionalBenefitListItem(contractLine);
			}
			version.clearAdditionalBenefitList();
		}
	}
}
