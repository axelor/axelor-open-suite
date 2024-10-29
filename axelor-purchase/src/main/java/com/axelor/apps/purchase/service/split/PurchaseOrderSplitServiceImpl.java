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
package com.axelor.apps.purchase.service.split;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PurchaseOrderSplitServiceImpl implements PurchaseOrderSplitService {

  protected PurchaseOrderService purchaseOrderService;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected PurchaseOrderLineRepository purchaseOrderLineRepository;

  @Inject
  public PurchaseOrderSplitServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepository) {
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  public PurchaseOrder separateInNewQuotation(
      PurchaseOrder purchaseOrder, ArrayList<LinkedHashMap<String, Object>> purchaseOrderLines)
      throws AxelorException {

    purchaseOrder = purchaseOrderRepository.find(purchaseOrder.getId());

    PurchaseOrder copyPurchaseOrder = purchaseOrderRepository.copy(purchaseOrder, true);
    copyPurchaseOrder.clearPurchaseOrderLineList();
    purchaseOrderRepository.save(copyPurchaseOrder);

    for (LinkedHashMap<String, Object> poLine : purchaseOrderLines) {
      if (!poLine.containsKey("selected") || !(boolean) poLine.get("selected")) {
        continue;
      }

      PurchaseOrderLine purchaseOrderLine =
          purchaseOrderLineRepository.find(Long.parseLong(poLine.get("id").toString()));
      List<PurchaseOrderLine> separatedPOLines = new ArrayList<>();
      separatedPOLines.add(purchaseOrderLine);

      for (PurchaseOrderLine separatedLine : separatedPOLines) {
        copyPurchaseOrder.addPurchaseOrderLineListItem(separatedLine);
      }
    }

    copyPurchaseOrder = purchaseOrderService.computePurchaseOrder(copyPurchaseOrder);
    purchaseOrderRepository.save(copyPurchaseOrder);

    // refresh the origin purchase order to refresh the field purchaseOrderLineList
    JPA.refresh(purchaseOrder);

    purchaseOrder = purchaseOrderService.computePurchaseOrder(purchaseOrder);
    purchaseOrderRepository.save(purchaseOrder);

    return copyPurchaseOrder;
  }
}
