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

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.StockLocationLineHistoryService;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.batch.BatchAccountingCutOffSupplyChain;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequestScoped
public class StockMoveLineServiceSupplychainImpl extends StockMoveLineServiceImpl
    implements StockMoveLineServiceSupplychain {

  protected AccountManagementService accountManagementService;
  protected PriceListService priceListService;
  protected SupplychainBatchRepository supplychainBatchRepo;
  protected SupplyChainConfigService supplychainConfigService;
  protected InvoiceLineRepository invoiceLineRepository;

  @Inject
  public StockMoveLineServiceSupplychainImpl(
      TrackingNumberService trackingNumberService,
      AppBaseService appBaseService,
      AppStockService appStockService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationLineService stockLocationLineService,
      UnitConversionService unitConversionService,
      WeightedAveragePriceService weightedAveragePriceService,
      TrackingNumberRepository trackingNumberRepo,
      ShippingCoefService shippingCoefService,
      AccountManagementService accountManagementService,
      PriceListService priceListService,
      ProductCompanyService productCompanyService,
      SupplychainBatchRepository supplychainBatchRepo,
      SupplyChainConfigService supplychainConfigService,
      StockLocationLineHistoryService stockLocationLineHistoryService,
      InvoiceLineRepository invoiceLineRepository) {
    super(
        trackingNumberService,
        appBaseService,
        appStockService,
        stockMoveToolService,
        stockMoveLineRepository,
        stockLocationLineService,
        unitConversionService,
        weightedAveragePriceService,
        trackingNumberRepo,
        productCompanyService,
        shippingCoefService,
        stockLocationLineHistoryService);
    this.accountManagementService = accountManagementService;
    this.priceListService = priceListService;
    this.supplychainBatchRepo = supplychainBatchRepo;
    this.supplychainConfigService = supplychainConfigService;
    this.invoiceLineRepository = invoiceLineRepository;
  }

  @Override
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal requestedReservedQty,
      BigDecimal unitPrice,
      BigDecimal companyUnitPriceUntaxed,
      BigDecimal companyPurchasePrice,
      Unit unit,
      StockMove stockMove,
      int type,
      boolean taxed,
      BigDecimal taxRate,
      SaleOrderLine saleOrderLine,
      PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {
    if (product != null) {

      StockMoveLine stockMoveLine =
          generateStockMoveLineConvertingUnitPrice(
              product,
              productName,
              description,
              quantity,
              unitPrice,
              companyUnitPriceUntaxed,
              companyPurchasePrice,
              unit,
              stockMove,
              taxed,
              taxRate);
      stockMoveLine.setRequestedReservedQty(requestedReservedQty);
      stockMoveLine.setIsQtyRequested(
          requestedReservedQty != null && requestedReservedQty.signum() > 0);
      stockMoveLine.setSaleOrderLine(saleOrderLine);
      stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
      TrackingNumberConfiguration trackingNumberConfiguration =
          product.getTrackingNumberConfiguration();

      return assignOrGenerateTrackingNumber(
          stockMoveLine, stockMove, product, trackingNumberConfiguration, type);
    } else {
      return this.createStockMoveLine(
          product,
          productName,
          description,
          quantity,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          companyUnitPriceUntaxed,
          BigDecimal.ZERO,
          unit,
          stockMove,
          null);
    }
  }

  @Override
  public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {

    // the case when stockMove is null is made in super.
    if (stockMove == null || !appBaseService.isApp("supplychain")) {
      return super.compute(stockMoveLine, null);
    }

    if (stockMove.getOriginId() != null
        && stockMove.getOriginId() != 0L
        && (stockMoveLine.getSaleOrderLine() != null
            || stockMoveLine.getPurchaseOrderLine() != null)) {
      // the stock move comes from a sale or purchase order, we take the price from the order.
      stockMoveLine = computeFromOrder(stockMoveLine, stockMove);
    } else {
      stockMoveLine = super.compute(stockMoveLine, stockMove);
    }
    return stockMoveLine;
  }

  protected StockMoveLine computeFromOrder(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    BigDecimal unitPriceUntaxed = stockMoveLine.getUnitPriceUntaxed();
    BigDecimal unitPriceTaxed = stockMoveLine.getUnitPriceTaxed();
    Unit orderUnit = null;
    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
      if (saleOrderLine == null) {
        // log the exception
        TraceBackService.trace(
            new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                SupplychainExceptionMessage.STOCK_MOVE_MISSING_SALE_ORDER,
                stockMove.getOriginId(),
                stockMove.getName()));
      } else {
        unitPriceUntaxed = saleOrderLine.getPriceDiscounted();
        unitPriceTaxed = saleOrderLine.getInTaxPrice();
        orderUnit = saleOrderLine.getUnit();
      }
    } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();
      if (purchaseOrderLine == null) {
        // log the exception
        TraceBackService.trace(
            new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                SupplychainExceptionMessage.STOCK_MOVE_MISSING_PURCHASE_ORDER,
                stockMove.getOriginId(),
                stockMove.getName()));
      } else {
        unitPriceUntaxed = purchaseOrderLine.getPriceDiscounted();
        unitPriceTaxed = purchaseOrderLine.getInTaxPrice();
        orderUnit = purchaseOrderLine.getUnit();
      }
    }

    stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
    stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);

    Unit stockUnit = getStockUnit(stockMoveLine);
    return convertUnitPrice(stockMoveLine, orderUnit, stockUnit);
  }

  protected StockMoveLine convertUnitPrice(StockMoveLine stockMoveLine, Unit fromUnit, Unit toUnit)
      throws AxelorException {
    // convert unit price, meaning the conversion is reversed : Box of 12 pieces => 12 pieces but
    // 1/12 the price
    if (toUnit != null && fromUnit != null) {
      BigDecimal unitPriceUntaxed =
          unitConversionService.convert(
              toUnit,
              fromUnit,
              stockMoveLine.getUnitPriceUntaxed(),
              appBaseService.getNbDecimalDigitForUnitPrice(),
              null);
      BigDecimal unitPriceTaxed =
          unitConversionService.convert(
              toUnit,
              fromUnit,
              stockMoveLine.getUnitPriceTaxed(),
              appBaseService.getNbDecimalDigitForUnitPrice(),
              null);
      stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
      stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);
    }
    return stockMoveLine;
  }

  @Override
  public StockMoveLine splitStockMoveLine(
      StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber)
      throws AxelorException {

    StockMoveLine newStockMoveLine = super.splitStockMoveLine(stockMoveLine, qty, trackingNumber);

    if (!appBaseService.isApp("supplychain")) {
      return newStockMoveLine;
    }
    BigDecimal reservedQtyAfterSplit =
        BigDecimal.ZERO.max(stockMoveLine.getRequestedReservedQty().subtract(qty));
    BigDecimal reservedQtyInNewLine = stockMoveLine.getRequestedReservedQty().min(qty);
    stockMoveLine.setRequestedReservedQty(reservedQtyAfterSplit);
    newStockMoveLine.setRequestedReservedQty(reservedQtyInNewLine);
    newStockMoveLine.setSaleOrderLine(stockMoveLine.getSaleOrderLine());
    newStockMoveLine.setPurchaseOrderLine(stockMoveLine.getPurchaseOrderLine());
    return newStockMoveLine;
  }

  @Override
  public void updateAvailableQty(StockMoveLine stockMoveLine, StockLocation stockLocation) {

    if (!appBaseService.isApp("supplychain")) {
      super.updateAvailableQty(stockMoveLine, stockLocation);
      return;
    }

    BigDecimal availableQty = BigDecimal.ZERO;
    BigDecimal availableQtyForProduct = BigDecimal.ZERO;

    if (stockMoveLine.getProduct() != null) {
      if (stockMoveLine.getProduct().getTrackingNumberConfiguration() != null) {

        if (stockMoveLine.getTrackingNumber() != null) {
          StockLocationLine stockLocationLine =
              stockLocationLineService.getDetailLocationLine(
                  stockLocation, stockMoveLine.getProduct(), stockMoveLine.getTrackingNumber());

          if (stockLocationLine != null) {
            availableQty =
                stockLocationLine
                    .getCurrentQty()
                    .add(stockMoveLine.getReservedQty())
                    .subtract(stockLocationLine.getReservedQty());
          }
        }

        if (availableQty.compareTo(stockMoveLine.getRealQty()) < 0) {
          StockLocationLine stockLocationLineForProduct =
              stockLocationLineService.getStockLocationLine(
                  stockLocation, stockMoveLine.getProduct());

          if (stockLocationLineForProduct != null) {
            availableQtyForProduct =
                stockLocationLineForProduct
                    .getCurrentQty()
                    .add(stockMoveLine.getReservedQty())
                    .subtract(stockLocationLineForProduct.getReservedQty());
          }
        }
      } else {
        StockLocationLine stockLocationLine =
            stockLocationLineService.getStockLocationLine(
                stockLocation, stockMoveLine.getProduct());

        if (stockLocationLine != null) {
          availableQty =
              stockLocationLine
                  .getCurrentQty()
                  .add(stockMoveLine.getReservedQty())
                  .subtract(stockLocationLine.getReservedQty());
        }
      }
    }
    stockMoveLine.setAvailableQty(availableQty);
    stockMoveLine.setAvailableQtyForProduct(availableQtyForProduct);
  }

  @Override
  public StockMoveLine getMergedStockMoveLine(List<StockMoveLine> stockMoveLineList)
      throws AxelorException {

    if (stockMoveLineList == null || stockMoveLineList.isEmpty()) {
      return null;
    }

    if (stockMoveLineList.size() == 1) {
      return stockMoveLineList.get(0);
    }
    StockMove stockMove = stockMoveLineList.get(0).getStockMove();

    SaleOrderLine saleOrderLine = stockMoveLineList.get(0).getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLineList.get(0).getPurchaseOrderLine();

    Product product;
    String productName;
    String description;
    BigDecimal quantity = BigDecimal.ZERO;
    Unit unit;

    if (saleOrderLine != null) {
      product = saleOrderLine.getProduct();
      productName = saleOrderLine.getProductName();
      description = saleOrderLine.getDescription();
      unit = saleOrderLine.getUnit();

    } else if (purchaseOrderLine != null) {
      product = purchaseOrderLine.getProduct();
      productName = purchaseOrderLine.getProductName();
      description = purchaseOrderLine.getDescription();
      unit = purchaseOrderLine.getUnit();

    } else {
      return null; // shouldn't ever happen or you misused this function
    }

    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      quantity = quantity.add(stockMoveLine.getRealQty());
    }

    StockMoveLine generatedStockMoveLine =
        createStockMoveLine(
            product,
            productName,
            description,
            quantity,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            unit,
            stockMove,
            null);

    generatedStockMoveLine.setSaleOrderLine(saleOrderLine);
    generatedStockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    generatedStockMoveLine.setIsMergedStockMoveLine(true);
    return generatedStockMoveLine;
  }

  /**
   * This implementation copy fields from sale order line and purchase order line if the link
   * exists.
   *
   * @param stockMove
   * @param stockMoveLine
   * @param company
   */
  @Override
  public void setProductInfo(StockMove stockMove, StockMoveLine stockMoveLine, Company company)
      throws AxelorException {

    if (!appBaseService.isApp("supplychain")) {
      super.setProductInfo(stockMove, stockMoveLine, company);
      return;
    }

    Preconditions.checkNotNull(stockMoveLine);
    Preconditions.checkNotNull(company);

    Product product = stockMoveLine.getProduct();

    if (product == null) {
      return;
    }
    super.setProductInfo(stockMove, stockMoveLine, company);

    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();

    if (saleOrderLine != null) {
      setProductInfoFromSaleOrder(stockMoveLine, saleOrderLine);
    }
    if (purchaseOrderLine != null) {
      setProductInfoFromPurchaseOrder(stockMoveLine, purchaseOrderLine);
    }
  }

  protected void setProductInfoFromSaleOrder(
      StockMoveLine stockMoveLine, SaleOrderLine saleOrderLine) {

    stockMoveLine.setUnit(saleOrderLine.getUnit());
    stockMoveLine.setProductName(saleOrderLine.getProductName());
    if (Strings.isNullOrEmpty(stockMoveLine.getDescription())) {
      stockMoveLine.setDescription(saleOrderLine.getDescription());
    }
    stockMoveLine.setQty(saleOrderLine.getQty());
  }

  protected void setProductInfoFromPurchaseOrder(
      StockMoveLine stockMoveLine, PurchaseOrderLine purchaseOrderLine) {

    stockMoveLine.setUnit(purchaseOrderLine.getUnit());
    stockMoveLine.setProductName(purchaseOrderLine.getProductName());
    if (Strings.isNullOrEmpty(stockMoveLine.getDescription())) {
      stockMoveLine.setDescription(purchaseOrderLine.getDescription());
    }
    stockMoveLine.setQty(purchaseOrderLine.getQty());
  }

  @Override
  public boolean isAvailableProduct(StockMove stockMove, StockMoveLine stockMoveLine) {
    if (stockMoveLine.getProduct() == null
        || (stockMoveLine.getProduct() != null && !stockMoveLine.getProduct().getStockManaged())) {
      return true;
    }
    updateAvailableQty(stockMoveLine, stockMove.getFromStockLocation());
    BigDecimal availableQty = stockMoveLine.getAvailableQty();
    if (stockMoveLine.getProduct().getTrackingNumberConfiguration() != null
        && stockMoveLine.getTrackingNumber() == null) {
      availableQty = stockMoveLine.getAvailableQtyForProduct();
    }
    BigDecimal realQty = stockMoveLine.getRealQty();
    return availableQty.compareTo(realQty) >= 0;
  }

  @Override
  public void setInvoiceStatus(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getQtyInvoiced().compareTo(BigDecimal.ZERO) == 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Not invoiced"));
      stockMoveLine.setAvailableStatusSelect(3);
    } else if (stockMoveLine.getQtyInvoiced().compareTo(stockMoveLine.getRealQty()) < 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Partially invoiced"));
      stockMoveLine.setAvailableStatusSelect(4);
    } else if (stockMoveLine.getQtyInvoiced().compareTo(stockMoveLine.getRealQty()) == 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Invoiced"));
      stockMoveLine.setAvailableStatusSelect(1);
    }
  }

  @Override
  public boolean isAllocatedStockMoveLine(StockMoveLine stockMoveLine) {
    return stockMoveLine.getReservedQty().compareTo(BigDecimal.ZERO) > 0
        || stockMoveLine.getRequestedReservedQty().compareTo(BigDecimal.ZERO) > 0;
  }

  @Override
  public BigDecimal getAmountNotInvoiced(
      StockMoveLine stockMoveLine, boolean isPurchase, boolean ati, boolean recoveredTax)
      throws AxelorException {
    return this.getAmountNotInvoiced(
        stockMoveLine,
        stockMoveLine.getPurchaseOrderLine(),
        stockMoveLine.getSaleOrderLine(),
        isPurchase,
        ati,
        recoveredTax);
  }

  @Override
  public BigDecimal getAmountNotInvoiced(
      StockMoveLine stockMoveLine,
      PurchaseOrderLine purchaseOrderLine,
      SaleOrderLine saleOrderLine,
      boolean isPurchase,
      boolean ati,
      boolean recoveredTax)
      throws AxelorException {
    BigDecimal amountInCurrency = null;
    BigDecimal totalQty = null;
    BigDecimal notInvoicedQty = null;

    if (isPurchase && purchaseOrderLine != null) {
      totalQty = purchaseOrderLine.getQty();

      notInvoicedQty =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              purchaseOrderLine.getUnit(),
              stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced()),
              stockMoveLine.getRealQty().scale(),
              purchaseOrderLine.getProduct());

      if (ati && !recoveredTax) {
        amountInCurrency = purchaseOrderLine.getInTaxTotal();
      } else {
        amountInCurrency = purchaseOrderLine.getExTaxTotal();
      }
    } else if (!isPurchase && saleOrderLine != null) {
      totalQty = saleOrderLine.getQty();

      notInvoicedQty =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              saleOrderLine.getUnit(),
              stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced()),
              stockMoveLine.getRealQty().scale(),
              saleOrderLine.getProduct());
      if (ati) {
        amountInCurrency = saleOrderLine.getInTaxTotal();
      } else {
        amountInCurrency = saleOrderLine.getExTaxTotal();
      }
    }

    if (totalQty == null || BigDecimal.ZERO.compareTo(totalQty) == 0) {
      return null;
    }

    BigDecimal qtyRate = notInvoicedQty.divide(totalQty, 10, RoundingMode.HALF_UP);
    return amountInCurrency.multiply(qtyRate).setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public Batch validateCutOffBatch(List<Long> recordIdList, Long batchId) {
    BatchAccountingCutOffSupplyChain batchAccountingCutOff =
        Beans.get(BatchAccountingCutOffSupplyChain.class);

    batchAccountingCutOff.recordIdList = recordIdList;
    batchAccountingCutOff.run(Beans.get(AccountingBatchRepository.class).find(batchId));

    return batchAccountingCutOff.getBatch();
  }

  protected String getProductTypeFilter(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    List<String> domainFilerList = new ArrayList<>();
    domainFilerList.add(getFilterForStorables(stockMoveLine, stockMove));
    domainFilerList.add(getFilterForServices(stockMove));
    domainFilerList.removeIf(String::isEmpty);

    if (ObjectUtils.isEmpty(domainFilerList)) {
      return " AND self.productTypeSelect IN ('')";
    }

    return " AND " + domainFilerList.stream().collect(Collectors.joining(" OR ", "(", ")"));
  }

  @Override
  protected String getFilterForStorables(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    String checkQtyFilterStr = super.getFilterForStorables(stockMoveLine, stockMove);

    SupplyChainConfig supplyChainConfig =
        supplychainConfigService.getSupplyChainConfig(stockMove.getCompany());
    String storableFilter = "";
    final boolean isOutMove = stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING;
    final boolean isInMove = stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING;

    if ((isOutMove && supplyChainConfig.getHasOutSmForStorableProduct())
        || (isInMove && supplyChainConfig.getHasInSmForStorableProduct())
        || stockMove.getTypeSelect() == StockMoveRepository.TYPE_INTERNAL) {
      storableFilter =
          " self.productTypeSelect = '" + ProductRepository.PRODUCT_TYPE_STORABLE + "'";
      return "(" + storableFilter + checkQtyFilterStr + ")";
    }
    return "";
  }

  protected String getFilterForServices(StockMove stockMove) throws AxelorException {
    SupplyChainConfig supplyChainConfig =
        supplychainConfigService.getSupplyChainConfig(stockMove.getCompany());
    final boolean isOutMove = stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING;
    final boolean isInMove = stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING;
    if ((isOutMove && supplyChainConfig.getHasOutSmForNonStorableProduct())
        || (isInMove && supplyChainConfig.getHasInSmForNonStorableProduct())
        || stockMove.getTypeSelect() == StockMoveRepository.TYPE_INTERNAL) {
      return " self.productTypeSelect = '" + ProductRepository.PRODUCT_TYPE_SERVICE + "'";
    }
    return "";
  }

  @Override
  public List<InvoiceLine> getInvoiceLines(StockMoveLine stockMoveLine) {
    Objects.requireNonNull(stockMoveLine);
    if (stockMoveLine.getId() != null) {
      return invoiceLineRepository
          .all()
          .filter("self.stockMoveLine = :stockMoveLine")
          .bind("stockMoveLine", stockMoveLine)
          .fetch();
    }
    return new ArrayList<>();
  }
}
