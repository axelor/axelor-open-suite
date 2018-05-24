package com.axelor.apps.contract.batch;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.time.format.DateTimeFormatter;

public class BatchContractStateTerminate extends BatchContractState {

  @Inject
  BatchContractStateTerminate(
      ContractRepository repository, ContractService service, AppBaseService baseService) {
    super(repository, service, baseService);
  }

  @Override
  Query<Contract> prepare() {
    return repository
        .all()
        .filter(
            "(self.terminatedDate <= :date "
                + " OR self.currentVersion.supposedEndDate <= :date)"
                + " AND self.statusSelect = :status")
        .bind("date", baseService.getTodayDate().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")))
        .bind("status", ContractRepository.ACTIVE_CONTRACT);
  }

  @Override
  void process(Contract contract) throws AxelorException {
    service.terminateContract(contract, false, baseService.getTodayDate());
  }
}
