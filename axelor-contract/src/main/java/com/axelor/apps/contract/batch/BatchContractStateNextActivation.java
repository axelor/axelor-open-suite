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

public class BatchContractStateNextActivation extends BatchContractState {

    @Inject
    BatchContractStateNextActivation(
            ContractRepository repository, ContractService service,
            AppBaseService baseService) {
        super(repository, service, baseService);
    }

    @Override
    Query<Contract> prepare() {
        return repository.all().filter(
                "self.nextVersion.supposedActivationDate <= :date " +
                        "AND self.nextVersion.statusSelect = :status")
                .bind("date", baseService.getTodayDate()
                        .format(DateTimeFormatter.ofPattern("YYYY-MM-dd")))
                .bind("status", ContractVersionRepository.WAITING_VERSION);
    }

    @Override
    void process(Contract contract) throws AxelorException {
        service.activeNextVersion(contract, baseService.getTodayDate());
    }
}
