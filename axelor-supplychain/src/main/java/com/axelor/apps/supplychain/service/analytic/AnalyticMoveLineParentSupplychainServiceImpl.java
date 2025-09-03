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
package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineParentServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class AnalyticMoveLineParentSupplychainServiceImpl
    extends AnalyticMoveLineParentServiceImpl {

  protected PurchaseOrderLineRepository purchaseOrderLineRepository;
  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public AnalyticMoveLineParentSupplychainServiceImpl(
      AnalyticLineService analyticLineService,
      MoveLineRepository moveLineRepository,
      InvoiceLineRepository invoiceLineRepository,
      MoveLineMassEntryRepository moveLineMassEntryRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      SaleOrderLineRepository saleOrderLineRepository) {
    super(
        analyticLineService,
        moveLineRepository,
        invoiceLineRepository,
        moveLineMassEntryRepository);
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refreshAxisOnParent(AnalyticMoveLine analyticMoveLine) throws AxelorException {
    PurchaseOrderLine purchaseOrderLine = analyticMoveLine.getPurchaseOrderLine();
    SaleOrderLine saleOrderLine = analyticMoveLine.getSaleOrderLine();
    AnalyticLineModel analyticLineModel = null;
    if (purchaseOrderLine != null) {
      analyticLineModel =
          new AnalyticLineModel(purchaseOrderLine, purchaseOrderLine.getPurchaseOrder());
      analyticLineService.setAnalyticAccount(
          analyticLineModel,
          Optional.of(purchaseOrderLine)
              .map(PurchaseOrderLine::getPurchaseOrder)
              .map(PurchaseOrder::getCompany)
              .orElse(null));
      purchaseOrderLineRepository.save(purchaseOrderLine);
    } else if (saleOrderLine != null) {
      analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrderLine.getSaleOrder());
      analyticLineService.setAnalyticAccount(
          analyticLineModel,
          Optional.of(saleOrderLine)
              .map(SaleOrderLine::getSaleOrder)
              .map(SaleOrder::getCompany)
              .orElse(null));
      saleOrderLineRepository.save(saleOrderLine);
    } else {
      super.refreshAxisOnParent(analyticMoveLine);
    }
  }
}
