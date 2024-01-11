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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingServiceImpl;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.utils.MapTools;
import com.google.inject.Inject;
import java.util.List;
import java.util.StringJoiner;

public class SaleOrderMergingServiceSupplyChainImpl extends SaleOrderMergingServiceImpl {

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
  protected AppSupplychainService appSupplyChainService;

  @Inject
  public SaleOrderMergingServiceSupplyChainImpl(
      SaleOrderCreateService saleOrdreCreateService,
      AppSaleService appSaleService,
      AppSupplychainService appSupplyChainService) {
    super(saleOrdreCreateService);
    this.appSaleService = appSaleService;
    this.appSupplyChainService = appSupplyChainService;
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
      if (appSupplyChainService.getAppSupplychain().getActivatePartnerRelations()) {
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
        checks.setExistIncotermDiff(true);
      }
      if (appSupplyChainService.getAppSupplychain().getActivatePartnerRelations()) {
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
    }
  }

  @Override
  protected SaleOrder mergeSaleOrders(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result) throws AxelorException {
    if (!appSaleService.isApp("supplychain")) {
      return super.mergeSaleOrders(saleOrdersToMerge, result);
    }
    CommonFieldsSupplyChainImpl commonFields = getCommonFields(result);
    return Beans.get(SaleOrderCreateServiceSupplychainImpl.class)
        .mergeSaleOrders(
            saleOrdersToMerge,
            commonFields.getCommonCurrency(),
            commonFields.getCommonClientPartner(),
            commonFields.getCommonCompany(),
            commonFields.getCommonStockLocation(),
            commonFields.getCommonContactPartner(),
            commonFields.getCommonPriceList(),
            commonFields.getCommonTeam(),
            commonFields.getCommonTaxNumber(),
            commonFields.getCommonFiscalPosition(),
            commonFields.getCommonIncoterm(),
            commonFields.getCommonInvoicedPartner(),
            commonFields.getCommonDeliveredPartner());
  }

  @Override
  protected void updateResultWithContext(SaleOrderMergingResult result, Context context) {
    super.updateResultWithContext(result, context);
    if (appSaleService.isApp("supplychain")) {
      if (context.get("stockLocation") != null) {
        getCommonFields(result)
            .setCommonStockLocation(
                MapTools.findObject(StockLocation.class, context.get("stockLocation")));
      }
    }
  }
}
