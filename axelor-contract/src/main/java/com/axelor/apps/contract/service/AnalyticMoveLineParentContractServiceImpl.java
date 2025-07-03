/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
