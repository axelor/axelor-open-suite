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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class CallTenderPurchaseOrderServiceImpl implements CallTenderPurchaseOrderService {

  protected final PurchaseOrderCreateService purchaseOrderCreateService;
  protected final AppBaseService appBaseService;
  protected final PartnerPriceListService partnerPriceListService;
  protected final PurchaseOrderLineService purchaseOrderLineService;
  protected final ProductCompanyService productCompanyService;
  protected final PurchaseOrderRepository purchaseOrderRepository;
  protected final PurchaseOrderService purchaseOrderService;

  @Inject
  public CallTenderPurchaseOrderServiceImpl(
      PurchaseOrderCreateService purchaseOrderCreateService,
      AppBaseService appBaseService,
      PartnerPriceListService partnerPriceListService,
      PurchaseOrderLineService purchaseOrderLineService,
      ProductCompanyService productCompanyService,
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseOrderService purchaseOrderService) {
    this.purchaseOrderCreateService = purchaseOrderCreateService;
    this.appBaseService = appBaseService;
    this.partnerPriceListService = partnerPriceListService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.productCompanyService = productCompanyService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseOrderService = purchaseOrderService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<PurchaseOrder> generatePurchaseOrders(
      CallTender callTender, List<CallTenderOffer> selectedCallTenderOfferList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(selectedCallTenderOfferList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_NO_PO_SELECTED));
    }

    List<PurchaseOrder> purchaseOrderList = new ArrayList<>();

    Company company = callTender.getCompany();

    List<Partner> partnerList =
        selectedCallTenderOfferList.stream()
            .map(CallTenderOffer::getSupplierPartner)
            .distinct()
            .collect(Collectors.toList());
    for (Partner partner : partnerList) {
      purchaseOrderList.add(
          createPurchaseOrderFromOffer(callTender, partner, company, selectedCallTenderOfferList));
    }

    return purchaseOrderList;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected PurchaseOrder createPurchaseOrderFromOffer(
      CallTender callTender,
      Partner partner,
      Company company,
      List<CallTenderOffer> callTenderOfferList)
      throws AxelorException {
    PurchaseOrder purchaseOrder = createPurchaseOrder(callTender, partner, company);
    List<CallTenderOffer> filteredCallTenderOfferList =
        callTenderOfferList.stream()
            .filter(offer -> offer.getSupplierPartner().equals(partner))
            .collect(Collectors.toList());
    for (CallTenderOffer filteredCallTenderOffer : filteredCallTenderOfferList) {
      filteredCallTenderOffer.setStatusSelect(CallTenderOfferRepository.STATUS_SELECTED);
      PurchaseOrderLine purchaseOrderLine =
          createPurchaseOrderLine(filteredCallTenderOffer, company, purchaseOrder);
      purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
    }
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
    return purchaseOrderRepository.save(purchaseOrder);
  }

  protected PurchaseOrder createPurchaseOrder(
      CallTender callTender, Partner partner, Company company) throws AxelorException {
    PurchaseOrder purchaseOrder =
        purchaseOrderCreateService.createPurchaseOrder(
            AuthUtils.getUser(),
            company,
            null,
            partner.getCurrency(),
            null,
            callTender.getCallTenderSeq(),
            null,
            appBaseService.getTodayDate(company),
            partnerPriceListService.getDefaultPriceList(partner, PriceListRepository.TYPE_PURCHASE),
            partner,
            null,
            null);
    purchaseOrder.setCallTender(callTender);
    return purchaseOrder;
  }

  protected PurchaseOrderLine createPurchaseOrderLine(
      CallTenderOffer filteredCallTenderOffer, Company company, PurchaseOrder purchaseOrder)
      throws AxelorException {
    Product product = filteredCallTenderOffer.getProduct();
    Unit unit =
        filteredCallTenderOffer.getProposedUnit() != null
            ? filteredCallTenderOffer.getProposedUnit()
            : (Unit) productCompanyService.get(product, "purchasesUnit", company);
    PurchaseOrderLine purchaseOrderLine =
        purchaseOrderLineService.createPurchaseOrderLine(
            purchaseOrder,
            product,
            product.getName(),
            filteredCallTenderOffer.getOfferComment(),
            filteredCallTenderOffer.getProposedQty(),
            unit);
    purchaseOrderLine.setPrice(filteredCallTenderOffer.getProposedPrice());
    purchaseOrderLineService.compute(purchaseOrderLine, purchaseOrder);
    return purchaseOrderLine;
  }
}
