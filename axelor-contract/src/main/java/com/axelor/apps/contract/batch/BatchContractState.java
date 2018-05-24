package com.axelor.apps.contract.batch;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;

abstract class BatchContractState {
  ContractRepository repository;
  ContractService service;
  AppBaseService baseService;

  BatchContractState(
      ContractRepository repository, ContractService service, AppBaseService baseService) {
    this.repository = repository;
    this.service = service;
    this.baseService = baseService;
  }

  abstract Query<Contract> prepare();

  abstract void process(Contract contract) throws AxelorException;
}
