package com.axelor.apps.purchase.service.purchase.request;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PurchaseRequestToPoCreateServiceImpl implements PurchaseRequestToPoCreateService {

  protected final PurchaseOrderService purchaseOrderService;
  protected final PurchaseOrderCreateService purchaseOrderCreateService;
  protected final PurchaseOrderLineService purchaseOrderLineService;
  protected final PurchaseOrderRepository purchaseOrderRepo;
  protected final PurchaseRequestRepository purchaseRequestRepo;
  protected final AppBaseService appBaseService;

  @Inject
  public PurchaseRequestToPoCreateServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      PurchaseRequestRepository purchaseRequestRepo,
      AppBaseService appBaseService) {
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderCreateService = purchaseOrderCreateService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.purchaseRequestRepo = purchaseRequestRepo;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseRequestToPoGenerationResult createFromRequests(
      List<PurchaseRequest> purchaseRequests, Boolean groupBySupplier, Boolean groupByProduct)
      throws AxelorException {

    final Map<String, PurchaseOrder> poMap = new HashMap<>();
    List<String> alreadyLinkedRequests = new ArrayList<>();
    List<String> notAcceptedRequests = new ArrayList<>();

    for (PurchaseRequest purchaseRequest : purchaseRequests) {
      if (purchaseRequest.getPurchaseOrder() != null) {
        alreadyLinkedRequests.add(getPurchaseRequestReference(purchaseRequest));
        continue;
      }

      if (purchaseRequest.getStatusSelect() != PurchaseRequestRepository.STATUS_ACCEPTED) {
        notAcceptedRequests.add(getPurchaseRequestReference(purchaseRequest));
        continue;
      }

      String key =
          groupBySupplier
              ? getGroupBySupplierKey(purchaseRequest)
              : purchaseRequest.getId().toString();
      PurchaseOrder po = poMap.get(key);
      if (po == null) {
        po = createPurchaseOrder(purchaseRequest);
        poMap.put(key, po);
      }

      generatePoLinesPurchaseRequest(purchaseRequest, po, groupByProduct);

      purchaseRequest.setPurchaseOrder(po);
      purchaseRequestRepo.save(purchaseRequest);
    }

    for (PurchaseOrder po : poMap.values()) {
      purchaseOrderService.computePurchaseOrder(po);
      purchaseOrderRepo.save(po);
    }

    return new PurchaseRequestToPoGenerationResult(
        poMap.values().stream().collect(Collectors.toList()),
        buildGenerationWarnings(alreadyLinkedRequests, notAcceptedRequests));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder createFromRequest(PurchaseRequest pr) throws AxelorException {
    PurchaseRequestToPoGenerationResult result = createFromRequests(List.of(pr), false, false);

    if (result.hasWarnings()) {
      throw new AxelorException(TraceBackRepository.CATEGORY_NO_VALUE, result.getWarningMessage());
    }

    List<PurchaseOrder> out = result.getPurchaseOrders();
    return out.isEmpty() ? null : out.get(0);
  }

  protected String getPurchaseRequestReference(PurchaseRequest purchaseRequest) {
    return StringUtils.isBlank(purchaseRequest.getPurchaseRequestSeq())
        ? purchaseRequest.getId().toString()
        : purchaseRequest.getPurchaseRequestSeq();
  }

  protected List<String> buildGenerationWarnings(
      List<String> alreadyLinkedRequests, List<String> notAcceptedRequests) {
    List<String> messages = new ArrayList<>();
    if (!alreadyLinkedRequests.isEmpty()) {
      messages.add(
          String.format(
              I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_PO_ALREADY_LINKED),
              String.join(", ", alreadyLinkedRequests)));
    }
    if (!notAcceptedRequests.isEmpty()) {
      messages.add(
          String.format(
              I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_NOT_ACCEPTED_FOR_PO),
              String.join(", ", notAcceptedRequests)));
    }
    return messages;
  }

  protected String getGroupBySupplierKey(PurchaseRequest pr) {
    return String.valueOf(pr.getSupplierPartner().getId());
  }

  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest)
      throws AxelorException {
    return purchaseOrderRepo.save(
        purchaseOrderCreateService.createPurchaseOrder(
            AuthUtils.getUser(),
            purchaseRequest.getCompany(),
            null,
            purchaseRequest.getSupplierPartner().getCurrency(),
            null,
            null,
            null,
            appBaseService.getTodayDate(purchaseRequest.getCompany()),
            null,
            purchaseRequest.getSupplierPartner(),
            purchaseRequest.getTradingName()));
  }

  protected void generatePoLinesPurchaseRequest(
      PurchaseRequest purchaseRequest, PurchaseOrder purchaseOrder, boolean groupByProduct)
      throws AxelorException {

    for (PurchaseRequestLine purchaseRequestLine : purchaseRequest.getPurchaseRequestLineList()) {

      PurchaseOrderLine pol =
          groupByProduct ? findLineByProductAndUnit(purchaseRequestLine, purchaseOrder) : null;

      if (pol != null) {
        pol.setQty(pol.getQty().add(purchaseRequestLine.getQuantity()));
      } else {
        pol =
            purchaseOrderLineService.createPurchaseOrderLine(
                purchaseOrder,
                purchaseRequestLine.getProduct(),
                purchaseRequestLine.getNewProduct() ? purchaseRequestLine.getProductTitle() : null,
                null,
                purchaseRequestLine.getQuantity(),
                purchaseRequestLine.getUnit());
        purchaseOrder.addPurchaseOrderLineListItem(pol);
      }

      purchaseOrderLineService.compute(pol, purchaseOrder);
    }
  }

  protected PurchaseOrderLine findLineByProductAndUnit(
      PurchaseRequestLine purchaseRequestLine, PurchaseOrder purchaseOrder) {
    return purchaseOrder.getPurchaseOrderLineList().stream()
        .filter(
            l ->
                l != null
                    && (!purchaseRequestLine.getNewProduct()
                        ? purchaseRequestLine.getProduct().equals(l.getProduct())
                        : purchaseRequestLine.getProductTitle().equals(l.getProductName()))
                    && (purchaseRequestLine.getUnit() == null
                        || purchaseRequestLine.getUnit().equals(l.getUnit())))
        .findFirst()
        .orElse(null);
  }
}
