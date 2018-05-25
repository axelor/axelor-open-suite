package com.axelor.apps.contract.batch;

import java.time.format.DateTimeFormatter;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BatchContractStateCurrentActivation extends BatchContractState {

    @Inject
    BatchContractStateCurrentActivation(
            ContractRepository repository, ContractService service,
            AppBaseService baseService) {
        super(repository, service, baseService);
    }

    @Override
    Query<Contract> prepare() {
        return repository.all().filter(
                "self.currentVersion.supposedActivationDate <= :date " +
                        "AND self.currentVersion.statusSelect = :status")
                .bind("date", baseService.getTodayDate()
                        .format(DateTimeFormatter.ofPattern("YYYY-MM-dd")))
                .bind("status", ContractVersionRepository.WAITING_VERSION);
    }

    @Override
    void process(Contract contract) throws AxelorException {
        service.ongoingCurrentVersion(contract, baseService.getTodayDate());
    }
}
