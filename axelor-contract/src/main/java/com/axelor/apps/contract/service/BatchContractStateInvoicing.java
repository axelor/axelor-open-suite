package com.axelor.apps.contract.service;

import java.time.format.DateTimeFormatter;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class BatchContractStateInvoicing implements BatchContractState {
    protected ContractRepository repository;
    protected ContractService service;

    @Inject
    public BatchContractStateInvoicing(
            ContractRepository repository, ContractService service) {
        this.repository = repository;
        this.service = service;
    }

    @Override
    public Query<Contract> prepare() {
        return repository.all().filter("self.isInvoicingManagement = TRUE AND " +
                "self.currentVersion.automaticInvoicing = TRUE AND " +
                "self.invoicingDate <= :date")
                .bind("date", Beans.get(AppBaseService.class)
                        .getTodayDate()
                        .format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    }

    @Override
    public Contract process(Contract contract) throws Exception {
        service.invoicingContract(contract);
        return contract;
    }
}
