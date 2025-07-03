package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.analytic.AnalyticMoveLineParentSupplychainServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class AnalyticMoveLineParentContractServiceImpl
    extends AnalyticMoveLineParentSupplychainServiceImpl {

  protected ContractLineRepository contractLineRepository;

  @Inject
  public AnalyticMoveLineParentContractServiceImpl(
      AnalyticLineService analyticLineService,
      MoveLineRepository moveLineRepository,
      InvoiceLineRepository invoiceLineRepository,
      MoveLineMassEntryRepository moveLineMassEntryRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      ContractLineRepository contractLineRepository) {
    super(
        analyticLineService,
        moveLineRepository,
        invoiceLineRepository,
        moveLineMassEntryRepository,
        purchaseOrderLineRepository,
        saleOrderLineRepository);
    this.contractLineRepository = contractLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refreshAxisOnParent(AnalyticMoveLine analyticMoveLine) throws AxelorException {
    ContractLine contractLine = analyticMoveLine.getContractLine();
    if (contractLine != null) {
      Contract contract =
          Optional.of(contractLine)
              .map(ContractLine::getContractVersion)
              .map(ContractVersion::getContract)
              .orElse(null);
      AnalyticLineModel analyticLineModel =
          new AnalyticLineContractModel(contractLine, contractLine.getContractVersion(), contract);
      analyticLineService.setAnalyticAccount(
          analyticLineModel,
          Optional.of(contractLine)
              .map(ContractLine::getContractVersion)
              .map(ContractVersion::getContract)
              .map(Contract::getCompany)
              .orElse(null));
      contractLineRepository.save(contractLine);
    } else {
      super.refreshAxisOnParent(analyticMoveLine);
    }
  }
}
