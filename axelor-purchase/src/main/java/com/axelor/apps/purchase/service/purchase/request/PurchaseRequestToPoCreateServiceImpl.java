/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service.purchase.request;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
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
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
      List<PurchaseRequest> purchaseRequests,
      Boolean groupBySupplier,
      Boolean groupByProduct,
      Company company,
      Partner defaultSupplier)
      throws AxelorException {

    List<String> alreadyLinkedRequests = new ArrayList<>();
    List<String> notAcceptedRequests = new ArrayList<>();
    List<PurchaseRequest> validRequests =
        filterValidRequests(purchaseRequests, alreadyLinkedRequests, notAcceptedRequests);

    List<PurchaseOrder> createdPos;
    if (Boolean.TRUE.equals(groupByProduct)) {
      createdPos = createFromRequestsGroupedByProduct(validRequests, company, defaultSupplier);
    } else {
      createdPos =
          createFromRequestsGroupedBySupplier(
              validRequests, groupBySupplier, company, defaultSupplier);
    }

    return new PurchaseRequestToPoGenerationResult(
        createdPos, buildGenerationWarnings(alreadyLinkedRequests, notAcceptedRequests));
  }

  protected List<PurchaseOrder> createFromRequestsGroupedBySupplier(
      List<PurchaseRequest> validRequests,
      Boolean groupBySupplier,
      Company company,
      Partner defaultSupplier)
      throws AxelorException {
    final boolean grouped = Boolean.TRUE.equals(groupBySupplier);
    final Map<String, PoGroup> poMap = new HashMap<>();
    for (PurchaseRequest purchaseRequest : validRequests) {
      String key;
      Partner supplier =
          Optional.ofNullable(purchaseRequest.getSupplierPartner()).orElse(defaultSupplier);
      if (grouped && supplier != null) {
        key = getGroupBySupplierKey(purchaseRequest, supplier);
      } else {
        key = purchaseRequest.getId().toString();
      }
      PoGroup poGroup = poMap.get(key);
      if (poGroup == null) {
        poGroup =
            new PoGroup(
                createPurchaseOrder(purchaseRequest, company, defaultSupplier), new ArrayList<>());
        poMap.put(key, poGroup);
      }
      if (grouped) {
        poGroup.lines().addAll(purchaseRequest.getPurchaseRequestLineList());
      } else {
        generatePoLinesPurchaseRequest(purchaseRequest, poGroup.purchaseOrder());
      }
      purchaseRequest.setPurchaseOrder(poGroup.purchaseOrder());
      purchaseRequestRepo.save(purchaseRequest);
    }
    List<PurchaseOrder> createdPos = new ArrayList<>();
    for (PoGroup poGroup : poMap.values()) {
      PurchaseOrder po = poGroup.purchaseOrder();
      if (grouped) {
        generateGroupedPoLines(poGroup.lines(), po);
      }
      purchaseOrderService.computePurchaseOrder(po);
      purchaseOrderRepo.save(po);
      createdPos.add(po);
    }
    return createdPos;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder createFromRequest(PurchaseRequest pr) throws AxelorException {
    PurchaseRequestToPoGenerationResult result =
        createFromRequests(List.of(pr), false, false, null, null);

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

  protected List<PurchaseRequest> filterValidRequests(
      List<PurchaseRequest> purchaseRequests,
      List<String> alreadyLinkedRefs,
      List<String> notAcceptedRefs) {
    List<PurchaseRequest> valid = new ArrayList<>();
    for (PurchaseRequest pr : purchaseRequests) {
      if (pr.getPurchaseOrder() != null) {
        alreadyLinkedRefs.add(getPurchaseRequestReference(pr));
      } else if (pr.getStatusSelect() != PurchaseRequestRepository.STATUS_ACCEPTED) {
        notAcceptedRefs.add(getPurchaseRequestReference(pr));
      } else {
        valid.add(pr);
      }
    }
    return valid;
  }

  protected String getGroupBySupplierKey(PurchaseRequest pr, Partner supplier) {
    return String.valueOf(supplier.getId());
  }

  protected PurchaseOrder createPurchaseOrder(
      PurchaseRequest purchaseRequest, Company defaultCompany, Partner defaultSupplier)
      throws AxelorException {
    Partner supplier =
        Optional.ofNullable(purchaseRequest.getSupplierPartner()).orElse(defaultSupplier);
    Company company = Optional.ofNullable(purchaseRequest.getCompany()).orElse(defaultCompany);
    return createPurchaseOrder(supplier, company, purchaseRequest.getTradingName());
  }

  protected PurchaseOrder createPurchaseOrder(
      Partner supplier, Company company, TradingName tradingName) throws AxelorException {
    PurchaseOrder purchaseOrder =
        purchaseOrderCreateService.createPurchaseOrder(
            AuthUtils.getUser(),
            company,
            null,
            supplier.getCurrency(),
            null,
            null,
            null,
            appBaseService.getTodayDate(company),
            null,
            supplier,
            tradingName);
    setPurchaseOrderSupplierDetails(purchaseOrder);
    return purchaseOrderRepo.save(purchaseOrder);
  }

  protected void setPurchaseOrderSupplierDetails(PurchaseOrder purchaseOrder)
      throws AxelorException {
    Partner supplierPartner = purchaseOrder.getSupplierPartner();
    if (supplierPartner == null) {
      return;
    }
    purchaseOrder.setNotes(supplierPartner.getPurchaseOrderComments());

    if (supplierPartner.getContactPartnerSet().size() == 1) {
      purchaseOrder.setContactPartner(supplierPartner.getContactPartnerSet().iterator().next());
    }

    purchaseOrder.setPriceList(
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(supplierPartner, PriceListRepository.TYPE_PURCHASE));
  }

  protected void generatePoLinesPurchaseRequest(
      PurchaseRequest purchaseRequest, PurchaseOrder purchaseOrder) throws AxelorException {
    for (PurchaseRequestLine line : purchaseRequest.getPurchaseRequestLineList()) {
      addPurchaseOrderLine(purchaseOrder, line, line.getQuantity());
    }
  }

  protected void generateGroupedPoLines(
      List<PurchaseRequestLine> requestLines, PurchaseOrder purchaseOrder) throws AxelorException {
    Map<ProductUnit, List<PurchaseRequestLine>> linesByProductUnit = new LinkedHashMap<>();
    List<List<PurchaseRequestLine>> lineGroups = new ArrayList<>();
    for (PurchaseRequestLine line : requestLines) {
      if (line.getProduct() == null || line.getNewProduct() || line.getUnit() == null) {
        lineGroups.add(List.of(line));
        continue;
      }
      ProductUnit key = new ProductUnit(line.getProduct(), line.getUnit());
      List<PurchaseRequestLine> group = linesByProductUnit.get(key);
      if (group == null) {
        group = new ArrayList<>();
        linesByProductUnit.put(key, group);
        lineGroups.add(group);
      }
      group.add(line);
    }

    for (List<PurchaseRequestLine> group : lineGroups) {
      BigDecimal quantity =
          group.stream()
              .map(PurchaseRequestLine::getQuantity)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      addPurchaseOrderLine(purchaseOrder, group.get(0), quantity);
    }
  }

  protected void addPurchaseOrderLine(
      PurchaseOrder purchaseOrder, PurchaseRequestLine requestLine, BigDecimal quantity)
      throws AxelorException {
    PurchaseOrderLine purchaseOrderLine =
        purchaseOrderLineService.createPurchaseOrderLine(
            purchaseOrder,
            requestLine.getProduct(),
            requestLine.getNewProduct() ? requestLine.getProductTitle() : null,
            null,
            quantity,
            requestLine.getUnit());
    purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
    if (purchaseOrderLine.getProduct() != null) {
      purchaseOrderLineService.fillPrice(purchaseOrderLine, purchaseOrder);
    }
    purchaseOrderLineService.compute(purchaseOrderLine, purchaseOrder);
  }

  protected record PoGroup(PurchaseOrder purchaseOrder, List<PurchaseRequestLine> lines) {}

  protected record ProductUnit(Product product, Unit unit) {}

  protected List<PurchaseOrder> createFromRequestsGroupedByProduct(
      List<PurchaseRequest> validRequests, Company company, Partner defaultSupplier)
      throws AxelorException {

    List<PurchaseOrder> createdPos = new ArrayList<>();

    Map<Product, List<PurchaseRequestLine>> linesByProduct = new LinkedHashMap<>();
    Map<Product, Set<PurchaseRequest>> prsByProduct = new LinkedHashMap<>();

    for (PurchaseRequest pr : validRequests) {
      for (PurchaseRequestLine line : pr.getPurchaseRequestLineList()) {
        if (line.getProduct() == null) {
          continue;
        }
        linesByProduct.computeIfAbsent(line.getProduct(), k -> new ArrayList<>()).add(line);
        prsByProduct.computeIfAbsent(line.getProduct(), k -> new LinkedHashSet<>()).add(pr);
      }
    }

    for (Map.Entry<Product, List<PurchaseRequestLine>> entry : linesByProduct.entrySet()) {
      Product product = entry.getKey();
      List<PurchaseRequestLine> lines = entry.getValue();
      Set<PurchaseRequest> contributingPrs = prsByProduct.get(product);

      // Resolve supplier: if all PRs have the same supplier use it directly;
      // if they differ, the user must have provided defaultSupplier via the wizard.
      Set<Partner> distinctSuppliers =
          contributingPrs.stream()
              .map(PurchaseRequest::getSupplierPartner)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());

      Partner supplier;
      if (distinctSuppliers.size() <= 1) {
        supplier =
            distinctSuppliers.isEmpty() ? defaultSupplier : distinctSuppliers.iterator().next();
      } else {
        // Multiple different suppliers: user must have selected one via the wizard
        if (defaultSupplier == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_SUPPLIER_CONFLICT_FOR_PRODUCT),
                  product.getName()));
        }
        supplier = defaultSupplier;
      }
      // 0 suppliers across all contributing PRs and no wizard default → also an error
      if (supplier == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_SUPPLIER_CONFLICT_FOR_PRODUCT),
                product.getName()));
      }

      Company poCompany =
          contributingPrs.stream()
              .map(PurchaseRequest::getCompany)
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(company);

      PurchaseOrder po = createPurchaseOrder(supplier, poCompany, null);
      createdPos.add(po);

      BigDecimal totalQty =
          lines.stream()
              .map(PurchaseRequestLine::getQuantity)
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      PurchaseRequestLine firstLine = lines.get(0);
      PurchaseOrderLine pol =
          purchaseOrderLineService.createPurchaseOrderLine(
              po, product, null, null, totalQty, firstLine.getUnit());
      po.addPurchaseOrderLineListItem(pol);
      purchaseOrderLineService.compute(pol, po);

      purchaseOrderService.computePurchaseOrder(po);
      purchaseOrderRepo.save(po);

      // Link each contributing PR to the PO. When a PR has lines for multiple products,
      // it is linked to the PO of the first product encountered (first-PO-wins).
      for (PurchaseRequest pr : contributingPrs) {
        if (pr.getPurchaseOrder() == null) {
          pr.setPurchaseOrder(po);
        }
        purchaseRequestRepo.save(pr);
      }
    }

    return createdPos;
  }
}
