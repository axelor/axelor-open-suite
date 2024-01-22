/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.suppliermanagement.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.suppliermanagement.db.PurchaseOrderSupplierLine;
import com.axelor.apps.suppliermanagement.db.repo.PurchaseOrderSupplierLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class PurchaseOrderSupplierLineService {

  @Inject PurchaseOrderSupplierLineRepository poSupplierLineRepo;

  @Transactional(rollbackOn = {Exception.class})
  public void accept(PurchaseOrderSupplierLine purchaseOrderSupplierLine) throws AxelorException {

    PurchaseOrderLine purchaseOrderLine = purchaseOrderSupplierLine.getPurchaseOrderLine();

    purchaseOrderLine.setEstimatedReceiptDate(purchaseOrderSupplierLine.getEstimatedDelivDate());

    Partner supplierPartner = purchaseOrderSupplierLine.getSupplierPartner();
    Company company = purchaseOrderLine.getPurchaseOrder().getCompany();
    if (Beans.get(BlockingService.class)
            .getBlocking(supplierPartner, company, BlockingRepository.PURCHASE_BLOCKING)
        != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.SUPPLIER_BLOCKED),
          supplierPartner);
    }
    purchaseOrderLine.setSupplierPartner(supplierPartner);

    purchaseOrderLine.setPrice(purchaseOrderSupplierLine.getPrice());
    purchaseOrderLine.setExTaxTotal(
        PurchaseOrderLineServiceImpl.computeAmount(
            purchaseOrderLine.getQty(), purchaseOrderLine.getPrice()));

    purchaseOrderSupplierLine.setStateSelect(PurchaseOrderSupplierLineRepository.STATE_ACCEPTED);

    poSupplierLineRepo.save(purchaseOrderSupplierLine);
  }

  public PurchaseOrderSupplierLine create(Partner supplierPartner, BigDecimal price) {

    return new PurchaseOrderSupplierLine(
        price, PurchaseOrderSupplierLineRepository.STATE_REQUESTED, supplierPartner);
  }
}
