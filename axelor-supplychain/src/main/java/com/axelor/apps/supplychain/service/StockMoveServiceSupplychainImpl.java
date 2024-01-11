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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.PartnerSupplychainLink;
import com.axelor.apps.supplychain.db.repo.PartnerSupplychainLinkTypeRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSupplychain;
import com.google.common.base.Strings;
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
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveServiceSupplychainImpl extends StockMoveServiceImpl
    implements StockMoveServiceSupplychain {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppSupplychainService appSupplyChainService;

  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected UnitConversionService unitConversionService;
  protected ReservedQtyService reservedQtyService;
  protected PartnerSupplychainService partnerSupplychainService;

  @Inject private StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;

  @Inject
  public StockMoveServiceSupplychainImpl(
      StockMoveLineService stockMoveLineService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService,
      AppSupplychainService appSupplyChainService,
      AccountConfigService accountConfigService,
      PurchaseOrderRepository purchaseOrderRepo,
      SaleOrderRepository saleOrderRepo,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService,
      ProductRepository productRepository,
      PartnerSupplychainService partnerSupplychainService,
      AppAccountService appAccountService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService) {
    super(
        stockMoveLineService,
        stockMoveToolService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService,
        productRepository,
        partnerStockSettingsService,
        stockConfigService);
    this.appSupplyChainService = appSupplyChainService;
    this.accountConfigService = accountConfigService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.unitConversionService = unitConversionService;
    this.reservedQtyService = reservedQtyService;
    this.partnerSupplychainService = partnerSupplychainService;
    this.appAccountService = appAccountService;
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

    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      updateSaleOrderLinesDeliveryState(stockMove, !stockMove.getIsReversion());
      // Update linked saleOrder delivery state depending on BackOrder's existence
      SaleOrder saleOrder = saleOrderRepo.find(stockMove.getOriginId());
      if (newStockSeq != null) {
        saleOrder.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
      } else {
        Beans.get(SaleOrderStockService.class).updateDeliveryState(saleOrder);

        if (appSupplychain.getTerminateSaleOrderOnDelivery()) {
          terminateOrConfirmSaleOrderStatus(saleOrder);
        }
      }

      saleOrderRepo.save(saleOrder);
    } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      updatePurchaseOrderLines(stockMove, !stockMove.getIsReversion());
      // Update linked purchaseOrder receipt state depending on BackOrder's existence
      PurchaseOrder purchaseOrder = purchaseOrderRepo.find(stockMove.getOriginId());
      if (newStockSeq != null) {
        purchaseOrder.setReceiptState(PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED);
      } else {
        Beans.get(PurchaseOrderStockService.class).updateReceiptState(purchaseOrder);

        if (appSupplychain.getTerminatePurchaseOrderOnReceipt()) {
          finishOrValidatePurchaseOrderStatus(purchaseOrder);
        }
      }

      purchaseOrderRepo.save(purchaseOrder);
    }
    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      reservedQtyService.updateReservedQuantity(stockMove, StockMoveRepository.STATUS_REALIZED);
    }

    detachNonDeliveredStockMoveLines(stockMove);

    List<Long> trackingNumberIds =
        stockMove.getStockMoveLineList().stream()
            .map(StockMoveLine::getTrackingNumber)
            .filter(Objects::nonNull)
            .map(TrackingNumber::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(trackingNumberIds)) {
      Query update =
          JPA.em()
              .createQuery(
                  "UPDATE FixedAsset self SET self.stockLocation = :stockLocation WHERE self.trackingNumber.id IN (:trackingNumber)");
      update.setParameter("stockLocation", stockMove.getToStockLocation());
      update.setParameter("trackingNumber", trackingNumberIds);
      update.executeUpdate();
    }

    return newStockSeq;
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
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(StockMove stockMove) throws AxelorException {

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
      if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
        updateSaleOrderOnCancel(stockMove);
      }
      if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
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
  public void planStockMove(StockMove stockMove) throws AxelorException {
    super.planStockMove(stockMove);
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
    SaleOrder so = saleOrderRepo.find(stockMove.getOriginId());

    updateSaleOrderLinesDeliveryState(stockMove, stockMove.getIsReversion());
    Beans.get(SaleOrderStockService.class).updateDeliveryState(so);

    if (appSupplyChainService.getAppSupplychain().getTerminateSaleOrderOnDelivery()) {
      terminateOrConfirmSaleOrderStatus(so);
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
      saleOrderWorkflowService.confirmSaleOrder(saleOrder);
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
    PurchaseOrder po = purchaseOrderRepo.find(stockMove.getOriginId());

    updatePurchaseOrderLines(stockMove, stockMove.getIsReversion());
    Beans.get(PurchaseOrderStockService.class).updateReceiptState(po);
    if (appSupplyChainService.getAppSupplychain().getTerminatePurchaseOrderOnReceipt()) {
      finishOrValidatePurchaseOrderStatus(po);
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
    newStockMove.setOriginTypeSelect(originalStockMove.getOriginTypeSelect());
    newStockMove.setOriginId(originalStockMove.getOriginId());
    return newStockMove;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean splitStockMoveLines(
      StockMove stockMove, List<StockMoveLine> stockMoveLines, BigDecimal splitQty)
      throws AxelorException {
    checkAssociatedInvoiceLine(stockMoveLines);
    return super.splitStockMoveLines(stockMove, stockMoveLines, splitQty);
  }

  /**
   * Methods that checks if any of the stock move lines are associated with a invoice line. Will
   * throws a exception if it is the case
   *
   * @throws AxelorException if any stock move line is associated with invoice line.
   * @param modifiedStockMoveLines
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
        && appSupplychain.getIsVerifyProductStock()
        && stockMove.getFromStockLocation() != null) {
      StringJoiner notAvailableProducts = new StringJoiner(",");
      int counter = 1;
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        boolean isAvailableProduct =
            stockMoveLineServiceSupplychain.isAvailableProduct(stockMove, stockMoveLine);
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
        if (!CollectionUtils.isEmpty(partner.getPartner1SupplychainLinkList())) {
          List<PartnerSupplychainLink> partnerSupplychainLinkList =
              partner.getPartner1SupplychainLinkList();
          // Retrieve all Invoiced by Type
          List<PartnerSupplychainLink> partnerSupplychainLinkInvoicedByList =
              partnerSupplychainLinkList.stream()
                  .filter(
                      partnerSupplychainLink ->
                          PartnerSupplychainLinkTypeRepository.TYPE_SELECT_INVOICED_BY.equals(
                              partnerSupplychainLink
                                  .getPartnerSupplychainLinkType()
                                  .getTypeSelect()))
                  .collect(Collectors.toList());

          // If there is only one, then it is the default one
          if (partnerSupplychainLinkInvoicedByList.size() == 1) {
            PartnerSupplychainLink partnerSupplychainLinkInvoicedBy =
                partnerSupplychainLinkInvoicedByList.get(0);
            stockMove.setInvoicedPartner(partnerSupplychainLinkInvoicedBy.getPartner2());
          } else if (partnerSupplychainLinkInvoicedByList.isEmpty()) {
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

    if (appAccountService.isApp("account")) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      if (accountConfig.getIsManagePassedForPayment()
          && stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
          && !stockMove.getIsReversion()) {
        stockMove.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_AWAITING);
      }
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
}
