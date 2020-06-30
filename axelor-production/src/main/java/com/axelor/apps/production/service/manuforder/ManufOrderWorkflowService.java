/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderWorkflowService {
  protected OperationOrderWorkflowService operationOrderWorkflowService;
  protected OperationOrderRepository operationOrderRepo;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufOrderRepository manufOrderRepo;

  @Inject ProductionConfigRepository productionConfigRepo;
  @Inject AppBaseService appBaseService;

  @Inject
  public ManufOrderWorkflowService(
      OperationOrderWorkflowService operationOrderWorkflowService,
      OperationOrderRepository operationOrderRepo,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderRepository manufOrderRepo) {
    this.operationOrderWorkflowService = operationOrderWorkflowService;
    this.operationOrderRepo = operationOrderRepo;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.manufOrderRepo = manufOrderRepo;
  }

  @Transactional(rollbackOn = {Exception.class})
  public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {
    List<ManufOrder> manufOrderList = new ArrayList<>();
    manufOrderList.add(manufOrder);
    plan(manufOrderList);
    return manufOrderRepo.save(manufOrder);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<ManufOrder> plan(List<ManufOrder> manufOrderList) throws AxelorException {
    return plan(manufOrderList, true);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<ManufOrder> plan(List<ManufOrder> manufOrderList, boolean quickSolve)
      throws AxelorException {
    ManufOrderService manufOrderService = Beans.get(ManufOrderService.class);
    SequenceService sequenceService = Beans.get(SequenceService.class);

    for (ManufOrder manufOrder : manufOrderList) {
      if (manufOrder.getBillOfMaterial().getStatusSelect()
              != BillOfMaterialRepository.STATUS_APPLICABLE
          && manufOrder.getProdProcess().getStatusSelect()
              != ProdProcessRepository.STATUS_APPLICABLE) {
        throw new AxelorException(
            manufOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get("Bill of material and production process must be applicable"));
      }

      if (sequenceService.isEmptyOrDraftSequenceNumber(manufOrder.getManufOrderSeq())) {
        manufOrder.setManufOrderSeq(manufOrderService.getManufOrderSeq(manufOrder));
      }
      if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
        manufOrderService.preFillOperations(manufOrder);
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
    }

    for (ManufOrder manufOrder : manufOrderList) {
      if (manufOrder.getOperationOrderList() != null) {
        for (OperationOrder operationOrder : getSortedOperationOrderList(manufOrder)) {
          operationOrderWorkflowService.plan(operationOrder);
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
    }
    return manufOrderList;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void start(ManufOrder manufOrder) throws AxelorException {

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
  }

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

  @Transactional(rollbackOn = {Exception.class})
  public boolean finish(ManufOrder manufOrder) throws AxelorException {
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
            Beans.get(AppBaseService.class).getTodayDate());

    // update price in product
    Product product = manufOrder.getProduct();
    if (product.getRealOrEstimatedPriceSelect() == ProductRepository.PRICE_METHOD_FORECAST) {
      product.setLastProductionPrice(manufOrder.getBillOfMaterial().getCostPrice());
    } else if (product.getRealOrEstimatedPriceSelect() == ProductRepository.PRICE_METHOD_REAL) {
      BigDecimal costPrice = computeOneUnitProductionPrice(manufOrder);
      if (costPrice.signum() != 0) {
        product.setLastProductionPrice(costPrice);
      }
    } else {
      // default value is forecast
      product.setRealOrEstimatedPriceSelect(ProductRepository.PRICE_METHOD_FORECAST);
      product.setLastProductionPrice(manufOrder.getBillOfMaterial().getCostPrice());
    }

    // update costprice in product
    if (product.getCostTypeSelect() == ProductRepository.COST_TYPE_LAST_PRODUCTION_PRICE) {
      product.setCostPrice(product.getLastProductionPrice());
      if (product.getAutoUpdateSalePrice()) {
        Beans.get(ProductService.class).updateSalePrice(product);
      }
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
    ProductionConfig productionConfig =
        manufOrder.getCompany() != null
            ? productionConfigRepo.findByCompany(manufOrder.getCompany())
            : null;
    if (productionConfig != null && productionConfig.getFinishMoAutomaticEmail()) {
      return this.sendMail(manufOrder, productionConfig.getFinishMoMessageTemplate());
    }
    return true;
  }

  /** Return the cost price for one unit in a manufacturing order. */
  protected BigDecimal computeOneUnitProductionPrice(ManufOrder manufOrder) {
    BigDecimal qty = manufOrder.getQty();
    if (qty.signum() != 0) {
      int scale = Beans.get(AppProductionService.class).getNbDecimalDigitForUnitPrice();
      return manufOrder.getCostPrice().divide(qty, scale, BigDecimal.ROUND_HALF_EVEN);
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
            Beans.get(AppBaseService.class).getTodayDate());
    Beans.get(ManufOrderStockMoveService.class).partialFinish(manufOrder);
    ProductionConfig productionConfig =
        manufOrder.getCompany() != null
            ? productionConfigRepo.findByCompany(manufOrder.getCompany())
            : null;
    if (productionConfig != null && productionConfig.getPartFinishMoAutomaticEmail()) {
      return this.sendMail(manufOrder, productionConfig.getPartFinishMoMessageTemplate());
    }
    return true;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void cancel(ManufOrder manufOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException {
    if (cancelReason == null
        && manufOrder.getStatusSelect() != ManufOrderRepository.STATUS_DRAFT
        && manufOrder.getStatusSelect() != ManufOrderRepository.STATUS_PLANNED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MANUF_ORDER_CANCEL_REASON_ERROR));
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
  }

  public LocalDateTime computePlannedStartDateT(ManufOrder manufOrder) {

    OperationOrder firstOperationOrder = getFirstOperationOrder(manufOrder);

    if (firstOperationOrder != null) {

      return firstOperationOrder.getPlannedStartDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder) {

    OperationOrder lastOperationOrder = getLastOperationOrder(manufOrder);

    if (lastOperationOrder != null) {

      return lastOperationOrder.getPlannedEndDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void allOpFinished(ManufOrder manufOrder) throws AxelorException {
    int count = 0;
    List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
    for (OperationOrder operationOrderIt : operationOrderList) {
      if (operationOrderIt.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
        count++;
      }
    }

    if (count == operationOrderList.size()) {
      this.finish(manufOrder);
    }
  }

  /**
   * Returns first operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return First operation order of {@code manufOrder}
   */
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

  protected boolean sendMail(ManufOrder manufOrder, Template template) throws AxelorException {
    if (template == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MANUF_ORDER_MISSING_TEMPLATE));
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
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage(), manufOrder);
    }
    return true;
  }

  private void createPurchaseOrderLineProduction(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder) throws AxelorException {

    UnitConversionService unitConversionService = Beans.get(UnitConversionService.class);
    PurchaseOrderLineService purchaseOrderLineService = Beans.get(PurchaseOrderLineService.class);
    PurchaseOrderLine purchaseOrderLine;
    BigDecimal quantity = BigDecimal.ONE;
    Unit startUnit =
        Beans.get(UnitRepository.class)
            .all()
            .filter("self.name = 'Hour' AND self.unitTypeSelect = 3")
            .fetchOne();

    for (ProdHumanResource humanResource : operationOrder.getProdHumanResourceList()) {

      Product product = humanResource.getProduct();
      Unit purchaseUnit = product.getPurchasesUnit();

      if (purchaseUnit != null) {
        quantity =
            unitConversionService.convert(
                startUnit,
                purchaseUnit,
                new BigDecimal(humanResource.getDuration() / 3600),
                0,
                humanResource.getProduct());
      }

      purchaseOrderLine =
          purchaseOrderLineService.createPurchaseOrderLine(
              purchaseOrder,
              product,
              product.getName(),
              product.getDescription(),
              quantity,
              purchaseUnit);

      purchaseOrder.getPurchaseOrderLineList().add(purchaseOrderLine);
    }
  }

  private PurchaseOrder setPurchaseOrderSupplierDetails(PurchaseOrder purchaseOrder)
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

  @Transactional
  public void createPurchaseOrder(ManufOrder manufOrder) throws AxelorException {

    PurchaseOrder purchaseOrder =
        Beans.get(PurchaseOrderService.class)
            .createPurchaseOrder(
                null,
                manufOrder.getCompany(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                manufOrder.getProdProcess().getSubcontractors(),
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

    Beans.get(PurchaseOrderService.class).computePurchaseOrder(purchaseOrder);
    manufOrder.setPurchaseOrder(purchaseOrder);

    manufOrderRepo.save(manufOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void merge(List<Long> ids) throws AxelorException {
    if (!canMerge(ids)) {
      throw new AxelorException(
          ManufOrder.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.MANUF_ORDER_NO_GENERATION));
    }
    List<ManufOrder> manufOrderList =
        manufOrderRepo.all().filter("self.id in (" + Joiner.on(",").join(ids) + ")").fetch();

    /** Init all the necessary values to create the new Manuf Order */
    Product product = manufOrderList.get(0).getProduct();
    StockLocation stockLocation = manufOrderList.get(0).getWorkshopStockLocation();
    Company company = manufOrderList.get(0).getCompany();
    BillOfMaterial billOfMaterial =
        manufOrderList.stream()
            .filter(x -> x.getBillOfMaterial().getVersionNumber() == 1)
            .findFirst()
            .get()
            .getBillOfMaterial();
    int priority = manufOrderList.stream().mapToInt(mo -> mo.getPrioritySelect()).max().getAsInt();
    Unit unit = billOfMaterial.getUnit();
    BigDecimal qty = BigDecimal.ZERO;
    String note = "";

    ManufOrder mergedManufOrder = new ManufOrder();

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_MERGED);

      manufOrder.setManufOrderMergeResult(mergedManufOrder);
      for (ProductionOrder productionOrder : manufOrder.getProductionOrderSet()) {
        mergedManufOrder.addProductionOrderSetItem(productionOrder);
      }
      for (SaleOrder saleOrder : manufOrder.getSaleOrderSet()) {
        mergedManufOrder.addSaleOrderSetItem(saleOrder);
      }
      /*
       * If unit are the same, then add the qty If not, convert the unit and get the converted qty
       */
      if (manufOrder.getUnit().equals(unit)) {
        qty = qty.add(manufOrder.getQty());
      } else {
        BigDecimal qtyConverted =
            Beans.get(UnitConversionService.class)
                .convert(
                    manufOrder.getUnit(),
                    unit,
                    manufOrder.getQty(),
                    appBaseService.getNbDecimalDigitForQty(),
                    null);
        qty = qty.add(qtyConverted);
      }
      if (manufOrder.getNote() != null && manufOrder.getNote() != "") {
        note += manufOrder.getManufOrderSeq() + " : " + manufOrder.getNote() + "\n";
      }
    }

    Optional<LocalDateTime> minDate =
        manufOrderList.stream().map(mo -> mo.getPlannedStartDateT()).min(LocalDateTime::compareTo);

    if (minDate.isPresent()) {
      mergedManufOrder.setPlannedStartDateT(minDate.get());
    }

    /* Update the created manuf order */
    mergedManufOrder.setStatusSelect(ManufOrderRepository.STATUS_DRAFT);
    mergedManufOrder.setProduct(product);
    mergedManufOrder.setUnit(unit);
    mergedManufOrder.setWorkshopStockLocation(stockLocation);
    mergedManufOrder.setQty(qty);
    mergedManufOrder.setBillOfMaterial(billOfMaterial);
    mergedManufOrder.setCompany(company);
    mergedManufOrder.setPrioritySelect(priority);
    mergedManufOrder.setProdProcess(billOfMaterial.getProdProcess());
    mergedManufOrder.setNote(note);

    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    /*
     * Check the config to see if you directly plan the created manuf order or just prefill the
     * opertations
     */
    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getIsAutomaticallyPlanned()) {
      plan(mergedManufOrder);
    } else {
      ManufOrderService moService = Beans.get(ManufOrderService.class);
      moService.preFillOperations(mergedManufOrder);
    }

    manufOrderRepo.save(mergedManufOrder);
  }

  public boolean canMerge(List<Long> ids) {
    List<ManufOrder> manufOrderList =
        manufOrderRepo.all().filter("self.id in (" + Joiner.on(",").join(ids) + ")").fetch();

    // I check if all the status of the manuf order in the list are Draft or
    // Planned. If not i can return false
    boolean allStatusDraftOrPlanned =
        manufOrderList.stream()
            .allMatch(
                x ->
                    x.getStatusSelect().equals(ManufOrderRepository.STATUS_DRAFT)
                        || x.getStatusSelect().equals(ManufOrderRepository.STATUS_PLANNED));
    if (!allStatusDraftOrPlanned) {
      return false;
    }
    // I check if all the products are the same. If not i return false
    Product product = manufOrderList.get(0).getProduct();
    boolean allSameProducts = manufOrderList.stream().allMatch(x -> x.getProduct().equals(product));
    if (!allSameProducts) {
      return false;
    }

    // Check if one of the workShopStockLocation is null
    boolean oneWorkShopIsNull =
        manufOrderList.stream().anyMatch(x -> x.getWorkshopStockLocation() == null);
    if (oneWorkShopIsNull) {
      return false;
    }

    // I check if all the stockLocation are the same. If not i return false
    StockLocation stockLocation = manufOrderList.get(0).getWorkshopStockLocation();
    boolean allSameLocation =
        manufOrderList.stream()
            .allMatch(
                x ->
                    x.getWorkshopStockLocation() != null
                        && x.getWorkshopStockLocation().equals(stockLocation));
    if (!allSameLocation) {
      return false;
    }

    // Check if one of the billOfMaterial is null
    boolean oneBillOfMaterialIsNull =
        manufOrderList.stream().anyMatch(x -> x.getBillOfMaterial() == null);
    if (oneBillOfMaterialIsNull) {
      return false;
    }

    // Check if one of the billOfMaterial has his version equal to 1
    boolean oneBillOfMaterialWithFirstVersion =
        manufOrderList.stream().anyMatch(x -> x.getBillOfMaterial().getVersionNumber() == 1);
    if (!oneBillOfMaterialWithFirstVersion) {
      return false;
    }

    // I check if all the billOfMaterial are the same. If not i will check
    // if all version are compatible, and if not i can return false
    BillOfMaterial billOfMaterial =
        manufOrderList.stream()
            .filter(x -> x.getBillOfMaterial().getVersionNumber() == 1)
            .findFirst()
            .get()
            .getBillOfMaterial();
    boolean allSameOrCompatibleBillOfMaterial =
        manufOrderList.stream()
            .allMatch(
                x ->
                    x.getBillOfMaterial().equals(billOfMaterial)
                        || x.getBillOfMaterial()
                            .getOriginalBillOfMaterial()
                            .equals(billOfMaterial));
    if (!allSameOrCompatibleBillOfMaterial) {
      return false;
    }

    return true;
  }
}
