package com.axelor.apps.contract.supplychain.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.contract.db.Contract;
import com.axelor.exception.AxelorException;

public abstract class InvoiceGeneratorContract extends InvoiceGenerator {
	
	protected Contract contract;

	protected InvoiceGeneratorContract(Contract contract) throws AxelorException {

		super(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, contract.getCompany(), contract.getPartner(), null, null, contract.getContractId(), null, null);
		this.contract = contract;
		this.currency = contract.getCurrency();
		this.paymentCondition = contract.getCurrentVersion().getPaymentCondition();
		this.paymentMode = contract.getCurrentVersion().getPaymentMode();
	}

	

}
