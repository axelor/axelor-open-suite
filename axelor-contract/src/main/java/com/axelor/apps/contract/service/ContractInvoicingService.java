package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;

public interface ContractInvoicingService {

  default List<ContractVersion> getVersions(Contract contract) {
    List<ContractVersion> versions = contract.getVersionHistory();
    if (versions == null) {
      versions = new ArrayList<>();
    }
    if (contract.getCurrentContractVersion() != null) {
      versions.add(contract.getCurrentContractVersion());
    }
    return versions;
  }

  default boolean isFullProrated(Contract contract) {
    return contract.getCurrentContractVersion() != null
        && (contract.getCurrentContractVersion().getIsTimeProratedInvoice()
            && contract.getCurrentContractVersion().getIsVersionProratedInvoice());
  }

  /**
   * Invoicing the contract
   *
   * @param contract
   * @throws AxelorException
   */
  Invoice invoicingContract(Contract contract) throws AxelorException;

  Invoice invoicingContracts(List<Contract> contractList) throws AxelorException;

  Contract increaseInvoiceDates(Contract contract);
  /**
   * Take each consumption line and convert it to contract line if a associate consumption contract
   * line is present in contract.
   *
   * @param contract contain consumption and contract lines.
   * @return Multimap of consumption lines successfully converted to contract lines.
   */
  Multimap<ContractLine, ConsumptionLine> mergeConsumptionLines(Contract contract)
      throws AxelorException;

  void fillInvoicingDateByInvoicingMoment(Contract contract);
}
