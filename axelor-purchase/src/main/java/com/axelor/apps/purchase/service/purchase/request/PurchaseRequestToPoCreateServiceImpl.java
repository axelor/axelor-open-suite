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
import java.util.Collection;
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
    checkSuppliers(validRequests, defaultSupplier);

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
      linkPurchaseRequest(purchaseRequest, poGroup.purchaseOrder());
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
        ? String.valueOf(purchaseRequest.getId())
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

  /**
   * Server-side guard: every request must resolve to a supplier (its own or the wizard default)
   * before any purchase order is created.
   */
  protected void checkSuppliers(List<PurchaseRequest> purchaseRequests, Partner defaultSupplier)
      throws AxelorException {
    if (defaultSupplier != null) {
      return;
    }
    List<String> missingSupplierRefs =
        purchaseRequests.stream()
            .filter(pr -> pr.getSupplierPartner() == null)
            .map(this::getPurchaseRequestReference)
            .collect(Collectors.toList());
    if (!missingSupplierRefs.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_MISSING_SUPPLIER_USER),
          String.join(", ", missingSupplierRefs));
    }
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
    return createPurchaseOrder(purchaseRequest, supplier, company);
  }

  /**
   * Creates a purchase order for the given supplier and company. The source request carries the
   * request-level information copied onto the order (trading name, and stock location in
   * supplychain), so subclasses override this method to enrich the order.
   */
  protected PurchaseOrder createPurchaseOrder(
      PurchaseRequest sourceRequest, Partner supplier, Company company) throws AxelorException {
    if (supplier == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_MISSING_SUPPLIER_USER),
          getPurchaseRequestReference(sourceRequest));
    }
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
            sourceRequest.getTradingName());
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
      line.setPurchaseOrder(purchaseOrder);
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
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      addPurchaseOrderLine(purchaseOrder, group.get(0), quantity);
      for (PurchaseRequestLine line : group) {
        line.setPurchaseOrder(purchaseOrder);
      }
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

  /**
   * Links the request to the order. The request-level link points to the first order generated for
   * the request; every line keeps its own link (see {@link #generateGroupedPoLines}), so a request
   * spread over several orders (group by product) is tracked line by line.
   */
  protected void linkPurchaseRequest(PurchaseRequest purchaseRequest, PurchaseOrder purchaseOrder) {
    if (purchaseRequest.getPurchaseOrder() == null) {
      purchaseRequest.setPurchaseOrder(purchaseOrder);
    }
    purchaseRequestRepo.save(purchaseRequest);
  }

  protected record PoGroup(PurchaseOrder purchaseOrder, List<PurchaseRequestLine> lines) {}

  protected record ProductUnit(Product product, Unit unit) {}

  protected record ProductGroup(List<PurchaseRequestLine> lines, Set<PurchaseRequest> requests) {}

  /**
   * One purchase order per product (and per company; supplychain also splits per stock location),
   * across all selected requests. Lines of the same product and unit are summed into a single order
   * line; free-text lines are grouped under an order per product title.
   */
  protected List<PurchaseOrder> createFromRequestsGroupedByProduct(
      List<PurchaseRequest> validRequests, Company company, Partner defaultSupplier)
      throws AxelorException {

    Map<String, ProductGroup> groups = new LinkedHashMap<>();
    for (PurchaseRequest pr : validRequests) {
      Company prCompany = Optional.ofNullable(pr.getCompany()).orElse(company);
      for (PurchaseRequestLine line : pr.getPurchaseRequestLineList()) {
        String key = getGroupByProductKey(pr, line, prCompany);
        ProductGroup group =
            groups.computeIfAbsent(
                key, k -> new ProductGroup(new ArrayList<>(), new LinkedHashSet<>()));
        group.lines().add(line);
        group.requests().add(pr);
      }
    }

    List<PurchaseOrder> createdPos = new ArrayList<>();
    for (ProductGroup group : groups.values()) {
      PurchaseRequest firstRequest = group.requests().iterator().next();
      Partner supplier = resolveGroupSupplier(group, defaultSupplier);
      Company poCompany = Optional.ofNullable(firstRequest.getCompany()).orElse(company);

      PurchaseOrder po = createPurchaseOrder(firstRequest, supplier, poCompany);
      generateGroupedPoLines(group.lines(), po);
      purchaseOrderService.computePurchaseOrder(po);
      purchaseOrderRepo.save(po);
      createdPos.add(po);

      for (PurchaseRequest pr : group.requests()) {
        linkPurchaseRequest(pr, po);
      }
    }

    return createdPos;
  }

  /**
   * Key identifying the purchase order a request line belongs to when grouping by product. The
   * default key is the product (or the product title for free-text lines) and the company.
   */
  protected String getGroupByProductKey(
      PurchaseRequest purchaseRequest, PurchaseRequestLine line, Company company) {
    String productKey;
    if (line.getProduct() != null && !line.getNewProduct()) {
      productKey = "P" + line.getProduct().getId();
    } else if (StringUtils.notBlank(line.getProductTitle())) {
      productKey = "T" + line.getProductTitle().trim();
    } else {
      productKey = "L" + (line.getId() != null ? line.getId() : System.identityHashCode(line));
    }
    return productKey + "_C" + (company != null ? company.getId() : "");
  }

  /**
   * All contributing requests share one supplier: use it. Different suppliers: the wizard supplier
   * is mandatory. No supplier at all: the wizard supplier is mandatory too.
   */
  protected Partner resolveGroupSupplier(ProductGroup group, Partner defaultSupplier)
      throws AxelorException {
    Set<Partner> distinctSuppliers =
        group.requests().stream()
            .map(PurchaseRequest::getSupplierPartner)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    if (distinctSuppliers.size() == 1) {
      return distinctSuppliers.iterator().next();
    }
    if (defaultSupplier != null) {
      return defaultSupplier;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_INCONSISTENCY,
        I18n.get(PurchaseExceptionMessage.PURCHASE_REQUEST_SUPPLIER_CONFLICT_FOR_PRODUCT),
        getProductGroupLabel(group.lines()));
  }

  protected String getProductGroupLabel(Collection<PurchaseRequestLine> lines) {
    PurchaseRequestLine line = lines.iterator().next();
    if (line.getProduct() != null && !line.getNewProduct()) {
      return line.getProduct().getName();
    }
    return line.getProductTitle();
  }
}
