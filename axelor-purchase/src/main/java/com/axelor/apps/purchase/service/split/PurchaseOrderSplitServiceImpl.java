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
