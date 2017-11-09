package com.axelor.apps.contract.web;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import java.util.ArrayList;
import java.util.List;

public class ContractVersionController {

	public void newVersion(ActionRequest request, ActionResponse response) {

		Contract contract = Beans.get(ContractRepository.class).find(Long.valueOf(request.getContext().get("_xContractId").toString()));

		response.setValue("statusSelect", ContractVersionRepository.DRAFT_VERSION);
		response.setValue("contractNext", contract);
		response.setValue("paymentMode", contract.getCurrentVersion().getPaymentMode());
		response.setValue("paymentCondition", contract.getCurrentVersion().getPaymentCondition());
		response.setValue("invoicingFrequency", contract.getCurrentVersion().getInvoicingFrequency());

		response.setValue("invoicingMoment", contract.getCurrentVersion().getInvoicingMoment());
		//response.setValue("isConsumptionManagement", contract.getIsConsumptionManagement());
		//response.setValue("isAdditionaBenefitManagement", contract.getIsAdditionaBenefitManagement());
		response.setValue("isPeriodicInvoicing", contract.getCurrentVersion().getIsPeriodicInvoicing());
		response.setValue("automaticInvoicing", contract.getCurrentVersion().getAutomaticInvoicing());
		response.setValue("isProratedInvoice", contract.getCurrentVersion().getIsProratedInvoice());
		response.setValue("isProratedFirstInvoice", contract.getCurrentVersion().getIsProratedFirstInvoice());
		response.setValue("isProratedLastInvoice", contract.getCurrentVersion().getIsProratedLastInvoice());
		response.setValue("contractLineList", copyContractLineList(contract.getCurrentVersion().getContractLineList()));

	}

	private List<ContractLine> copyContractLineList(List<ContractLine> contractLineList) {
		List<ContractLine> list = new ArrayList<>();
		if(ObjectUtils.isEmpty(contractLineList)) { return list; }

		ContractLineRepository clRepo = Beans.get(ContractLineRepository.class);
		for (ContractLine contractLine : contractLineList) {
			list.add(clRepo.copy(contractLine, true));
		}

		return list;
	}

}
