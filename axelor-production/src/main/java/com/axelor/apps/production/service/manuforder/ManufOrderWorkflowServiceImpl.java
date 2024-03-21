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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.AxelorMessageException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.operationorder.OperationOrderOutsourceService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.service.TemplateMessageService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ManufOrderWorkflowServiceImpl implements ManufOrderWorkflowService {
  protected OperationOrderWorkflowService operationOrderWorkflowService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufOrderRepository manufOrderRepo;
  protected ProductCompanyService productCompanyService;
  protected ProductionConfigRepository productionConfigRepo;
  protected AppBaseService appBaseService;
  protected OperationOrderService operationOrderService;
  protected AppProductionService appProductionService;
  protected ProductionConfigService productionConfigService;
  protected ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService;
  protected ManufOrderService manufOrderService;
  protected SequenceService sequenceService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;
  protected OperationOrderOutsourceService operationOrderOutsourceService;
  protected ProductService productService;

  @Inject
  public ManufOrderWorkflowServiceImpl(
      OperationOrderWorkflowService operationOrderWorkflowService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderRepository manufOrderRepo,
      ProductCompanyService productCompanyService,
      ProductionConfigRepository productionConfigRepo,
      AppBaseService appBaseService,
      OperationOrderService operationOrderService,
      AppProductionService appProductionService,
      ProductionConfigService productionConfigService,
      ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService,
      ManufOrderService manufOrderService,
      SequenceService sequenceService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      OperationOrderOutsourceService operationOrderOutsourceService,
      ProductService productService) {
    this.operationOrderWorkflowService = operationOrderWorkflowService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.manufOrderRepo = manufOrderRepo;
    this.productCompanyService = productCompanyService;
    this.productionConfigRepo = productionConfigRepo;
    this.appBaseService = appBaseService;
    this.operationOrderService = operationOrderService;
    this.appProductionService = appProductionService;
    this.productionConfigService = productionConfigService;
    this.manufOrderOutgoingStockMoveService = manufOrderOutgoingStockMoveService;
    this.manufOrderService = manufOrderService;
    this.sequenceService = sequenceService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
    this.operationOrderOutsourceService = operationOrderOutsourceService;
    this.productService = productService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void start(ManufOrder manufOrder) throws AxelorException {

    manufOrderService.checkApplicableManufOrder(manufOrder);

    manufOrder.setRealStartDateT(
        Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());

    int beforeOrAfterConfig = manufOrder.getProdProcess().getStockMoveRealizeOrderSelect();
    if (beforeOrAfterConfig == ProductionConfigRepository.REALIZE_START) {
      for (StockMove stockMove : manufOrder.getInStockMoveList()) {
        manufOrderStockMoveService.finishStockMove(stockMove);
      }
    }
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);
    manufOrderRepo.save(manufOrder);
    Beans.get(ProductionOrderService.class).updateStatus(manufOrder.getProductionOrderSet());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void pause(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS) {
          operationOrderWorkflowService.pause(operationOrder);
        }
      }
    }

    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_STANDBY);
    manufOrderRepo.save(manufOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void resume(ManufOrder manufOrder) {
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_STANDBY) {
          operationOrderWorkflowService.resume(operationOrder);
        }
      }
    }

    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);
    manufOrderRepo.save(manufOrder);
  }

  /**
   * CAUTION : This method can not be called from a Transactional method or the mail sending method
   * could not work correctly.
   */
  @Override
  public boolean finish(ManufOrder manufOrder) throws AxelorException {
    finishManufOrder(manufOrder);
    return sendFinishedMail(manufOrder);
  }

  @Override
  public boolean sendFinishedMail(ManufOrder manufOrder) {
    ProductionConfig productionConfig =
        manufOrder.getCompany() != null
            ? productionConfigRepo.findByCompany(manufOrder.getCompany())
            : null;
    if (productionConfig != null && productionConfig.getFinishMoAutomaticEmail()) {
      return this.sendMail(manufOrder, productionConfig.getFinishMoMessageTemplate());
    }
    return true;
  }

  /** CAUTION : Must be called in a different transaction from sending mail method. */
  @Transactional(rollbackOn = {Exception.class})
  public void finishManufOrder(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_FINISHED) {
          if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_IN_PROGRESS
              && operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_STANDBY) {
            operationOrderWorkflowService.start(operationOrder);
          }
          operationOrderWorkflowService.finish(operationOrder);
        }
      }
    }

    manufOrderStockMoveService.finish(manufOrder);

    // create cost sheet
    Beans.get(CostSheetService.class)
        .computeCostPrice(
            manufOrder,
            CostSheetRepository.CALCULATION_END_OF_PRODUCTION,
            Beans.get(AppBaseService.class).getTodayDate(manufOrder.getCompany()));

    // update price in product
    Product product = manufOrder.getProduct();
    Company company = manufOrder.getCompany();
    if (((Integer) productCompanyService.get(product, "realOrEstimatedPriceSelect", company))
        == ProductRepository.PRICE_METHOD_FORECAST) {
      productCompanyService.set(
          product, "lastProductionPrice", manufOrder.getBillOfMaterial().getCostPrice(), company);
    } else if (((Integer) productCompanyService.get(product, "realOrEstimatedPriceSelect", company))
        == ProductRepository.PRICE_METHOD_REAL) {
      BigDecimal costPrice = computeOneUnitProductionPrice(manufOrder);
      if (costPrice.signum() != 0) {
        productCompanyService.set(product, "lastProductionPrice", costPrice, company);
      }
    } else {
      // default value is forecast
      productCompanyService.set(
          product, "realOrEstimatedPriceSelect", ProductRepository.PRICE_METHOD_FORECAST, company);
      productCompanyService.set(
          product, "lastProductionPrice", manufOrder.getBillOfMaterial().getCostPrice(), company);
    }

    manufOrder.setRealEndDateT(
        Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_FINISHED);
    manufOrder.setEndTimeDifference(
        new BigDecimal(
            ChronoUnit.MINUTES.between(
                manufOrder.getPlannedEndDateT(), manufOrder.getRealEndDateT())));
    manufOrderRepo.save(manufOrder);

    updateProductCostPrice(manufOrder, product, company);

    manufOrderOutgoingStockMoveService.setManufOrderOnOutgoingMove(manufOrder);

    Beans.get(ProductionOrderService.class).updateStatus(manufOrder.getProductionOrderSet());
  }

  protected void updateProductCostPrice(ManufOrder manufOrder, Product product, Company company)
      throws AxelorException {
    // update costprice in product
    if (((Integer) productCompanyService.get(product, "costTypeSelect", company))
        == ProductRepository.COST_TYPE_LAST_PRODUCTION_PRICE) {
      productCompanyService.set(product, "costPrice", manufOrder.getCostPrice(), company);
      if ((Boolean) productCompanyService.get(product, "autoUpdateSalePrice", company)) {
        productService.updateSalePrice(product, company);
      }
    }
  }

  /** Return the cost price for one unit in a manufacturing order. */
  protected BigDecimal computeOneUnitProductionPrice(ManufOrder manufOrder) {
    BigDecimal qty = manufOrder.getQty();
    if (qty.signum() != 0) {
      int scale = Beans.get(AppProductionService.class).getNbDecimalDigitForUnitPrice();
      return manufOrder.getCostPrice().divide(qty, scale, RoundingMode.HALF_UP);
    } else {
      return BigDecimal.ZERO;
    }
  }

  /**
   * Allows to finish partially a manufacturing order, by realizing current stock move and planning
   * the difference with the planned prodproducts.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean partialFinish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_PLANNED) {
          operationOrderWorkflowService.start(operationOrder);
        }
      }
    }
    manufOrderStockMoveService.partialFinish(manufOrder);
    Beans.get(CostSheetService.class)
        .computeCostPrice(
            manufOrder,
            CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION,
            Beans.get(AppBaseService.class).getTodayDate(manufOrder.getCompany()));
    return sendPartialFinishMail(manufOrder);
  }

  public boolean sendPartialFinishMail(ManufOrder manufOrder) {
    ProductionConfig productionConfig =
        manufOrder.getCompany() != null
            ? productionConfigRepo.findByCompany(manufOrder.getCompany())
            : null;
    if (productionConfig != null && productionConfig.getPartFinishMoAutomaticEmail()) {
      return this.sendMail(manufOrder, productionConfig.getPartFinishMoMessageTemplate());
    }
    return true;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(ManufOrder manufOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException {
    if (cancelReason == null
        && manufOrder.getStatusSelect() != ManufOrderRepository.STATUS_DRAFT
        && manufOrder.getStatusSelect() != ManufOrderRepository.STATUS_PLANNED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_CANCEL_REASON_ERROR));
    }
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_CANCELED) {
          operationOrderWorkflowService.cancel(operationOrder);
        }
      }
    }

    manufOrderStockMoveService.cancel(manufOrder);

    if (manufOrder.getConsumedStockMoveLineList() != null) {
      manufOrder
          .getConsumedStockMoveLineList()
          .forEach(stockMoveLine -> stockMoveLine.setConsumedManufOrder(null));
    }
    if (manufOrder.getProducedStockMoveLineList() != null) {
      manufOrder
          .getProducedStockMoveLineList()
          .forEach(stockMoveLine -> stockMoveLine.setProducedManufOrder(null));
    }
    if (manufOrder.getDiffConsumeProdProductList() != null) {
      manufOrder.clearDiffConsumeProdProductList();
    }

    cancelProductReservationFromManufOrder(manufOrder);
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_CANCELED);
    if (cancelReason != null) {
      manufOrder.setCancelReason(cancelReason);
      if (Strings.isNullOrEmpty(cancelReasonStr)) {
        manufOrder.setCancelReasonStr(cancelReason.getName());
      } else {
        manufOrder.setCancelReasonStr(cancelReasonStr);
      }
    }
    manufOrderRepo.save(manufOrder);
    Beans.get(ProductionOrderService.class).updateStatus(manufOrder.getProductionOrderSet());
  }

  @Override
  public void allOpFinished(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getOperationOrderList().stream()
        .allMatch(
            operationOrder ->
                operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED)) {
      this.finishManufOrder(manufOrder);
    }
  }

  protected boolean sendMail(ManufOrder manufOrder, Template template) {
    if (template == null) {
      TraceBackService.trace(
          new AxelorMessageException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(ProductionExceptionMessage.MANUF_ORDER_MISSING_TEMPLATE)));
    }
    if (Beans.get(EmailAccountRepository.class)
            .all()
            .filter("self.isDefault = true AND self.isValid = true")
            .fetchOne()
        == null) {
      return false;
    }
    try {
      Beans.get(TemplateMessageService.class).generateAndSendMessage(manufOrder, template);
    } catch (Exception e) {
      TraceBackService.trace(
          new AxelorMessageException(
              e, manufOrder, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR));
    }
    return true;
  }

  @Override
  public List<Partner> getOutsourcePartners(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getOutsourcing()
        && manufOrderOutsourceService.getOutsourcePartner(manufOrder).isPresent()) {
      return List.of(manufOrderOutsourceService.getOutsourcePartner(manufOrder).get());
    } else {
      return manufOrder.getOperationOrderList().stream()
          .filter(OperationOrder::getOutsourcing)
          .map(oo -> operationOrderOutsourceService.getOutsourcePartner(oo))
          .map(optPartner -> optPartner.orElse(null))
          .filter(Objects::nonNull)
          .distinct()
          .collect(Collectors.toList());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setOperationOrderMaxPriority(ManufOrder manufOrder) {

    if (manufOrder == null
        || Boolean.FALSE.equals(manufOrder.getProdProcess().getOperationContinuity())) {
      return;
    }

    manufOrder = manufOrderRepo.find(manufOrder.getId());

    List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
    int optionalOperationOrderLargestPriority =
        operationOrderList.stream()
            .filter(order -> isValidOperationOrder(order, true))
            .mapToInt(OperationOrder::getPriority)
            .max()
            .orElse(-1);

    int operationOrderMaxPriority =
        operationOrderList.stream()
            .filter(order -> isValidOperationOrder(order, false))
            .mapToInt(OperationOrder::getPriority)
            .findFirst()
            .orElse(optionalOperationOrderLargestPriority + 1);

    manufOrder.setOperationOrderMaxPriority(operationOrderMaxPriority);

    manufOrderRepo.save(manufOrder);
  }

  protected boolean isValidOperationOrder(OperationOrder order, boolean optional) {
    Integer statusSelect = order.getStatusSelect();
    ProdProcessLine prodProcessLine = order.getProdProcessLine();
    return !statusSelect.equals(ManufOrderRepository.STATUS_FINISHED)
        && !statusSelect.equals(ManufOrderRepository.STATUS_CANCELED)
        && ObjectUtils.notEmpty(prodProcessLine)
        && prodProcessLine.getOptional() == optional;
  }

  protected void cancelProductReservationFromManufOrder(ManufOrder manufOrder) {
    ProductReservationService productReservationService =
        Beans.get(ProductReservationService.class);
    ProductReservationRepository productReservationRepository =
        Beans.get(ProductReservationRepository.class);
    Query<ProductReservation> query = productReservationRepository.findByManufOrder(manufOrder);
    if (query.count() != 0) {
      query.fetch().forEach(productReservationService::cancelReservation);
    }
  }
}
