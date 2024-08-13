package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderCheckServiceImpl implements SaleOrderCheckService {

  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderCheckServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public String finalizeCheckAlert(SaleOrder saleOrder) {
    if (productSoldAtLoss(saleOrder)) {
      return I18n.get(SaleExceptionMessage.SALE_ORDER_FINALIZE_PRODUCT_SOLD_AT_LOSS);
    }

    if (priceListIsNotValid(saleOrder)) {
      return I18n.get(SaleExceptionMessage.SALE_ORDER_FINALIZE_PRICE_LIST_NOT_VALID);
    }

    return "";
  }

  @Override
  public String confirmCheckAlert(SaleOrder saleOrder) throws AxelorException {
    if (isTotalAmountZero(saleOrder)) {
      return I18n.get(SaleExceptionMessage.SALE_ORDER_CONFIRM_TOTAL_AMOUNT_ZERO);
    }
    return "";
  }

  protected boolean isTotalAmountZero(SaleOrder saleOrder) {
    return saleOrder.getExTaxTotal().signum() == 0
        && CollectionUtils.isNotEmpty(saleOrder.getSaleOrderLineList());
  }

  @Override
  public void checkSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_FINALIZE_EMPTY_LIST));
    }
  }

  @Override
  public boolean productSoldAtLoss(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    return saleOrderLineList.stream().anyMatch(line -> line.getSubTotalGrossMargin().signum() < 0);
  }

  @Override
  public boolean priceListIsNotValid(SaleOrder saleOrder) {
    PriceList priceList = saleOrder.getPriceList();
    if (priceList == null) {
      return false;
    }
    LocalDate todayDate = appBaseService.getTodayDate(null);
    LocalDate priceListBeginDate = priceList.getApplicationBeginDate();
    LocalDate priceListEndDate = priceList.getApplicationEndDate();

    boolean beginDateNotValid =
        priceListBeginDate == null || priceListBeginDate.isBefore(todayDate);
    boolean endDateNotValid = priceListEndDate == null || priceListEndDate.isAfter(todayDate);
    return beginDateNotValid && endDateNotValid;
  }
}
