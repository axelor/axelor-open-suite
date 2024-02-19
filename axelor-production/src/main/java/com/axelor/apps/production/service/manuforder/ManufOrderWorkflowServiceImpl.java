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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.service.TemplateMessageService;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderWorkflowServiceImpl implements ManufOrderWorkflowService {
  protected OperationOrderWorkflowService operationOrderWorkflowService;
  protected OperationOrderRepository operationOrderRepo;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufOrderRepository manufOrderRepo;
  protected ProductCompanyService productCompanyService;
  protected ProductionConfigRepository productionConfigRepo;
  protected PurchaseOrderService purchaseOrderService;
  protected AppBaseService appBaseService;
  protected ProductService productService;

  @Inject
  public ManufOrderWorkflowServiceImpl(
      OperationOrderWorkflowService operationOrderWorkflowService,
      OperationOrderRepository operationOrderRepo,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderRepository manufOrderRepo,
      ProductCompanyService productCompanyService,
      ProductionConfigRepository productionConfigRepo,
      PurchaseOrderService purchaseOrderService,
      AppBaseService appBaseService,
      ProductService productService) {
    this.operationOrderWorkflowService = operationOrderWorkflowService;
    this.operationOrderRepo = operationOrderRepo;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.manufOrderRepo = manufOrderRepo;
    this.productCompanyService = productCompanyService;
    this.productionConfigRepo = productionConfigRepo;
    this.purchaseOrderService = purchaseOrderService;
    this.appBaseService = appBaseService;
    this.productService = productService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {
    List<ManufOrder> manufOrderList = new ArrayList<>();
    manufOrderList.add(manufOrder);
    plan(manufOrderList);
    return manufOrderRepo.save(manufOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<ManufOrder> plan(List<ManufOrder> manufOrderList) throws AxelorException {
    return plan(manufOrderList, true);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<ManufOrder> plan(List<ManufOrder> manufOrderList, boolean quickSolve)
      throws AxelorException {
    ManufOrderService manufOrderService = Beans.get(ManufOrderService.class);
    SequenceService sequenceService = Beans.get(SequenceService.class);

    for (ManufOrder manufOrder : manufOrderList) {
      if (manufOrder.getBillOfMaterial().getStatusSelect()
              != BillOfMaterialRepository.STATUS_APPLICABLE
          || manufOrder.getProdProcess().getStatusSelect()
              != ProdProcessRepository.STATUS_APPLICABLE) {
        throw new AxelorException(
            manufOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.CHECK_BOM_AND_PROD_PROCESS));
      }

      if (sequenceService.isEmptyOrDraftSequenceNumber(manufOrder.getManufOrderSeq())) {
        manufOrder.setManufOrderSeq(manufOrderService.getManufOrderSeq(manufOrder));
      }
      manufOrderService.createBarcode(manufOrder);
      if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
        manufOrderService.preFillOperations(manufOrder);
      } else {
        manufOrderService.updateOperationsName(manufOrder);
      }
      if (!manufOrder.getIsConsProOnOperation()
          && CollectionUtils.isEmpty(manufOrder.getToConsumeProdProductList())) {
        manufOrderService.createToConsumeProdProductList(manufOrder);
      }

      if (CollectionUtils.isEmpty(manufOrder.getToProduceProdProductList())) {
        manufOrderService.createToProduceProdProductList(manufOrder);
      }

      if (manufOrder.getPlannedStartDateT() == null && manufOrder.getPlannedEndDateT() == null) {
        manufOrder.setPlannedStartDateT(
            Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
      } else if (manufOrder.getPlannedStartDateT() == null
          && manufOrder.getPlannedEndDateT() != null) {
        long duration = 0;
        for (OperationOrder order : manufOrder.getOperationOrderList()) {
          duration +=
              operationOrderWorkflowService.computeEntireCycleDuration(
                  order, order.getManufOrder().getQty()); // in seconds
        }
        manufOrder.setPlannedStartDateT(manufOrder.getPlannedEndDateT().minusSeconds(duration));
      }
      manufOrder.setRealStartDateT(null);
      manufOrder.setRealEndDateT(null);
    }

    for (ManufOrder manufOrder : manufOrderList) {
      if (manufOrder.getOperationOrderList() != null) {
        for (OperationOrder operationOrder : getSortedOperationOrderList(manufOrder)) {
          operationOrderWorkflowService.plan(operationOrder, null);
        }
      }
    }

    for (ManufOrder manufOrder : manufOrderList) {
      if (manufOrder.getPlannedEndDateT() == null) {
        manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));
      }

      if (manufOrder.getBillOfMaterial() != null) {
        manufOrder.setUnit(manufOrder.getBillOfMaterial().getUnit());
      }

      if (!manufOrder.getIsConsProOnOperation()) {
        manufOrderStockMoveService.createToConsumeStockMove(manufOrder);
      }

      manufOrderStockMoveService.createToProduceStockMove(manufOrder);
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_PLANNED);
      manufOrder.setCancelReason(null);
      manufOrder.setCancelReasonStr(null);

      manufOrderRepo.save(manufOrder);
      Beans.get(ProductionOrderService.class).updateStatus(manufOrder.getProductionOrderSet());
    }
    return manufOrderList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void start(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getBillOfMaterial().getStatusSelect()
            != BillOfMaterialRepository.STATUS_APPLICABLE
        || manufOrder.getProdProcess().getStatusSelect()
            != ProdProcessRepository.STATUS_APPLICABLE) {
      throw new AxelorException(
          manufOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CHECK_BOM_AND_PROD_PROCESS));
    }

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
  @Transactional
  public void pause(ManufOrder manufOrder) {
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
  @Transactional
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

    manufOrderStockMoveService.finish(manufOrder);
    manufOrder.setRealEndDateT(
        Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_FINISHED);
    manufOrder.setEndTimeDifference(
        new BigDecimal(
            ChronoUnit.MINUTES.between(
                manufOrder.getPlannedEndDateT(), manufOrder.getRealEndDateT())));
    manufOrderRepo.save(manufOrder);

    updateProductCostPrice(manufOrder, product, company);

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
      return manufOrder.getCostPrice().divide(qty, scale, BigDecimal.ROUND_HALF_UP);
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
    Beans.get(CostSheetService.class)
        .computeCostPrice(
            manufOrder,
            CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION,
            Beans.get(AppBaseService.class).getTodayDate(manufOrder.getCompany()));
    manufOrderStockMoveService.partialFinish(manufOrder);
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
  public LocalDateTime computePlannedStartDateT(ManufOrder manufOrder) {

    OperationOrder firstOperationOrder = getFirstOperationOrder(manufOrder);

    if (firstOperationOrder != null) {

      return firstOperationOrder.getPlannedStartDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  @Override
  public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder) {

    OperationOrder lastOperationOrder = getLastOperationOrder(manufOrder);

    if (lastOperationOrder != null) {

      return lastOperationOrder.getPlannedEndDateT();
    }

    return manufOrder.getPlannedStartDateT();
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

  /**
   * Returns first operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return First operation order of {@code manufOrder}
   */
  @Override
  public OperationOrder getFirstOperationOrder(ManufOrder manufOrder) {
    return operationOrderRepo
        .all()
        .filter("self.manufOrder = ? AND self.plannedStartDateT IS NOT NULL", manufOrder)
        .order("plannedStartDateT")
        .fetchOne();
  }

  /**
   * Returns last operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return Last operation order of {@code manufOrder}
   */
  @Override
  public OperationOrder getLastOperationOrder(ManufOrder manufOrder) {
    return operationOrderRepo
        .all()
        .filter("self.manufOrder = ? AND self.plannedEndDateT IS NOT NULL", manufOrder)
        .order("-plannedEndDateT")
        .fetchOne();
  }

  /**
   * Update planned dates.
   *
   * @param manufOrder
   * @param plannedStartDateT
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePlannedDates(ManufOrder manufOrder, LocalDateTime plannedStartDateT)
      throws AxelorException {
    manufOrder.setPlannedStartDateT(plannedStartDateT);

    if (manufOrder.getOperationOrderList() != null) {
      List<OperationOrder> operationOrderList = getSortedOperationOrderList(manufOrder);
      operationOrderWorkflowService.resetPlannedDates(operationOrderList);

      for (OperationOrder operationOrder : operationOrderList) {
        operationOrderWorkflowService.replan(operationOrder);
      }
    }

    manufOrder.setPlannedEndDateT(computePlannedEndDateT(manufOrder));
  }

  /**
   * Get a list of operation orders sorted by priority and id from the specified manufacturing
   * order.
   *
   * @param manufOrder
   * @return
   */
  protected List<OperationOrder> getSortedOperationOrderList(ManufOrder manufOrder) {
    List<OperationOrder> operationOrderList =
        MoreObjects.firstNonNull(manufOrder.getOperationOrderList(), Collections.emptyList());
    Comparator<OperationOrder> byPriority =
        Comparator.comparing(
            OperationOrder::getPriority, Comparator.nullsFirst(Comparator.naturalOrder()));
    Comparator<OperationOrder> byId =
        Comparator.comparing(
            OperationOrder::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    return operationOrderList.stream()
        .sorted(byPriority.thenComparing(byId))
        .collect(Collectors.toList());
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

  protected void createPurchaseOrderLineProduction(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder) throws AxelorException {

    UnitConversionService unitConversionService = Beans.get(UnitConversionService.class);
    PurchaseOrderLineService purchaseOrderLineService = Beans.get(PurchaseOrderLineService.class);
    PurchaseOrderLine purchaseOrderLine;
    BigDecimal quantity;
    Unit startUnit = appBaseService.getAppBase().getUnitHours();

    if (startUnit == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(ProductionExceptionMessage.PURCHASE_ORDER_NO_HOURS_UNIT));
    }

    for (ProdHumanResource humanResource : operationOrder.getProdHumanResourceList()) {

      Product product = humanResource.getProduct();
      Unit purchaseUnit = product.getPurchasesUnit();
      Unit stockUnit = product.getUnit();

      Unit endUnit = (purchaseUnit != null) ? purchaseUnit : stockUnit;

      if (endUnit == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(ProductionExceptionMessage.PURCHASE_ORDER_NO_END_UNIT));
      }
      final int COMPUTATION_SCALE = 20;
      quantity =
          unitConversionService.convert(
              startUnit,
              endUnit,
              new BigDecimal(humanResource.getDuration())
                  .divide(BigDecimal.valueOf(3600), COMPUTATION_SCALE, RoundingMode.HALF_UP),
              appBaseService.getNbDecimalDigitForQty(),
              humanResource.getProduct());
      // have to force the scale as the conversion service will not round if the start unit and the
      // end unit are equals.
      quantity = quantity.setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);

      purchaseOrderLine =
          purchaseOrderLineService.createPurchaseOrderLine(
              purchaseOrder, product, null, null, quantity, purchaseUnit);

      purchaseOrder.getPurchaseOrderLineList().add(purchaseOrderLine);
    }
  }

  protected PurchaseOrder setPurchaseOrderSupplierDetails(PurchaseOrder purchaseOrder)
      throws AxelorException {
    Partner supplierPartner = purchaseOrder.getSupplierPartner();

    if (supplierPartner != null) {
      purchaseOrder.setCurrency(supplierPartner.getCurrency());
      purchaseOrder.setShipmentMode(supplierPartner.getShipmentMode());
      purchaseOrder.setFreightCarrierMode(supplierPartner.getFreightCarrierMode());
      purchaseOrder.setNotes(supplierPartner.getPurchaseOrderComments());

      if (supplierPartner.getPaymentCondition() != null) {
        purchaseOrder.setPaymentCondition(supplierPartner.getPaymentCondition());
      } else {
        purchaseOrder.setPaymentCondition(
            purchaseOrder.getCompany().getAccountConfig().getDefPaymentCondition());
      }

      if (supplierPartner.getOutPaymentMode() != null) {
        purchaseOrder.setPaymentMode(supplierPartner.getOutPaymentMode());
      } else {
        purchaseOrder.setPaymentMode(
            purchaseOrder.getCompany().getAccountConfig().getOutPaymentMode());
      }

      if (supplierPartner.getContactPartnerSet().size() == 1) {
        purchaseOrder.setContactPartner(supplierPartner.getContactPartnerSet().iterator().next());
      }

      purchaseOrder.setCompanyBankDetails(
          Beans.get(BankDetailsService.class)
              .getDefaultCompanyBankDetails(
                  purchaseOrder.getCompany(),
                  purchaseOrder.getPaymentMode(),
                  purchaseOrder.getSupplierPartner(),
                  null));

      purchaseOrder.setPriceList(
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(
                  purchaseOrder.getSupplierPartner(), PriceListRepository.TYPE_PURCHASE));

      if (Beans.get(AppSupplychainService.class).isApp("supplychain")
          && Beans.get(AppSupplychainService.class).getAppSupplychain().getIntercoFromPurchase()
          && !purchaseOrder.getCreatedByInterco()
          && (Beans.get(CompanyRepository.class)
                  .all()
                  .filter("self.partner = ?", supplierPartner)
                  .fetchOne()
              != null)) {
        purchaseOrder.setInterco(true);
      }
    }

    return purchaseOrder;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createPurchaseOrder(ManufOrder manufOrder) throws AxelorException {

    PurchaseOrder purchaseOrder =
        purchaseOrderService.createPurchaseOrder(
            null,
            manufOrder.getCompany(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            manufOrder.getProdProcess().getSubcontractor(),
            null);

    purchaseOrder.setOutsourcingOrder(true);

    if (manufOrder.getCompany() != null && manufOrder.getCompany().getStockConfig() != null) {
      purchaseOrder.setStockLocation(
          manufOrder.getCompany().getStockConfig().getOutsourcingReceiptStockLocation());
    }

    this.setPurchaseOrderSupplierDetails(purchaseOrder);

    for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
      if (operationOrder.getUseLineInGeneratedPurchaseOrder()) {
        this.createPurchaseOrderLineProduction(operationOrder, purchaseOrder);
      }
    }

    purchaseOrderService.computePurchaseOrder(purchaseOrder);
    manufOrder.setPurchaseOrder(purchaseOrder);

    manufOrderRepo.save(manufOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String planManufOrders(List<ManufOrder> manufOrderList) throws AxelorException {

    StringBuilder messageBuilder = new StringBuilder();

    for (ManufOrder manufOrder : manufOrderList) {
      this.plan(manufOrder);
      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrder())) {
        messageBuilder.append(manufOrder.getMoCommentFromSaleOrder());
      }

      if (Boolean.TRUE.equals(manufOrder.getProdProcess().getGeneratePurchaseOrderOnMoPlanning())) {
        this.createPurchaseOrder(manufOrder);
      }

      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrderLine())) {
        messageBuilder
            .append(System.lineSeparator())
            .append(manufOrder.getMoCommentFromSaleOrderLine());
      }
    }
    return messageBuilder.toString();
  }
}
