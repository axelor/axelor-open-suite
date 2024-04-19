package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ContractYearEndBonusServiceImpl implements ContractYearEndBonusService {

  protected InvoiceLinePricingService invoiceLinePricingService;

  @Inject
  public ContractYearEndBonusServiceImpl(InvoiceLinePricingService invoiceLinePricingService) {
    this.invoiceLinePricingService = invoiceLinePricingService;
  }

  @Override
  public void invoiceYebContract(Contract contract, Invoice invoice) throws AxelorException {
    if (!isYerContract(contract)) {
      return;
    }

    invoiceLinePricingService.computePricing(invoice);
  }

  protected boolean isYerContract(Contract contract) {
    int targetTypeSelect = contract.getTargetTypeSelect();
    List<Integer> yerTypes = new ArrayList<>();
    yerTypes.add(ContractRepository.YEB_CUSTOMER_CONTRACT);
    yerTypes.add(ContractRepository.YEB_SUPPLIER_CONTRACT);
    return yerTypes.contains(targetTypeSelect);
  }
}
