package com.axelor.apps.supplychain.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.analytic.AnalyticToolSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderConfirmSupplychainServiceImpl implements SaleOrderConfirmSupplychainService {

  protected AppSupplychainService appSupplychainService;
  protected AnalyticToolSupplychainService analyticToolSupplychainService;
  protected PartnerSupplychainService partnerSupplychainService;
  protected SaleOrderPurchaseService saleOrderPurchaseService;
  protected SaleOrderStockService saleOrderStockService;
  protected IntercoService intercoService;
  protected StockMoveRepository stockMoveRepository;

  @Inject
  public SaleOrderConfirmSupplychainServiceImpl(
      AppSupplychainService appSupplychainService,
      AnalyticToolSupplychainService analyticToolSupplychainService,
      PartnerSupplychainService partnerSupplychainService,
      SaleOrderPurchaseService saleOrderPurchaseService,
      SaleOrderStockService saleOrderStockService,
      IntercoService intercoService,
      StockMoveRepository stockMoveRepository) {
    this.appSupplychainService = appSupplychainService;
    this.analyticToolSupplychainService = analyticToolSupplychainService;
    this.partnerSupplychainService = partnerSupplychainService;
    this.saleOrderPurchaseService = saleOrderPurchaseService;
    this.saleOrderStockService = saleOrderStockService;
    this.intercoService = intercoService;
    this.stockMoveRepository = stockMoveRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String confirmProcess(SaleOrder saleOrder) throws AxelorException {

    if (!appSupplychainService.isApp("supplychain")) {
      return "";
    }

    analyticToolSupplychainService.checkSaleOrderLinesAnalyticDistribution(saleOrder);

    if (partnerSupplychainService.isBlockedPartnerOrParent(saleOrder.getClientPartner())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.CUSTOMER_HAS_BLOCKED_ACCOUNT));
    }

    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();

    if (appSupplychain.getPurchaseOrderGenerationAuto()) {
      saleOrderPurchaseService.createPurchaseOrders(saleOrder);
    }
    if (appSupplychain.getCustomerStockMoveGenerationAuto()) {
      saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
    }
    int intercoSaleCreatingStatus = appSupplychain.getIntercoSaleCreatingStatusSelect();
    if (saleOrder.getInterco()
        && intercoSaleCreatingStatus == SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      intercoService.generateIntercoPurchaseFromSale(saleOrder);
    }

    String notifyMessage = getStockMoveGeneratedNotifyMessage(saleOrder, appSupplychain);
    if (StringUtils.notEmpty(notifyMessage)) {
      return notifyMessage;
    }

    return "";
  }

  protected String getStockMoveGeneratedNotifyMessage(
      SaleOrder saleOrder, AppSupplychain appSupplychain) {
    if (appSupplychain.getCustomerStockMoveGenerationAuto()) {
      StockMove stockMove =
          stockMoveRepository
              .all()
              .filter(
                  ":saleOrderId MEMBER OF self.saleOrderSet AND self.statusSelect = :statusSelect")
              .bind("saleOrderId", saleOrder.getId())
              .bind("statusSelect", StockMoveRepository.STATUS_PLANNED)
              .fetchOne();
      if (stockMove != null) {
        return String.format(
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_STOCK_MOVE_CREATED),
            stockMove.getStockMoveSeq());
      }
    }
    return "";
  }
}
