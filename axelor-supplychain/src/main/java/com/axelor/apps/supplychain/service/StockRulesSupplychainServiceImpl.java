/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import static com.axelor.apps.supplychain.exception.SupplychainExceptionMessage.COULD_NOT_FIND_ELIGIBLE_EMAIL_TEMPLATE_FOR_STOCK_RULES;
import static com.axelor.apps.supplychain.exception.SupplychainExceptionMessage.COULD_NOT_FIND_RECIPIENTS_FOR_MESSAGE_GENERATED_FROM_STOCK_RULES;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.TemplateRepository;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StockRulesSupplychainServiceImpl implements StockRulesSupplychainService {

  protected StockRulesService stockRulesService;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected TemplateRepository templateRepo;
  protected TemplateMessageService templateMessageService;
  protected StockConfigRepository stockConfigRepo;
  protected AppPurchaseService appPurchaseService;
  protected PurchaseOrderService purchaseOrderService;
  protected AppSupplychainService appSupplychainService;
  protected AppBaseService appBaseService;
  protected PurchaseOrderSupplychainService purchaseOrderSupplychainService;
  protected PartnerPriceListService partnerPriceListService;

  @Inject
  public StockRulesSupplychainServiceImpl(
      StockRulesService stockRulesService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      TemplateRepository templateRepo,
      TemplateMessageService templateMessageService,
      StockConfigRepository stockConfigRepo,
      AppPurchaseService appPurchaseService,
      PurchaseOrderService purchaseOrderService,
      AppSupplychainService appSupplychainService,
      AppBaseService appBaseService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PartnerPriceListService partnerPriceListService) {
    this.stockRulesService = stockRulesService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.templateRepo = templateRepo;
    this.templateMessageService = templateMessageService;
    this.stockConfigRepo = stockConfigRepo;
    this.appPurchaseService = appPurchaseService;
    this.purchaseOrderService = purchaseOrderService;
    this.appSupplychainService = appSupplychainService;
    this.appBaseService = appBaseService;
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
    this.partnerPriceListService = partnerPriceListService;
  }

  @Override
  public void processNonCompliantStockLocationLine(
      StockRules stockRules, StockLocationLine stockLocationLine) throws AxelorException {

    if (stockRules == null) {
      return;
    }

    if (stockRules.getOrderAlertSelect().equals(StockRulesRepository.ORDER_ALERT_ALERT)) {
      this.generateAndSendMessage(stockRules);
      return;
    }

    StockLocation stockLocation = stockLocationLine.getStockLocation();

    if (stockLocation == null
        || !appSupplychainService.isApp("supplychain")
        || !stockRules
            .getOrderAlertSelect()
            .equals(StockRulesRepository.ORDER_ALERT_PURCHASE_ORDER)) {
      return;
    }

    Product product = stockLocationLine.getProduct();

    BigDecimal minReorderQty = getDefaultSupplierMinQty(product);
    BigDecimal qtyToOrder =
        stockRulesService.getQtyToOrder(stockLocationLine, stockRules, minReorderQty);
    Partner supplierPartner = product.getDefaultSupplierPartner();

    if (supplierPartner != null) {

      Company company = stockLocation.getCompany();
      LocalDate today = appBaseService.getTodayDate(company);
      PurchaseOrder purchaseOrder =
          purchaseOrderRepo.save(
              purchaseOrderSupplychainService.createPurchaseOrder(
                  AuthUtils.getUser(),
                  company,
                  null,
                  supplierPartner.getCurrency(),
                  today.plusDays(supplierPartner.getDeliveryDelay()),
                  stockRules.getName(),
                  null,
                  stockLocation,
                  today,
                  partnerPriceListService.getDefaultPriceList(
                      supplierPartner, PriceListRepository.TYPE_PURCHASE),
                  supplierPartner,
                  null));

      purchaseOrder.addPurchaseOrderLineListItem(
          purchaseOrderLineService.createPurchaseOrderLine(
              purchaseOrder, product, null, null, qtyToOrder, product.getUnit()));

      purchaseOrderService.computePurchaseOrder(purchaseOrder);

      purchaseOrderRepo.save(purchaseOrder);

      if (stockRules.getAlert()) {
        generateAndSendMessage(stockRules);
      }
    }
  }

  public void generateAndSendMessage(StockRules stockRules) throws AxelorException {

    Template template = stockRules.getStockRuleMessageTemplate();

    if (template == null) {
      StockConfig stockConfig =
          stockConfigRepo
              .all()
              .filter(
                  "self.company = ?1 AND self.stockRuleMessageTemplate IS NOT NULL",
                  stockRules.getStockLocation().getCompany())
              .fetchOne();
      if (stockConfig != null) {
        template = stockConfig.getStockRuleMessageTemplate();
      } else {
        template =
            templateRepo
                .all()
                .filter(
                    "self.metaModel.fullName = ?1 AND self.isSystem != true",
                    StockRules.class.getName())
                .fetchOne();
      }
    }

    if (template == null) {
      throw new AxelorException(
          stockRules,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(COULD_NOT_FIND_ELIGIBLE_EMAIL_TEMPLATE_FOR_STOCK_RULES),
          stockRules.getId());
    }

    try {
      Message message = templateMessageService.generateAndSendMessage(stockRules, template);
      if (ObjectUtils.isEmpty(message.getToEmailAddressSet())
          && ObjectUtils.isEmpty(message.getReplyToEmailAddressSet())) {
        throw new AxelorException(
            stockRules,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(COULD_NOT_FIND_RECIPIENTS_FOR_MESSAGE_GENERATED_FROM_STOCK_RULES),
            stockRules.getId());
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  /**
   * Get minimum quantity from default supplier.
   *
   * @param product
   * @return
   */
  protected BigDecimal getDefaultSupplierMinQty(Product product) {
    Partner defaultSupplierPartner = product.getDefaultSupplierPartner();
    if (appPurchaseService.getAppPurchase().getManageSupplierCatalog()) {
      List<SupplierCatalog> supplierCatalogList = product.getSupplierCatalogList();
      if (defaultSupplierPartner != null && supplierCatalogList != null) {
        for (SupplierCatalog supplierCatalog : supplierCatalogList) {
          if (supplierCatalog.getSupplierPartner().equals(defaultSupplierPartner)) {
            return supplierCatalog.getMinQty();
          }
        }
      }
    }
    return BigDecimal.ZERO;
  }
}
