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

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.FiscalPositionServiceAccountImpl;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.IExceptionMessage;
import com.axelor.apps.contract.generator.InvoiceGeneratorContract;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContractServiceImpl extends ContractRepository
		implements ContractService {

	private static final Logger LOG = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass() );

	protected AppBaseService appBaseService;
	protected ContractVersionService versionService;
	protected ContractLineService contractLineService;
	protected DurationService durationService;

	protected ContractLineRepository contractLineRepo;
	protected ConsumptionLineRepository consumptionLineRepo;

	@Inject
	public ContractServiceImpl(AppBaseService appBaseService,
            ContractVersionService versionService,
            ContractLineService contractLineService,
            DurationService durationService,
            ContractLineRepository contractLineRepo,
            ConsumptionLineRepository consumptionLineRepo) {
	    this.appBaseService = appBaseService;
		this.versionService = versionService;
		this.contractLineService = contractLineService;
		this.durationService = durationService;
		this.contractLineRepo = contractLineRepo;
		this.consumptionLineRepo = consumptionLineRepo;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void activeContract(Contract contract, LocalDate date) {
		contract.setStartDate(date);
		contract.setStatusSelect(ACTIVE_CONTRACT);

		save(contract);
	}

	@Override
	public void waitingCurrentVersion(Contract contract, LocalDate date) {
		ContractVersion currentVersion = contract.getCurrentVersion();
		versionService.waiting(currentVersion, date);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice ongoingCurrentVersion(Contract contract, LocalDate date)
			throws AxelorException {
		ContractVersion currentVersion = contract.getCurrentVersion();
		
		Invoice invoice = null;

		if (currentVersion.getIsWithEngagement() &&
				contract.getStatusSelect() != ContractRepository.ACTIVE_CONTRACT
				|| currentVersion.getEngagementStartFromVersion()) {
			contract.setEngagementStartDate(date);
		}

		if(contract.getStatusSelect() != ContractRepository.ACTIVE_CONTRACT) {
			activeContract(contract, date);
		}
		
		versionService.ongoing(currentVersion, date);

		contract.setVersionNumber(contract.getVersionNumber() + 1);
		if (currentVersion.getIsPeriodicInvoicing()
				&& contract.getVersionNumber() == 0) {
			contract.setInvoicePeriodStartDate(
					currentVersion.getActivationDate());
			contract.setInvoicePeriodEndDate(
					contract.getFirstPeriodEndDate());
		}
		if (contract.getCurrentVersion().getAutomaticInvoicing()) {
			invoice = invoicingContract(contract);
		}

		return invoice;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Contract increaseInvoiceDates(Contract contract) {
		ContractVersion version = contract.getCurrentVersion();
		if (version.getIsPeriodicInvoicing()) {
			contract.setInvoicePeriodStartDate(contract
					.getInvoicePeriodEndDate().plusDays(1));
			contract.setInvoicePeriodEndDate(
					durationService.computeDuration(
							version.getInvoicingFrequency(),
							contract.getInvoicePeriodStartDate())
                    .minusDays(1));
		}

		if (version.getAutomaticInvoicing()) {
			switch (version.getInvoicingMoment()) {
				case 1:
					contract.setInvoicingDate(
							contract.getInvoicePeriodEndDate());
					break;
				case 2:
					contract.setInvoicingDate(
							contract.getInvoicePeriodStartDate());
					break;
				default:
					contract.setInvoicingDate(
							appBaseService.getTodayDate());
			}
		}
		return contract;
	}

	@Override
	public void isValid(Contract contract) throws AxelorException {
		if (contract.getId() == null) {
			return;
		}
		checkInvoicedConsumptionLines(contract);
		checkInvoicedAdditionalContractLine(contract);
	}

	protected void checkInvoicedConsumptionLines(Contract contract)
			throws AxelorException {
		Contract origin = find(contract.getId());
		List<ConsumptionLine> lineInvoiced = origin
				.getConsumptionLineList()
				.stream()
				.filter(ConsumptionLine::getIsInvoiced)
				.collect(Collectors.toList());
		for (ConsumptionLine line : contract.getConsumptionLineList()) {
			if (lineInvoiced.contains(line)) {
				lineInvoiced.remove(line);
			}
		}
		if (!lineInvoiced.isEmpty()) {
			throw new AxelorException(IException.FUNCTIONNAL,
					I18n.get(IExceptionMessage
							.CONTRACT_CANT_REMOVE_INVOICED_LINE));
		}
	}

	protected void checkInvoicedAdditionalContractLine(Contract contract)
			throws AxelorException {
		Contract origin = find(contract.getId());
		List<ContractLine> lineInvoiced = origin
				.getAdditionalBenefitContractLineList()
				.stream()
				.filter(ContractLine::getIsInvoiced)
				.collect(Collectors.toList());
		for (ContractLine line
				: contract.getAdditionalBenefitContractLineList()) {
			if (lineInvoiced.contains(line)) {
				lineInvoiced.remove(line);
			}
		}
		if (!lineInvoiced.isEmpty()) {
			throw new AxelorException(IException.FUNCTIONNAL,
					I18n.get(IExceptionMessage
							.CONTRACT_CANT_REMOVE_INVOICED_LINE));
		}
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
	public void activeNextVersion(Contract contract, LocalDate date)
			throws AxelorException {
		ContractVersion currentVersion = contract.getCurrentVersion();

		// Terminate currentVersion
		versionService.terminate(currentVersion, date.minusDays(1));

		// Archive current version
		archiveVersion(contract, date);

		if(contract.getCurrentVersion().getDoNotRenew()) {
			contract.getCurrentVersion().setIsTacitRenewal(false);
		}

		// Ongoing current version
		ongoingCurrentVersion(contract, date);

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
	public void checkCanTerminateContract(Contract contract)
			throws AxelorException {
		if (contract.getTerminatedDate() == null) {
			throw new AxelorException(IException.MISSING_FIELD,
					I18n.get(IExceptionMessage
							.CONTRACT_MISSING_TERMINATE_DATE));
		}
		ContractVersion version = contract.getCurrentVersion();

		if (contract.getTerminatedDate().isBefore(version.getActivationDate())){
			throw new AxelorException(IException.FUNCTIONNAL,
					I18n.get(IExceptionMessage
							.CONTRACT_UNVALIDE_TERMINATE_DATE));
		}

		if (version.getIsWithEngagement()) {
			if (contract.getEngagementStartDate() == null) {
				throw new AxelorException(IException.MISSING_FIELD,
						I18n.get(IExceptionMessage
								.CONTRACT_MISSING_ENGAGEMENT_DATE));
			}
			if (contract.getTerminatedDate().isBefore(
					durationService.computeDuration(
							version.getEngagementDuration(),
							contract.getEngagementStartDate()))) {
				throw new AxelorException(IException.FUNCTIONNAL,
						I18n.get(IExceptionMessage
								.CONTRACT_ENGAGEMENT_DURATION_NOT_RESPECTED));
			}
		}

		if (version.getIsWithPriorNotice() &&
				contract.getTerminatedDate().isBefore(
						durationService.computeDuration(
								version.getPriorNoticeDuration(),
								Beans.get(AppBaseService.class).getTodayDate())
				)) {
			throw new AxelorException(IException.FUNCTIONNAL,
					I18n.get(IExceptionMessage
							.CONTRACT_PRIOR_DURATION_NOT_RESPECTED));
		}
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void terminateContract(Contract contract, Boolean isManual,
				LocalDate date) throws AxelorException {
		ContractVersion currentVersion = contract.getCurrentVersion();

		if (isManual) {
			contract.setTerminationDemandDate(appBaseService.getTodayDate());
			contract.setTerminatedManually(true);
			contract.setTerminatedDate(date);
			contract.setTerminatedBy(AuthUtils.getUser());
		} else {
			if (currentVersion.getIsTacitRenewal()
					&& !currentVersion.getDoNotRenew()) {
				renewContract(contract, date);
				return;
			}
		}

		if (contract.getTerminatedDate().isBefore(appBaseService.getTodayDate())
				|| contract.getTerminatedDate().equals(
						appBaseService.getTodayDate())) {
			versionService.terminate(currentVersion, date);
			contract.setEndDate(date);
			contract.setStatusSelect(CLOSED_CONTRACT);
		} else {
			contract.setEndDate(date);
		}

		save(contract);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice invoicingContract(Contract contract) throws AxelorException {
        InvoiceGenerator invoiceGenerator = new InvoiceGeneratorContract(contract);
        Invoice invoice = invoiceGenerator.generate();
        InvoiceRepository invoiceRepository = Beans.get(InvoiceRepository.class);
        invoiceRepository.save(invoice);

        // Compute all additional lines
		List<ContractLine> additionalLines = contract
				.getAdditionalBenefitContractLineList()
				.stream()
				.filter(contractLine -> !contractLine.getIsInvoiced())
				.peek(contractLine -> contractLine.setIsInvoiced(true))
				.collect(Collectors.toList());
		for (ContractLine line: additionalLines) {
			generate(invoice, line);
            contractLineRepo.save(line);
		}


		// Compute all classic contract lines
		for (ContractVersion version: getVersions(contract)) {
			BigDecimal ratio = BigDecimal.ONE;
			if (isFullProrated(contract)) {
				if (!DateTool.isProrata(contract.getInvoicePeriodStartDate(),
						contract.getInvoicePeriodEndDate(),
						version.getActivationDate(),
						version.getEndDate())) {
					continue;
				}
				LocalDate start = version.getActivationDate()
						.isBefore(contract.getInvoicePeriodStartDate())
						? contract.getInvoicePeriodStartDate()
						: version.getActivationDate();
				LocalDate end = version.getEndDate() == null
						|| (version.getEndDate() != null
						&& contract.getInvoicePeriodEndDate()
						.isBefore(version.getEndDate()))
						? contract.getInvoicePeriodEndDate()
						: version.getEndDate();
				ratio = durationService.computeRatio(start, end,
						contract.getCurrentVersion().getInvoicingFrequency());
			}
			List<ContractLine> lines = version.getContractLineList().stream()
					.filter(contractLine -> !contractLine.getIsConsumptionLine())
					.collect(Collectors.toList());

			for (ContractLine line: lines) {
				ContractLine tmp = contractLineRepo.copy(line, false);
				tmp.setPrice(line.getPrice().multiply(ratio));
				tmp = this.contractLineService.computeTotal(tmp);
				generate(invoice, tmp);
			}
		}

		// Compute all consumption lines
		Multimap<ContractLine, ConsumptionLine> consLines
				= mergeConsumptionLines(contract);
		for (Entry<ContractLine, Collection<ConsumptionLine>> entries
				: consLines.asMap().entrySet()) {
			ContractLine line = entries.getKey();
			InvoiceLine invoiceLine = generate(invoice, line);
			entries.getValue().stream()
					.peek(cons -> cons.setInvoiceLine(invoiceLine))
					.forEach(cons -> cons.setIsInvoiced(true));
			line.setQty(BigDecimal.ZERO);
			contractLineService.computeTotal(line);
		}

		// Compute invoice
		if (invoice.getInvoiceLineList() != null
				&& !invoice.getInvoiceLineList().isEmpty()){
			Beans.get(InvoiceServiceImpl.class).compute(invoice);
		}

		// Increase invoice period date
		increaseInvoiceDates(contract);

		return invoiceRepository.save(invoice);
	}

	@Override
	public Multimap<ContractLine, ConsumptionLine> mergeConsumptionLines
			(Contract contract) {
		Multimap<ContractLine, ConsumptionLine> mergedLines = HashMultimap
				.create();

		Stream<ConsumptionLine> lineStream = contract.getConsumptionLineList()
				.stream()
				.filter(c -> !c.getIsInvoiced());

		if (contract.getCurrentVersion().getIsConsumptionBeforeEndDate()) {
			lineStream = lineStream.filter(line -> line.getLineDate().isBefore(
					contract.getInvoicePeriodEndDate()));
		}

		lineStream.forEach(line -> {
			ContractVersion version = contract.getCurrentVersion();

			if (isFullProrated(contract)) {
				version = versionService.getContractVersion(contract,
						line.getLineDate());
			}

			if (version == null) {
				line.setIsError(true);
			} else {
				ContractLine matchLine = contractLineRepo.findOneBy(version,
						line.getProduct(), line.getReference(), true);
				if (matchLine == null) {
					line.setIsError(true);
				} else {
					matchLine.setQty(matchLine.getQty().add(line.getQty()));
					contractLineService.computeTotal(matchLine);
					line.setIsError(false);
					line.setContractLine(matchLine);
					mergedLines.put(matchLine, line);
				}
			}
		});
		return mergedLines;
	}

	public InvoiceLine generate(Invoice invoice, ContractLine line) throws AxelorException {
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(
				invoice, line.getProduct(), line.getProductName(),
				line.getPrice(), null, line.getDescription(),
				line.getQty(), line.getUnit(), line.getTaxLine(),
				InvoiceLineGenerator.DEFAULT_SEQUENCE, BigDecimal.ZERO,
				PriceListLineRepository.AMOUNT_TYPE_NONE, line.getExTaxTotal(),
				line.getInTaxTotal(), false) {
			@Override
			public List<InvoiceLine> creates() throws AxelorException {
				InvoiceLine invoiceLine = this.createInvoiceLine();

				List<InvoiceLine> invoiceLines = new ArrayList<>();
				invoiceLines.add(invoiceLine);
				return invoiceLines;
			}
		};

		InvoiceLine invoiceLine = invoiceLineGenerator.creates().get(0);

		FiscalPositionServiceAccountImpl fiscalPositionService
				= Beans.get(FiscalPositionServiceAccountImpl.class);
		FiscalPosition fiscalPosition = line.getFiscalPosition();
		Account currentAccount = invoiceLine.getAccount();
		Account replacedAccount = fiscalPositionService
				.getAccount(fiscalPosition, currentAccount);

		boolean isPurchase = Beans.get(InvoiceLineService.class)
				.isPurchase(invoice);

		TaxLine taxLine = Beans.get(AccountManagementService.class)
				.getTaxLine(appBaseService.getTodayDate(),
						invoiceLine.getProduct(), invoice.getCompany(),
						fiscalPosition, isPurchase);

		invoiceLine.setTaxLine(taxLine);
		invoiceLine.setAccount(replacedAccount);

		invoice.addInvoiceLineListItem(invoiceLine);

		return Beans.get(InvoiceLineRepository.class).save(invoiceLine);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void renewContract(Contract contract, LocalDate date) throws AxelorException {
		
		ContractVersion currentVersion = contract.getCurrentVersion();
		ContractVersion nextVersion = Beans.get(ContractVersionRepository.class).copy(currentVersion, true);

		versionService.terminate(currentVersion, date.minusDays(1));

		contract.addVersionHistory(currentVersion);
		currentVersion.setContract(null);
		
		contract.setCurrentVersion(nextVersion);
		nextVersion.setContractNext(null);
		nextVersion.setContract(contract);
		if (nextVersion.getIsTacitRenewal()) {
			nextVersion.setSupposedEndDate(durationService.computeDuration(nextVersion.getRenewalDuration(), date));
		}
		if (nextVersion.getIsAutoEnableVersionOnRenew()) {
			versionService.ongoing(nextVersion, date);
		} else {
			versionService.waiting(nextVersion, date);
		}

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
	

	// TODO: Move to ContractTemplateService
	@Transactional
	public Contract createContractFromTemplate(ContractTemplate template){
		
		Contract contract = new Contract();
		
		if (template.getAdditionalBenefitList() != null && !template.getAdditionalBenefitList().isEmpty()){
			
			for (ContractLine line : template.getAdditionalBenefitList()) {
				
				ContractLine newLine = contractLineRepo.copy(line, false);
				contractLineRepo.save(newLine);
				contract.addAdditionalBenefitContractLineListItem(newLine);
			}
		}
		
		contract.setCompany( template.getCompany() );
		contract.setIsAdditionaBenefitManagement(template.getIsAdditionaBenefitManagement());
		contract.setIsConsumptionManagement(template.getIsConsumptionManagement());
		contract.setIsInvoicingManagement(template.getIsInvoicingManagement());
		contract.setName(template.getName());
		contract.setNote(template.getNote());
		
		
		ContractVersion version = new ContractVersion();
		
		if (template.getContractLineList() != null && !template.getContractLineList().isEmpty()){
			
			for (ContractLine line : template.getContractLineList()) {
				
				ContractLine newLine = contractLineRepo.copy(line, false);
				contractLineRepo.save(newLine);
				version.addContractLineListItem(newLine);
			}
		}
		
		version.setIsConsumptionBeforeEndDate(template.getIsConsumptionBeforeEndDate());
		version.setIsPeriodicInvoicing(template.getIsPeriodicInvoicing());
		version.setIsProratedFirstInvoice(template.getIsProratedFirstInvoice());
		version.setIsProratedInvoice(template.getIsProratedInvoice());
		version.setIsProratedLastInvoice(template.getIsProratedLastInvoice());
		version.setIsTacitRenewal(template.getIsTacitRenewal());
		version.setIsTimeProratedInvoice(template.getIsTimeProratedInvoice());
		version.setIsVersionProratedInvoice(template.getIsVersionProratedInvoice());
		version.setIsWithEngagement(template.getIsWithEngagement());
		version.setIsWithPriorNotice(template.getIsWithPriorNotice());
		
		version.setAutomaticInvoicing(template.getAutomaticInvoicing());
		version.setEngagementDuration(template.getEngagementDuration());
		version.setEngagementStartFromVersion(template.getEngagementStartFromVersion());
		version.setInvoicingFrequency(template.getInvoicingFrequency());
		version.setInvoicingMoment(template.getInvoicingMoment());
		version.setPaymentCondition(template.getPaymentCondition());
		version.setPaymentMode(template.getPaymentMode());
		version.setPriorNoticeDuration(template.getPriorNoticeDuration());
		version.setRenewalDuration(template.getRenewalDuration());
		version.setDescription(template.getDescription());
		
		contract.setCurrentVersion(version);
		
		return save(contract);
	}

	public boolean isInVersion(ContractLine contractLine, ContractVersion version){
		
		for (ContractLine line : version.getContractLineList()) {
			
			if (line.getId().compareTo( contractLine.getId() ) == 0){
				return true;
			}
		}
		return false;
	}

	@Override
	public List<ContractVersion> getVersions(Contract contract) {
		if (contract.getCurrentVersion() == null
				|| isFullProrated(contract)) {
			return ContractService.super.getVersions(contract);
		} else {
			return Collections.singletonList(contract.getCurrentVersion());
		}
	}

}
