package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckServiceImpl;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppStock;
import com.google.inject.Inject;
import java.util.Optional;

public class SaleOrderCheckSupplychainServiceImpl extends SaleOrderCheckServiceImpl {

  protected AppSupplychainService appSupplychainService;
  protected AppStockService appStockService;

  @Inject
  public SaleOrderCheckSupplychainServiceImpl(
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppStockService appStockService) {
    super(appBaseService);
    this.appSupplychainService = appSupplychainService;
    this.appStockService = appStockService;
  }

  @Override
  public String confirmCheckAlert(SaleOrder saleOrder) throws AxelorException {
    if (!appSupplychainService.isApp("supplychain")) {
      return super.confirmCheckAlert(saleOrder);
    }
    isIncotermFilled(saleOrder);
    return "";
  }

  protected void isIncotermFilled(SaleOrder saleOrder) throws AxelorException {
    AppStock appStock = appStockService.getAppStock();
    boolean isIncotermEnabled = appStock.getIsIncotermEnabled();
    Incoterm incoterm = saleOrder.getIncoterm();
    boolean anyStorableProduct =
        saleOrder.getSaleOrderLineList().stream()
            .anyMatch(
                line ->
                    line.getProduct() != null
                        && ProductRepository.PRODUCT_TYPE_STORABLE.equals(
                            line.getProduct().getProductTypeSelect()));
    StockLocation stockLocation = saleOrder.getStockLocation();
    String deliveryAddressAlpha2Code =
        Optional.ofNullable(saleOrder.getDeliveryAddress())
            .map(Address::getCountry)
            .map(Country::getAlpha2Code)
            .orElse("");
    String stockLocationAlpha2Code =
        Optional.ofNullable(stockLocation)
            .map(StockLocation::getAddress)
            .map(Address::getCountry)
            .map(Country::getAlpha2Code)
            .orElse("");
    String companyAlpha2Code =
        Optional.ofNullable(saleOrder.getCompany())
            .map(Company::getAddress)
            .map(Address::getCountry)
            .map(Country::getAlpha2Code)
            .orElse("");
    boolean isStockLocationAddressWrong =
        stockLocation != null && !deliveryAddressAlpha2Code.equals(stockLocationAlpha2Code);
    boolean isCompanyAddressWrong =
        stockLocation == null && !deliveryAddressAlpha2Code.equals(companyAlpha2Code);
    boolean isAddressWrong = isStockLocationAddressWrong || isCompanyAddressWrong;
    boolean isIncotermRequiredAndEmpty =
        isIncotermEnabled && incoterm == null && anyStorableProduct && isAddressWrong;

    if (isIncotermRequiredAndEmpty) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_CONFIRM_INCOTERM_REQUIRED));
    }
  }
}
