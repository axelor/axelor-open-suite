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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderMergingServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;

public class PurchaseOrderMergingServiceSupplyChainImpl extends PurchaseOrderMergingServiceImpl {

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

    public boolean isExistStockLocationDiff() {
      return existStockLocationDiff;
    }

    public void setExistStockLocationDiff(boolean existStockLocationDiff) {
      this.existStockLocationDiff = existStockLocationDiff;
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

  protected PurchaseOrderSupplychainService purchaseOrderSupplychainService;

  @Inject
  public PurchaseOrderMergingServiceSupplyChainImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService) {
    super(purchaseOrderService, purchaseOrderRepository);
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
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
  protected PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrdersToMerge, PurchaseOrderMergingResult result)
      throws AxelorException {
    CommonFieldsSupplyChainImpl commonFields = getCommonFields(result);
    return purchaseOrderSupplychainService.mergePurchaseOrders(
        purchaseOrdersToMerge,
        commonFields.getCommonCurrency(),
        commonFields.getCommonSupplierPartner(),
        commonFields.getCommonCompany(),
        commonFields.getCommonStockLocation(),
        commonFields.getCommonContactPartner(),
        commonFields.getCommonPriceList(),
        commonFields.getCommonTradingName());
  }

  @Override
  protected void updateResultWithContext(PurchaseOrderMergingResult result, Context context) {
    super.updateResultWithContext(result, context);
    if (context.get("stockLocation") != null) {
      getCommonFields(result)
          .setCommonStockLocation(MapHelper.get(context, StockLocation.class, "stockLocation"));
    }
  }
}
