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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderMergingServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class PurchaseOrderMergingServiceSupplyChainImpl extends PurchaseOrderMergingServiceImpl
    implements PurchaseOrderMergingSupplychainService {

  protected static class CommonFieldsSupplyChainImpl extends CommonFieldsImpl {
    private StockLocation commonStockLocation = null;

    public StockLocation getCommonStockLocation() {
      return commonStockLocation;
    }

    public void setCommonStockLocation(StockLocation commonStockLocation) {
      this.commonStockLocation = commonStockLocation;
    }
  }

  protected static class ChecksSupplyChainImpl extends ChecksImpl {
    private boolean existStockLocationDiff = false;
    private boolean existIntercoDiff = false;

    public boolean isExistStockLocationDiff() {
      return existStockLocationDiff;
    }

    public void setExistStockLocationDiff(boolean existStockLocationDiff) {
      this.existStockLocationDiff = existStockLocationDiff;
    }

    public boolean isExistIntercoDiff() {
      return existIntercoDiff;
    }

    public void setExistIntercoDiff(boolean existIntercoDiff) {
      this.existIntercoDiff = existIntercoDiff;
    }
  }

  protected static class PurchaseOrderMergingResultSupplyChainImpl
      extends PurchaseOrderMergingResultImpl {
    private final CommonFieldsSupplyChainImpl commonFields;
    private final ChecksSupplyChainImpl checks;

    public PurchaseOrderMergingResultSupplyChainImpl() {
      super();
      this.commonFields = new CommonFieldsSupplyChainImpl();
      this.checks = new ChecksSupplyChainImpl();
    }
  }

  protected PurchaseOrderCreateSupplychainService purchaseOrderCreateSupplychainService;

  @Inject
  public PurchaseOrderMergingServiceSupplyChainImpl(
      AppPurchaseService appPurchaseService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderRepository purchaseOrderRepository,
      DMSService dmsService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      PurchaseOrderCreateSupplychainService purchaseOrderCreateSupplychainService) {
    super(
        appPurchaseService,
        purchaseOrderService,
        purchaseOrderCreateService,
        purchaseOrderRepository,
        dmsService,
        purchaseOrderLineRepository);
    this.purchaseOrderCreateSupplychainService = purchaseOrderCreateSupplychainService;
  }

  @Override
  public PurchaseOrderMergingResultSupplyChainImpl create() {
    return new PurchaseOrderMergingResultSupplyChainImpl();
  }

  @Override
  public CommonFieldsSupplyChainImpl getCommonFields(PurchaseOrderMergingResult result) {
    return ((PurchaseOrderMergingResultSupplyChainImpl) result).commonFields;
  }

  @Override
  public ChecksSupplyChainImpl getChecks(PurchaseOrderMergingResult result) {
    return ((PurchaseOrderMergingResultSupplyChainImpl) result).checks;
  }

  @Override
  protected boolean isConfirmationNeeded(PurchaseOrderMergingResult result) {
    return super.isConfirmationNeeded(result) || getChecks(result).isExistStockLocationDiff();
  }

  @Override
  protected void fillCommonFields(
      List<PurchaseOrder> purchaseOrdersToMerge, PurchaseOrderMergingResult result) {
    super.fillCommonFields(purchaseOrdersToMerge, result);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getStockLocation)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(getCommonFields(result)::setCommonStockLocation);
  }

  @Override
  protected void updateDiffsCommonFields(
      PurchaseOrder purchaseOrder, PurchaseOrderMergingResult result) {
    super.updateDiffsCommonFields(purchaseOrder, result);
    CommonFieldsSupplyChainImpl commonFields = getCommonFields(result);
    ChecksSupplyChainImpl checks = getChecks(result);
    if (commonFields.getCommonStockLocation() != null
        && !getCommonFields(result)
            .getCommonStockLocation()
            .equals(purchaseOrder.getStockLocation())) {
      commonFields.setCommonStockLocation(null);
      checks.setExistStockLocationDiff(true);
    }
  }

  @Override
  protected PurchaseOrder generatePurchaseOrder(
      List<PurchaseOrder> purchaseOrdersToMerge, PurchaseOrderMergingResult result)
      throws AxelorException {

    String numSeq =
        computeConcatenatedString(purchaseOrdersToMerge, PurchaseOrder::getPurchaseOrderSeq, "-");
    String externalRef =
        computeConcatenatedString(purchaseOrdersToMerge, PurchaseOrder::getExternalReference, "|");
    Company company = getCommonFields(result).getCommonCompany();

    PurchaseOrder purchaseOrderMerged =
        purchaseOrderCreateSupplychainService.createPurchaseOrder(
            AuthUtils.getUser(),
            company,
            getCommonFields(result).getCommonContactPartner(),
            getCommonFields(result).getCommonCurrency(),
            null,
            numSeq,
            externalRef,
            getCommonFields(result).getCommonStockLocation(),
            appPurchaseService.getTodayDate(company),
            getCommonFields(result).getCommonPriceList(),
            getCommonFields(result).getCommonSupplierPartner(),
            getCommonFields(result).getCommonTradingName(),
            getCommonFields(result).getCommonFiscalPosition());

    purchaseOrderMerged.setInAti(purchaseOrdersToMerge.stream().anyMatch(PurchaseOrder::getInAti));
    purchaseOrderMerged.setInterco(
        purchaseOrdersToMerge.stream().anyMatch(PurchaseOrder::getInterco));
    purchaseOrderMerged.setTaxNumber(getCommonFields(result).getCommonCompanyTaxNumber());

    this.attachToNewPurchaseOrder(purchaseOrdersToMerge, purchaseOrderMerged);
    purchaseOrderService.computePurchaseOrder(purchaseOrderMerged);
    return purchaseOrderMerged;
  }

  @Override
  protected void updateResultWithContext(PurchaseOrderMergingResult result, Context context) {
    super.updateResultWithContext(result, context);
    if (context.get("stockLocation") != null) {
      getCommonFields(result)
          .setCommonStockLocation(MapHelper.get(context, StockLocation.class, "stockLocation"));
    }
  }

  @Override
  public PurchaseOrder getDummyMergedPurchaseOrder(StockMove stockMove) throws AxelorException {
    PurchaseOrderMergingResult result =
        simulateMergePurchaseOrders(new ArrayList<>(stockMove.getPurchaseOrderSet()));

    if (result.isConfirmationNeeded()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.STOCK_MOVE_INVOICING_ERROR),
          stockMove.getStockMoveSeq());
    }
    return result.getPurchaseOrder();
  }

  @Override
  protected void checkDiffs(
      List<PurchaseOrder> purchaseOrdersToMerge,
      PurchaseOrderMergingResult result,
      PurchaseOrder firstPurchaseOrder) {
    super.checkDiffs(purchaseOrdersToMerge, result, firstPurchaseOrder);

    if (purchaseOrdersToMerge.stream()
        .anyMatch(order -> order.getInterco() != firstPurchaseOrder.getInterco())) {
      ChecksSupplyChainImpl checks = getChecks(result);
      checks.setExistIntercoDiff(true);
    }
  }

  @Override
  protected void checkErrors(StringJoiner fieldErrors, PurchaseOrderMergingResult result) {
    super.checkErrors(fieldErrors, result);

    if (getChecks(result).isExistIntercoDiff()) {
      fieldErrors.add(
          I18n.get(SupplychainExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_INTERCO_CONFIG));
    }
  }
}
