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
package com.axelor.apps.supplychain.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
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
  protected AccountingSituationSupplychainService accountingSituationSupplychainService;

  @Inject
  public SaleOrderConfirmSupplychainServiceImpl(
      AppSupplychainService appSupplychainService,
      AnalyticToolSupplychainService analyticToolSupplychainService,
      PartnerSupplychainService partnerSupplychainService,
      SaleOrderPurchaseService saleOrderPurchaseService,
      SaleOrderStockService saleOrderStockService,
      IntercoService intercoService,
      StockMoveRepository stockMoveRepository,
      AccountingSituationSupplychainService accountingSituationSupplychainService) {
    this.appSupplychainService = appSupplychainService;
    this.analyticToolSupplychainService = analyticToolSupplychainService;
    this.partnerSupplychainService = partnerSupplychainService;
    this.saleOrderPurchaseService = saleOrderPurchaseService;
    this.saleOrderStockService = saleOrderStockService;
    this.intercoService = intercoService;
    this.stockMoveRepository = stockMoveRepository;
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
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

    accountingSituationSupplychainService.updateCustomerCreditFromSaleOrder(saleOrder);

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
