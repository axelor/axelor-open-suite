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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerLink;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.studio.db.AppSupplychain;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveServiceSupplychainImpl extends StockMoveServiceImpl
    implements StockMoveServiceSupplychain {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppSupplychainService appSupplyChainService;

  protected AppAccountService appAccountService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected UnitConversionService unitConversionService;
  protected ReservedQtyService reservedQtyService;
  protected PartnerSupplychainService partnerSupplychainService;
  protected FixedAssetRepository fixedAssetRepository;
  protected PfpService pfpService;
  protected SaleOrderConfirmService saleOrderConfirmService;
  protected StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;

  @Inject
  public StockMoveServiceSupplychainImpl(
      StockMoveLineService stockMoveLineService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService,
      ProductRepository productRepository,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService,
      AppStockService appStockService,
      ProductCompanyService productCompanyService,
      AppSupplychainService appSupplyChainService,
      AppAccountService appAccountService,
      PurchaseOrderRepository purchaseOrderRepo,
      SaleOrderRepository saleOrderRepo,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService,
      PartnerSupplychainService partnerSupplychainService,
      FixedAssetRepository fixedAssetRepository,
      PfpService pfpService,
      SaleOrderConfirmService saleOrderConfirmService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain) {
    super(
        stockMoveLineService,
        stockMoveToolService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService,
        productRepository,
        partnerStockSettingsService,
        stockConfigService,
        appStockService,
        productCompanyService);
    this.appSupplyChainService = appSupplyChainService;
    this.appAccountService = appAccountService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.unitConversionService = unitConversionService;
    this.reservedQtyService = reservedQtyService;
    this.partnerSupplychainService = partnerSupplychainService;
    this.fixedAssetRepository = fixedAssetRepository;
    this.pfpService = pfpService;
    this.saleOrderConfirmService = saleOrderConfirmService;
    this.stockMoveLineServiceSupplychain = stockMoveLineServiceSupplychain;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String realizeStockMove(StockMove stockMove, boolean check) throws AxelorException {

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
        && (stockMove.getPartner() != null
            && partnerSupplychainService.isBlockedPartnerOrParent(stockMove.getPartner()))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.CUSTOMER_HAS_BLOCKED_ACCOUNT));
    }

    if (!appSupplyChainService.isApp("supplychain")) {
      return super.realizeStockMove(stockMove, check);
    }

    LOG.debug("Stock move realization: {} ", stockMove.getStockMoveSeq());
    String newStockSeq = super.realizeStockMove(stockMove, check);
    AppSupplychain appSupplychain = appSupplyChainService.getAppSupplychain();

    Set<SaleOrder> saleOrderSet = stockMove.getSaleOrderSet();
    if (ObjectUtils.notEmpty(saleOrderSet)) {
      SaleOrderStockService saleOrderStockService = Beans.get(SaleOrderStockService.class);
      for (SaleOrder saleOrder : saleOrderSet) {
        updateSaleOrderLinesDeliveryState(stockMove, !stockMove.getIsReversion());
        // Update linked saleOrder delivery state depending on BackOrder's existence
        if (newStockSeq != null) {
          saleOrder.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
        } else {
          saleOrderStockService.updateDeliveryState(saleOrder);

          if (appSupplychain.getTerminateSaleOrderOnDelivery()) {
            terminateOrConfirmSaleOrderStatus(saleOrder);
          }
        }

        saleOrderRepo.save(saleOrder);
      }
    } else if (ObjectUtils.notEmpty(stockMove.getPurchaseOrderSet())) {
      PurchaseOrderStockService purchaseOrderStockService =
          Beans.get(PurchaseOrderStockService.class);
      for (PurchaseOrder purchaseOrder : stockMove.getPurchaseOrderSet()) {
        updatePurchaseOrderLines(stockMove, !stockMove.getIsReversion());
        // Update linked purchaseOrder receipt state depending on BackOrder's existence
        if (newStockSeq != null) {
          purchaseOrder.setReceiptState(PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED);
        } else {
          purchaseOrderStockService.updateReceiptState(purchaseOrder);

          if (appSupplychain.getTerminatePurchaseOrderOnReceipt()) {
            finishOrValidatePurchaseOrderStatus(purchaseOrder);
          }
        }

        purchaseOrderRepo.save(purchaseOrder);
      }
    }
    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      reservedQtyService.updateReservedQuantity(stockMove, StockMoveRepository.STATUS_REALIZED);
    }

    detachNonDeliveredStockMoveLines(stockMove);

    updateFixedAssets(stockMove);

    return newStockSeq;
  }

  protected void updateFixedAssets(StockMove stockMove) {
    List<StockMoveLine> stockMoveLineList =
        stockMove.getStockMoveLineList().stream()
            .filter(stockMoveLine -> stockMoveLine.getTrackingNumber() != null)
            .collect(Collectors.toList());
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      FixedAsset fixedAsset =
          fixedAssetRepository
              .all()
              .filter("self.trackingNumber = :trackingNumber")
              .bind("trackingNumber", stockMoveLine.getTrackingNumber())
              .fetchOne();
      if (fixedAsset != null) {
        fixedAsset.setStockLocation(stockMoveLine.getToStockLocation());
      }
    }
  }

  @Override
  public void detachNonDeliveredStockMoveLines(StockMove stockMove) {
    if (stockMove.getStockMoveLineList() == null) {
      return;
    }
    stockMove.getStockMoveLineList().stream()
        .filter(line -> line.getRealQty().signum() == 0)
        .forEach(line -> line.setSaleOrderLine(null));
  }

  @Override
  public void cancel(StockMove stockMove) throws AxelorException {

    cancelStockMove(stockMove);
    Boolean supplierArrivalCancellationAutomaticMail =
        stockConfigService
            .getStockConfig(stockMove.getCompany())
            .getSupplierArrivalCancellationAutomaticMail();
    if (!supplierArrivalCancellationAutomaticMail
        || stockMove.getIsReversion()
        || stockMove.getTypeSelect() != StockMoveRepository.TYPE_INCOMING) {
      return;
    }
    Template supplierCancellationMessageTemplate =
        stockConfigService
            .getStockConfig(stockMove.getCompany())
            .getSupplierArrivalCancellationMessageTemplate();
    super.sendMailForStockMove(stockMove, supplierCancellationMessageTemplate);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void cancelStockMove(StockMove stockMove) throws AxelorException {
    if (!appSupplyChainService.isApp("supplychain")) {
      super.cancel(stockMove);
      return;
    }

    if (stockMove.getInvoicingStatusSelect() == StockMoveRepository.STATUS_PARTIALLY_INVOICED
        || stockMove.getInvoicingStatusSelect() == StockMoveRepository.STATUS_INVOICED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.STOCK_MOVE_CANCEL_WRONG_STATUS_ERROR));
    }
    if (stockMove.getStatusSelect() == StockMoveRepository.STATUS_REALIZED) {
      if (ObjectUtils.notEmpty(stockMove.getSaleOrderSet())) {
        updateSaleOrderOnCancel(stockMove);
      }
      if (ObjectUtils.notEmpty(stockMove.getPurchaseOrderSet())) {
        updatePurchaseOrderOnCancel(stockMove);
      }
    }
    super.cancel(stockMove);
    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      reservedQtyService.updateReservedQuantity(stockMove, StockMoveRepository.STATUS_CANCELED);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void planStockMove(StockMove stockMove, boolean splitByTrackingNumber)
      throws AxelorException {
    super.planStockMove(stockMove, splitByTrackingNumber);
    updateReservedQuantity(stockMove);
  }

  protected void updateReservedQuantity(StockMove stockMove) throws AxelorException {
    if (appSupplyChainService.isApp("supplychain")
        && appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      reservedQtyService.updateReservedQuantity(stockMove, StockMoveRepository.STATUS_PLANNED);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateSaleOrderOnCancel(StockMove stockMove) throws AxelorException {
    Set<SaleOrder> saleOrderSet = stockMove.getSaleOrderSet();
    SaleOrderStockService saleOrderStockService = Beans.get(SaleOrderStockService.class);
    for (SaleOrder so : saleOrderSet) {

      updateSaleOrderLinesDeliveryState(stockMove, stockMove.getIsReversion());
      saleOrderStockService.updateDeliveryState(so);

      if (appSupplyChainService.getAppSupplychain().getTerminateSaleOrderOnDelivery()) {
        terminateOrConfirmSaleOrderStatus(so);
      }
    }
  }

  /**
   * Update saleOrder status from or to terminated status, from or to confirm status, depending on
   * its delivery state. Should be called only if we terminate sale order on receipt.
   *
   * @param saleOrder
   */
  protected void terminateOrConfirmSaleOrderStatus(SaleOrder saleOrder) throws AxelorException {
    // have to use Beans.get because of circular dependency
    SaleOrderWorkflowService saleOrderWorkflowService = Beans.get(SaleOrderWorkflowService.class);
    if (saleOrder.getDeliveryState() == SaleOrderRepository.DELIVERY_STATE_DELIVERED
        && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      saleOrderWorkflowService.completeSaleOrder(saleOrder);
    } else if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      saleOrderConfirmService.confirmSaleOrder(saleOrder);
    }
  }

  protected void updateSaleOrderLinesDeliveryState(StockMove stockMove, boolean qtyWasDelivered)
      throws AxelorException {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getSaleOrderLine() != null) {
        SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();

        BigDecimal realQty =
            unitConversionService.convert(
                stockMoveLine.getUnit(),
                saleOrderLine.getUnit(),
                stockMoveLine.getRealQty(),
                stockMoveLine.getRealQty().scale(),
                saleOrderLine.getProduct());

        if (stockMove.getTypeSelect() != StockMoveRepository.TYPE_INTERNAL) {
          if (qtyWasDelivered) {
            saleOrderLine.setDeliveredQty(saleOrderLine.getDeliveredQty().add(realQty));
          } else {
            saleOrderLine.setDeliveredQty(saleOrderLine.getDeliveredQty().subtract(realQty));
          }
        }
        if (saleOrderLine.getDeliveredQty().signum() == 0) {
          saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_NOT_DELIVERED);
        } else if (saleOrderLine.getDeliveredQty().compareTo(saleOrderLine.getQty()) < 0) {
          saleOrderLine.setDeliveryState(
              SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
        } else {
          saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_DELIVERED);
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updatePurchaseOrderOnCancel(StockMove stockMove) throws AxelorException {
    Set<PurchaseOrder> poSet = stockMove.getPurchaseOrderSet();
    PurchaseOrderStockService purchaseOrderStockService =
        Beans.get(PurchaseOrderStockService.class);
    for (PurchaseOrder po : poSet) {

      updatePurchaseOrderLines(stockMove, stockMove.getIsReversion());
      purchaseOrderStockService.updateReceiptState(po);
      if (appSupplyChainService.getAppSupplychain().getTerminatePurchaseOrderOnReceipt()) {
        finishOrValidatePurchaseOrderStatus(po);
      }
    }
  }

  /**
   * Update purchaseOrder status from or to finished status, from or to validated status, depending
   * on its state. Should be called only if we terminate purchase order on receipt.
   *
   * @param purchaseOrder a purchase order.
   */
  protected void finishOrValidatePurchaseOrderStatus(PurchaseOrder purchaseOrder) {

    if (purchaseOrder.getReceiptState() == PurchaseOrderRepository.STATE_RECEIVED) {
      purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_FINISHED);
    } else {
      purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_VALIDATED);
    }
  }

  protected void updatePurchaseOrderLines(StockMove stockMove, boolean qtyWasReceived)
      throws AxelorException {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getPurchaseOrderLine() != null) {
        PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();

        BigDecimal realQty =
            unitConversionService.convert(
                stockMoveLine.getUnit(),
                purchaseOrderLine.getUnit(),
                stockMoveLine.getRealQty(),
                stockMoveLine.getRealQty().scale(),
                purchaseOrderLine.getProduct());

        if (qtyWasReceived) {
          purchaseOrderLine.setReceivedQty(purchaseOrderLine.getReceivedQty().add(realQty));
        } else {
          purchaseOrderLine.setReceivedQty(purchaseOrderLine.getReceivedQty().subtract(realQty));
        }
        if (purchaseOrderLine.getReceivedQty().signum() == 0) {
          purchaseOrderLine.setReceiptState(PurchaseOrderRepository.STATE_NOT_RECEIVED);
        } else if (purchaseOrderLine.getReceivedQty().compareTo(purchaseOrderLine.getQty()) < 0) {
          purchaseOrderLine.setReceiptState(PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED);
        } else {
          purchaseOrderLine.setReceiptState(PurchaseOrderRepository.STATE_RECEIVED);
        }
      }
    }
  }

  /**
   * The splitted stock move line needs an allocation and will be planned before the previous stock
   * move line is realized. To solve this issue, we deallocate here in the previous stock move line
   * the quantity that will be allocated in the generated stock move line. The quantity will be
   * reallocated when the generated stock move is planned.
   *
   * @param stockMoveLine the previous stock move line
   * @return the generated stock move line
   * @throws AxelorException
   */
  @Override
  protected StockMoveLine copySplittedStockMoveLine(StockMoveLine stockMoveLine)
      throws AxelorException {
    StockMoveLine newStockMoveLine = super.copySplittedStockMoveLine(stockMoveLine);

    if (appSupplyChainService.isApp("supplychain")
        && appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      BigDecimal requestedReservedQty =
          stockMoveLine
              .getRequestedReservedQty()
              .subtract(stockMoveLine.getRealQty())
              .max(BigDecimal.ZERO);

      newStockMoveLine.setRequestedReservedQty(requestedReservedQty);
      newStockMoveLine.setReservedQty(BigDecimal.ZERO);

      reservedQtyService.deallocateStockMoveLineAfterSplit(
          stockMoveLine, stockMoveLine.getReservedQty());
      stockMoveLine.setReservedQty(BigDecimal.ZERO);
    }
    return newStockMoveLine;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove splitInto2(
      StockMove originalStockMove, List<StockMoveLine> modifiedStockMoveLines)
      throws AxelorException {

    checkAssociatedInvoiceLine(modifiedStockMoveLines);
    StockMove newStockMove = super.splitInto2(originalStockMove, modifiedStockMoveLines);
    newStockMove.setOrigin(originalStockMove.getOrigin());
    setOrigin(originalStockMove, newStockMove);
    return newStockMove;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void splitStockMoveLines(
      StockMove stockMove, List<StockMoveLine> stockMoveLines, BigDecimal splitQty)
      throws AxelorException {
    checkAssociatedInvoiceLine(stockMoveLines);
    super.splitStockMoveLines(stockMove, stockMoveLines, splitQty);
  }

  /**
   * Methods that checks if any of the stock move lines are associated with a invoice line. Will
   * throws a exception if it is the case
   *
   * @throws AxelorException if any stock move line is associated with invoice line.
   * @param stockMoveLines
   */
  protected void checkAssociatedInvoiceLine(List<StockMoveLine> stockMoveLines)
      throws AxelorException {
    StringJoiner pairSMLInvoiceLineSj = new StringJoiner(" / ");
    boolean isAssociated = false;
    for (StockMoveLine stockMoveLine : stockMoveLines) {
      if (stockMoveLine.getId() != null) {
        List<InvoiceLine> associatedInvoiceLines =
            stockMoveLineServiceSupplychain.getInvoiceLines(stockMoveLine);
        if (!associatedInvoiceLines.isEmpty()) {
          associatedInvoiceLines.stream()
              .forEach(
                  invoiceLine -> {
                    pairSMLInvoiceLineSj.add(
                        String.format(
                            "%s -> %s (%s)",
                            stockMoveLine.getName(),
                            invoiceLine.getInvoice().getInvoiceId(),
                            invoiceLine.getName()));
                  });
          isAssociated = true;
        }
      }
    }
    if (isAssociated) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              SupplychainExceptionMessage
                  .STOCK_MOVE_LINES_ASSOCIATED_WITH_INVOICE_LINES_CANNOT_SPLIT),
          pairSMLInvoiceLineSj.toString());
    }
  }

  @Override
  protected StockMoveLine createSplitStockMoveLine(
      StockMove originalStockMove,
      StockMoveLine originalStockMoveLine,
      StockMoveLine modifiedStockMoveLine) {

    StockMoveLine newStockMoveLine =
        super.createSplitStockMoveLine(
            originalStockMove, originalStockMoveLine, modifiedStockMoveLine);

    if (originalStockMoveLine.getQty().compareTo(originalStockMoveLine.getRequestedReservedQty())
        < 0) {
      newStockMoveLine.setRequestedReservedQty(
          originalStockMoveLine.getRequestedReservedQty().subtract(originalStockMoveLine.getQty()));
      originalStockMoveLine.setRequestedReservedQty(originalStockMoveLine.getQty());
    }
    newStockMoveLine.setPurchaseOrderLine(originalStockMoveLine.getPurchaseOrderLine());
    newStockMoveLine.setSaleOrderLine(originalStockMoveLine.getSaleOrderLine());

    return newStockMoveLine;
  }

  @Override
  public void verifyProductStock(StockMove stockMove) throws AxelorException {
    AppSupplychain appSupplychain = appSupplyChainService.getAppSupplychain();
    if (stockMove.getAvailabilityRequest()
        && stockMove.getStockMoveLineList() != null
        && appSupplychain.getIsVerifyProductStock()) {
      StringJoiner notAvailableProducts = new StringJoiner(",");
      int counter = 1;
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        boolean isAvailableProduct =
            stockMoveLineServiceSupplychain.isAvailableProduct(stockMoveLine);
        if (!isAvailableProduct && counter <= 10) {
          notAvailableProducts.add(stockMoveLine.getProduct().getFullName());
          counter++;
        }
      }
      if (!Strings.isNullOrEmpty(notAvailableProducts.toString())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(SupplychainExceptionMessage.STOCK_MOVE_VERIFY_PRODUCT_STOCK_ERROR),
                notAvailableProducts.toString()));
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<StockMove> generateReversion(StockMove stockMove) throws AxelorException {

    Optional<StockMove> newStockMove = super.generateReversion(stockMove);
    List<StockMoveLine> stockMoveLineList =
        newStockMove.isPresent() ? newStockMove.get().getStockMoveLineList() : null;
    if (stockMoveLineList != null && !stockMoveLineList.isEmpty()) {
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        stockMoveLine.setQtyInvoiced(BigDecimal.ZERO);
      }
    }
    return newStockMove;
  }

  @Override
  public boolean isAllocatedStockMoveLineRemoved(StockMove stockMove) {

    StockMove storedStockMove = stockMoveRepo.find(stockMove.getId());
    Boolean isAllocatedStockMoveLineRemoved = false;

    if (ObjectUtils.notEmpty(storedStockMove)) {
      List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
      List<StockMoveLine> storedStockMoveLineList = storedStockMove.getStockMoveLineList();
      if (stockMoveLineList != null && storedStockMoveLineList != null) {
        for (StockMoveLine stockMoveLine : storedStockMoveLineList) {
          if (stockMoveLineServiceSupplychain.isAllocatedStockMoveLine(stockMoveLine)
              && !stockMoveLineList.contains(stockMoveLine)) {
            stockMoveLineList.add(stockMoveLine);
            isAllocatedStockMoveLineRemoved = true;
          }
          if (isAllocatedStockMoveLineRemoved) {
            stockMove.setStockMoveLineList(stockMoveLineList);
          }
        }
      }
    }

    return isAllocatedStockMoveLineRemoved;
  }

  @Override
  public void setDefaultInvoicedPartner(StockMove stockMove) {
    if (stockMove != null
        && stockMove.getPartner() != null
        && stockMove.getPartner().getId() != null) {
      Partner partner = Beans.get(PartnerRepository.class).find(stockMove.getPartner().getId());
      if (partner != null) {
        if (!CollectionUtils.isEmpty(partner.getManagedByPartnerLinkList())) {
          List<PartnerLink> partnerLinkList = partner.getManagedByPartnerLinkList();
          // Retrieve all Invoiced to Type
          List<PartnerLink> partnerLinkInvoicedByList =
              partnerLinkList.stream()
                  .filter(
                      partnerLink ->
                          partnerLink
                              .getPartnerLinkType()
                              .getTypeSelect()
                              .equals(PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_TO))
                  .collect(Collectors.toList());

          // If there is only one, then it is the default one
          if (partnerLinkInvoicedByList.size() == 1) {
            PartnerLink partnerLinkInvoicedBy = partnerLinkInvoicedByList.get(0);
            stockMove.setInvoicedPartner(partnerLinkInvoicedBy.getPartner2());
          } else if (partnerLinkInvoicedByList.isEmpty()) {
            stockMove.setInvoicedPartner(partner);
          } else {
            stockMove.setInvoicedPartner(null);
          }

        } else {
          stockMove.setInvoicedPartner(partner);
        }
      }
    }
  }

  @Override
  public StockMove createStockMove(
      Address fromAddress,
      Address toAddress,
      Company company,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      LocalDate realDate,
      LocalDate estimatedDate,
      String note,
      int typeSelect)
      throws AxelorException {
    StockMove stockMove =
        super.createStockMove(
            fromAddress,
            toAddress,
            company,
            fromStockLocation,
            toStockLocation,
            realDate,
            estimatedDate,
            note,
            typeSelect);

    if (appAccountService.isApp("account")
        && pfpService.isManagePassedForPayment(company)
        && stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
        && !stockMove.getIsReversion()) {
      stockMove.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_AWAITING);
    }
    return stockMove;
  }

  @Override
  public void checkInvoiceStatus(StockMove stockMove) throws AxelorException {
    Set<Invoice> invoiceSet = stockMove.getInvoiceSet();
    if (!CollectionUtils.isEmpty(invoiceSet)) {
      for (Invoice invoice : invoiceSet) {
        if (invoice.getStatusSelect() == InvoiceRepository.STATUS_DRAFT
            || invoice.getStatusSelect() == InvoiceRepository.STATUS_VALIDATED) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SupplychainExceptionMessage.STOCK_MOVE_CANCEL_WRONG_INVOICE_STATUS_ALERT),
              stockMove.getStockMoveSeq());
        }
      }
    }
  }

  @Override
  public void setOrigin(StockMove oldStockMove, StockMove newStockMove) {
    if (ObjectUtils.notEmpty(oldStockMove.getSaleOrderSet())) {
      newStockMove.setSaleOrderSet(Sets.newHashSet(oldStockMove.getSaleOrderSet()));
    } else if (oldStockMove.getPurchaseOrderSet() != null) {
      newStockMove.setPurchaseOrderSet(Sets.newHashSet(oldStockMove.getPurchaseOrderSet()));
    } else {
      super.setOrigin(oldStockMove, newStockMove);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setInvoicingStatusInvoicedDelayed(StockMove stockMove) {
    stockMove = stockMoveRepo.find(stockMove.getId());
    stockMove.setInvoicingStatusSelect(StockMoveRepository.STATUS_DELAYED_INVOICE);
    stockMoveRepo.save(stockMove);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setInvoicingStatusInvoicedValidated(StockMove stockMove) {
    stockMove = stockMoveRepo.find(stockMove.getId());
    stockMove.setInvoicingStatusSelect(StockMoveRepository.STATUS_VALIDATED_INVOICE);
    stockMoveRepo.save(stockMove);
  }

  @Override
  public void fillRealQuantities(StockMove stockMove) {
    Objects.requireNonNull(stockMove);
    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
    if (stockMoveLineList != null) {
      for (StockMoveLine sml : stockMoveLineList) {
        sml.setRealQty(sml.getQty());
        sml.setTotalNetMass(sml.getQty().multiply(sml.getNetMass()));
      }
    }
  }
}
