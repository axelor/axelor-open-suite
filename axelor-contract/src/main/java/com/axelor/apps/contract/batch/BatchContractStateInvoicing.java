package com.axelor.apps.contract.batch;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.time.format.DateTimeFormatter;

public class BatchContractStateInvoicing extends BatchContractState {

  @Inject
  BatchContractStateInvoicing(
      ContractRepository repository, ContractService service, AppBaseService baseService) {
    super(repository, service, baseService);
  }

  @Override
  public Query<Contract> prepare() {
    return repository
        .all()
        .filter(
            "self.isInvoicingManagement = TRUE AND "
                + "self.currentVersion.automaticInvoicing = TRUE AND "
                + "self.invoicingDate <= :date")
        .bind("date", baseService.getTodayDate().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
  }

  @Override
  public void process(Contract contract) throws AxelorException {
    service.invoicingContract(contract);
  }
}
