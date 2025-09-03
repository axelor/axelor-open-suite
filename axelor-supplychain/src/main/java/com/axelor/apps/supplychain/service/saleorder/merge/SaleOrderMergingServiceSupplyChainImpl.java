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
package com.axelor.apps.supplychain.service.saleorder.merge;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.merge.SaleOrderMergingServiceImpl;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCreateSupplychainService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SaleOrderMergingServiceSupplyChainImpl extends SaleOrderMergingServiceImpl
    implements SaleOrderMergingServiceSupplyChain {

  protected static class CommonFieldsSupplyChainImpl extends CommonFieldsImpl {
    private StockLocation commonStockLocation = null;
    private Incoterm commonIncoterm = null;
    private Partner commonInvoicedPartner = null;
    private Partner commonDeliveredPartner = null;

    public StockLocation getCommonStockLocation() {
      return commonStockLocation;
    }

    public void setCommonStockLocation(StockLocation commonStockLocation) {
      this.commonStockLocation = commonStockLocation;
    }

    public Incoterm getCommonIncoterm() {
      return commonIncoterm;
    }

    public void setCommonIncoterm(Incoterm commonIncoterm) {
      this.commonIncoterm = commonIncoterm;
    }

    public Partner getCommonInvoicedPartner() {
      return commonInvoicedPartner;
    }

    public void setCommonInvoicedPartner(Partner commonInvoicedPartner) {
      this.commonInvoicedPartner = commonInvoicedPartner;
    }

    public Partner getCommonDeliveredPartner() {
      return commonDeliveredPartner;
    }

    public void setCommonDeliveredPartner(Partner commonDeliveredPartner) {
      this.commonDeliveredPartner = commonDeliveredPartner;
    }
  }

  protected static class ChecksSupplyChainImpl extends ChecksImpl {
    private boolean existStockLocationDiff = false;
    private boolean existIncotermDiff = false;
    private boolean existInvoicedPartnerDiff = false;
    private boolean existDeliveredPartnerDiff = false;
    private boolean existIntercoDiff = false;

    public boolean isExistStockLocationDiff() {
      return existStockLocationDiff;
    }

    public void setExistStockLocationDiff(boolean existStockLocationDiff) {
      this.existStockLocationDiff = existStockLocationDiff;
    }

    public boolean isExistIncotermDiff() {
      return existIncotermDiff;
    }

    public void setExistIncotermDiff(boolean existIncotermDiff) {
      this.existIncotermDiff = existIncotermDiff;
    }

    public boolean isExistInvoicedPartnerDiff() {
      return existInvoicedPartnerDiff;
    }

    public void setExistInvoicedPartnerDiff(boolean existInvoicedPartnerDiff) {
      this.existInvoicedPartnerDiff = existInvoicedPartnerDiff;
    }

    public boolean isExistDeliveredPartnerDiff() {
      return existDeliveredPartnerDiff;
    }

    public void setExistDeliveredPartnerDiff(boolean existDeliveredPartnerDiff) {
      this.existDeliveredPartnerDiff = existDeliveredPartnerDiff;
    }

    public boolean isExistIntercoDiff() {
      return existIntercoDiff;
    }

    public void setExistIntercoDiff(boolean existIntercoDiff) {
      this.existIntercoDiff = existIntercoDiff;
    }
  }

  protected static class SaleOrderMergingResultSupplyChainImpl extends SaleOrderMergingResultImpl {
    private final CommonFieldsSupplyChainImpl commonFields;
    private final ChecksSupplyChainImpl checks;

    public SaleOrderMergingResultSupplyChainImpl() {
      super();
      this.commonFields = new CommonFieldsSupplyChainImpl();
      this.checks = new ChecksSupplyChainImpl();
    }
  }

  protected AppSaleService appSaleService;
  protected AppStockService appStockService;
  protected SaleOrderCreateSupplychainService saleOrderCreateSupplychainService;

  @Inject
  public SaleOrderMergingServiceSupplyChainImpl(
      SaleOrderCreateService saleOrderCreateService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      DMSService dmsService,
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      AppStockService appStockService,
      SaleOrderCreateSupplychainService saleOrderCreateSupplychainService) {
    super(
        saleOrderCreateService,
        saleOrderRepository,
        saleOrderComputeService,
        saleOrderLineRepository,
        dmsService,
        appBaseService);
    this.appSaleService = appSaleService;
    this.appStockService = appStockService;
    this.saleOrderCreateSupplychainService = saleOrderCreateSupplychainService;
  }

  @Override
  public SaleOrderMergingResultSupplyChainImpl create() {
    return new SaleOrderMergingResultSupplyChainImpl();
  }

  @Override
  public CommonFieldsSupplyChainImpl getCommonFields(SaleOrderMergingResult result) {
    return ((SaleOrderMergingResultSupplyChainImpl) result).commonFields;
  }

  @Override
  public ChecksSupplyChainImpl getChecks(SaleOrderMergingResult result) {
    return ((SaleOrderMergingResultSupplyChainImpl) result).checks;
  }

  @Override
  protected boolean isConfirmationNeeded(SaleOrderMergingResult result) {
    if (!appSaleService.isApp("supplychain")) {
      return super.isConfirmationNeeded(result);
    }
    return super.isConfirmationNeeded(result) || getChecks(result).existStockLocationDiff;
  }

  @Override
  protected void fillCommonFields(SaleOrder firstSaleOrder, SaleOrderMergingResult result) {
    super.fillCommonFields(firstSaleOrder, result);
    if (appSaleService.isApp("supplychain")) {
      getCommonFields(result).setCommonStockLocation(firstSaleOrder.getStockLocation());
      getCommonFields(result).setCommonIncoterm(firstSaleOrder.getIncoterm());
      if (appBaseService.getAppBase().getActivatePartnerRelations()) {
        getCommonFields(result).setCommonInvoicedPartner(firstSaleOrder.getInvoicedPartner());
        getCommonFields(result).setCommonDeliveredPartner(firstSaleOrder.getDeliveredPartner());
      }
    }
  }

  @Override
  protected void updateDiffsCommonFields(SaleOrder saleOrder, SaleOrderMergingResult result) {
    super.updateDiffsCommonFields(saleOrder, result);
    if (appSaleService.isApp("supplychain")) {
      CommonFieldsSupplyChainImpl commonFields = getCommonFields(result);
      ChecksSupplyChainImpl checks = getChecks(result);
      if ((commonFields.getCommonStockLocation() == null ^ saleOrder.getStockLocation() == null)
          || (commonFields.getCommonStockLocation() != saleOrder.getStockLocation()
              && !commonFields.getCommonStockLocation().equals(saleOrder.getStockLocation()))) {
        commonFields.setCommonStockLocation(null);
        checks.setExistStockLocationDiff(true);
      }
      if ((commonFields.getCommonIncoterm() == null ^ saleOrder.getIncoterm() == null)
          || (commonFields.getCommonIncoterm() != saleOrder.getIncoterm()
              && !commonFields.getCommonIncoterm().equals(saleOrder.getIncoterm()))) {
        commonFields.setCommonIncoterm(null);
        checks.setExistIncotermDiff(appStockService.getAppStock().getIsIncotermEnabled());
      }
      if (appBaseService.getAppBase().getActivatePartnerRelations()) {
        if ((commonFields.getCommonInvoicedPartner() == null
                ^ saleOrder.getInvoicedPartner() == null)
            || (commonFields.getCommonInvoicedPartner() != saleOrder.getInvoicedPartner()
                && !commonFields
                    .getCommonInvoicedPartner()
                    .equals(saleOrder.getInvoicedPartner()))) {
          commonFields.setCommonInvoicedPartner(null);
          checks.setExistInvoicedPartnerDiff(true);
        }
        if ((commonFields.getCommonDeliveredPartner() == null
                ^ saleOrder.getDeliveredPartner() == null)
            || (commonFields.getCommonDeliveredPartner() != saleOrder.getDeliveredPartner()
                && !commonFields
                    .getCommonDeliveredPartner()
                    .equals(saleOrder.getDeliveredPartner()))) {
          commonFields.setCommonDeliveredPartner(null);
          checks.setExistDeliveredPartnerDiff(true);
        }
      }
    }
  }

  @Override
  protected void checkErrors(StringJoiner fieldErrors, SaleOrderMergingResult result) {
    super.checkErrors(fieldErrors, result);

    if (appSaleService.isApp("supplychain")) {
      if (getChecks(result).isExistIncotermDiff()) {
        fieldErrors.add(I18n.get(SupplychainExceptionMessage.SALE_ORDER_MERGE_ERROR_INCOTERM));
      }
      if (getChecks(result).isExistInvoicedPartnerDiff()) {
        fieldErrors.add(
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_MERGE_ERROR_INVOICED_PARTNER));
      }
      if (getChecks(result).isExistDeliveredPartnerDiff()) {
        fieldErrors.add(
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_MERGE_ERROR_DELIVERED_PARTNER));
      }
      if (getChecks(result).isExistIntercoDiff()) {
        fieldErrors.add(I18n.get(SupplychainExceptionMessage.SALE_ORDER_MERGE_ERROR_INTERCO));
      }
    }
  }

  @Override
  protected SaleOrder generateSaleOrder(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result) throws AxelorException {
    if (!appSaleService.isApp("supplychain")) {
      return super.generateSaleOrder(saleOrdersToMerge, result);
    }
    String internalNote =
        computeConcatenatedString(saleOrdersToMerge, SaleOrder::getInternalNote, "<br>");
    String numSeq = computeConcatenatedString(saleOrdersToMerge, SaleOrder::getSaleOrderSeq, "-");
    String externalRef =
        computeConcatenatedString(saleOrdersToMerge, SaleOrder::getExternalReference, "|");

    SaleOrder saleOrderMerged =
        saleOrderCreateSupplychainService.createSaleOrder(
            AuthUtils.getUser(),
            getCommonFields(result).getCommonCompany(),
            getCommonFields(result).getCommonContactPartner(),
            getCommonFields(result).getCommonCurrency(),
            null,
            numSeq,
            externalRef,
            getCommonFields(result).getCommonStockLocation(),
            getCommonFields(result).getCommonPriceList(),
            getCommonFields(result).getCommonClientPartner(),
            getCommonFields(result).getCommonTeam(),
            getCommonFields(result).getCommonTaxNumber(),
            internalNote,
            getCommonFields(result).getCommonFiscalPosition(),
            getCommonFields(result).getCommonTradingName(),
            getCommonFields(result).getCommonIncoterm(),
            getCommonFields(result).getCommonInvoicedPartner(),
            getCommonFields(result).getCommonDeliveredPartner());

    saleOrderMerged.setInAti(saleOrdersToMerge.stream().anyMatch(SaleOrder::getInAti));
    saleOrderMerged.setInterco(saleOrdersToMerge.stream().anyMatch(SaleOrder::getInterco));

    this.attachToNewSaleOrder(saleOrdersToMerge, saleOrderMerged);

    saleOrderComputeService.computeSaleOrder(saleOrderMerged);
    updateChildrenOrder(saleOrdersToMerge, saleOrderMerged);
    return saleOrderMerged;
  }

  @Override
  protected void updateResultWithContext(SaleOrderMergingResult result, Context context) {
    super.updateResultWithContext(result, context);
    if (appSaleService.isApp("supplychain")) {
      if (context.get("stockLocation") != null) {
        getCommonFields(result)
            .setCommonStockLocation(MapHelper.get(context, StockLocation.class, "stockLocation"));
      }
    }
  }

  @Override
  public SaleOrder getDummyMergedSaleOrder(StockMove stockMove) throws AxelorException {
    SaleOrderMergingResult result =
        simulateMergeSaleOrders(new ArrayList<>(stockMove.getSaleOrderSet()));

    if (result.isConfirmationNeeded()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.STOCK_MOVE_INVOICING_ERROR),
          stockMove.getStockMoveSeq());
    }
    return result.getSaleOrder();
  }

  @Override
  protected void checkDiffs(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result, SaleOrder firstSaleOrder) {
    super.checkDiffs(saleOrdersToMerge, result, firstSaleOrder);

    if (saleOrdersToMerge.stream()
        .anyMatch(order -> order.getInterco() != firstSaleOrder.getInterco())) {
      ChecksSupplyChainImpl checks = getChecks(result);
      checks.setExistIntercoDiff(true);
    }
  }
}
