package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCostPriceComputeServiceImpl
    implements SaleOrderLineCostPriceComputeService {

  protected final AppSaleService appSaleService;
  protected final ProductCompanyService productCompanyService;
  protected final CurrencyScaleService currencyScaleService;

  @Inject
  public SaleOrderLineCostPriceComputeServiceImpl(
      AppSaleService appSaleService,
      ProductCompanyService productCompanyService,
      CurrencyScaleService currencyScaleService) {
    this.appSaleService = appSaleService;
    this.productCompanyService = productCompanyService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public Map<String, Object> computeSubTotalCostPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Product product) throws AxelorException {
    Map<String, Object> map = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    BigDecimal subTotalCostPrice = BigDecimal.ZERO;
    int listDisplayTypeSelect = appSale.getListDisplayTypeSelect();
    if (listDisplayTypeSelect != AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI
        || CollectionUtils.isEmpty(saleOrderLine.getSubSaleOrderLineList())) {
      if (product != null) {
        BigDecimal productCostPrice =
            (BigDecimal)
                productCompanyService.get(
                    saleOrderLine.getProduct(), "costPrice", saleOrder.getCompany());
        if (productCostPrice.compareTo(BigDecimal.ZERO) != 0) {
          subTotalCostPrice =
              currencyScaleService
                  .getCompanyScaledValue(saleOrder, productCostPrice)
                  .multiply(saleOrderLine.getQty());
        }
      }
      saleOrderLine.setSubTotalCostPrice(subTotalCostPrice);
    }
    map.put("subTotalCostPrice", saleOrderLine.getSubTotalCostPrice());
    return map;
  }
}
