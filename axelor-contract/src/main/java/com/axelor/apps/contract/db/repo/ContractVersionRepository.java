package com.axelor.apps.contract.db.repo;

import java.util.List;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.tool.ModelTool;
import com.axelor.inject.Beans;

public class ContractVersionRepository extends AbstractContractVersionRepository {

    public ContractVersion copy(Contract contract) {
        ContractVersion newVersion = new ContractVersion();
        ContractVersion currentVersion = contract.getCurrentVersion();

        newVersion.setStatusSelect(ContractVersionRepository.DRAFT_VERSION);
        newVersion.setContractNext(contract);
        newVersion.setPaymentMode(currentVersion.getPaymentMode());
        newVersion.setPaymentCondition(currentVersion.getPaymentCondition());
        newVersion.setInvoicingFrequency(currentVersion.getInvoicingFrequency());
        newVersion.setInvoicingMoment(currentVersion.getInvoicingMoment());
        newVersion.setIsPeriodicInvoicing(currentVersion.getIsPeriodicInvoicing());
        newVersion.setAutomaticInvoicing(currentVersion.getAutomaticInvoicing());
        newVersion.setIsProratedInvoice(currentVersion.getIsProratedInvoice());
        newVersion.setIsProratedFirstInvoice(currentVersion.getIsProratedFirstInvoice());
        newVersion.setIsProratedLastInvoice(currentVersion.getIsProratedLastInvoice());
        newVersion.setDescription(currentVersion.getDescription());

        newVersion.setIsTacitRenewal(currentVersion.getIsTacitRenewal());
        newVersion.setRenewalDuration(currentVersion.getRenewalDuration());
        newVersion.setIsAutoEnableVersionOnRenew(currentVersion.getIsAutoEnableVersionOnRenew());

        newVersion.setIsWithEngagement(currentVersion.getIsWithEngagement());
        newVersion.setEngagementDuration(currentVersion.getEngagementDuration());

        newVersion.setIsWithPriorNotice(currentVersion.getIsWithPriorNotice());
        newVersion.setPriorNoticeDuration(currentVersion.getPriorNoticeDuration());

        newVersion.setEngagementStartFromVersion(currentVersion.getEngagementStartFromVersion());

        newVersion.setDoNotRenew(currentVersion.getDoNotRenew());

        ContractLineRepository repository = Beans.get(ContractLineRepository.class);
        List<ContractLine> lines= ModelTool.copy(repository, currentVersion.getContractLineList(), false);
        newVersion.setContractLineList(lines);

        newVersion.setIsTimeProratedInvoice(currentVersion.getIsTimeProratedInvoice());
        newVersion.setIsVersionProratedInvoice(currentVersion.getIsVersionProratedInvoice());

        return newVersion;
    }
}
