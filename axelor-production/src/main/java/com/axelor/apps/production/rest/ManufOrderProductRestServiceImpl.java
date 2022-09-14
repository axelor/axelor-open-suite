package com.axelor.apps.production.rest;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ConsumedProductResponse;
import com.axelor.apps.production.rest.dto.ProducedProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManufOrderProductRestServiceImpl implements ManufOrderProductRestService {

  protected ProductStockLocationService productStockLocationService;

  @Inject
  public ManufOrderProductRestServiceImpl(ProductStockLocationService productStockLocationService) {
    this.productStockLocationService = productStockLocationService;
  }

  public List<ProdProduct> getProdProductsOfProduct(
      List<ProdProduct> prodProducts, Product product) {
    return prodProducts.stream()
        .filter(prodProduct -> prodProduct.getProduct().getId().equals(product.getId()))
        .collect(Collectors.toList());
  }

  public boolean isProductNotChecked(List<Product> checkProducts, Product newProduct) {
    return checkProducts.stream().noneMatch(product -> product.getId().equals(newProduct.getId()));
  }

  public List<StockMoveLine> getStockMoveLinesOfProduct(
      List<StockMoveLine> stockMoveLines, Product product) {
    return stockMoveLines.stream()
        .filter(line -> line.getProduct().getId().equals(product.getId()))
        .collect(Collectors.toList());
  }

  public ConsumedProductResponse createConsumedProductResponse(
      ManufOrder manufOrder, StockMoveLine stockMoveLine, BigDecimal plannedQty)
      throws AxelorException {
    Product product = stockMoveLine.getProduct();

    Map<String, Object> mapIndicators =
        productStockLocationService.computeIndicators(
            product.getId(), manufOrder.getCompany().getId(), 0L);

    BigDecimal availableQty = (BigDecimal) mapIndicators.get("$availableQty");

    return new ConsumedProductResponse(
        product,
        BigDecimal.ZERO.max(plannedQty),
        stockMoveLine.getQty(),
        BigDecimal.ZERO.max(plannedQty.subtract(availableQty)),
        availableQty,
        stockMoveLine.getUnit(),
        stockMoveLine.getTrackingNumber());
  }

  public BigDecimal getGlobalConsumedPlannedQty(Product product, ManufOrder manufOrder) {
    List<ProdProduct> prodProducts =
        getProdProductsOfProduct(manufOrder.getToConsumeProdProductList(), product);

    BigDecimal plannedQty = BigDecimal.ZERO;

    if (prodProducts == null || prodProducts.isEmpty()) {
      return BigDecimal.ZERO;
    }

    for (ProdProduct prodProduct : prodProducts) {
      plannedQty = plannedQty.add(prodProduct.getQty());
    }

    return plannedQty;
  }

  @Override
  public List<ConsumedProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException {
    List<Product> checkProducts = new ArrayList<>();
    List<ConsumedProductResponse> result =
        new ArrayList<>(getPlannedConsumedProductList(manufOrder, checkProducts));
    result.addAll(getAdditionalConsumedProductList(manufOrder, checkProducts));
    return result;
  }

  public void getAllConsumedProductResponsesOfProduct(
      ManufOrder manufOrder,
      Product product,
      List<StockMoveLine> productLines,
      List<ConsumedProductResponse> productResponses)
      throws AxelorException {
    BigDecimal consumedQty = BigDecimal.ZERO;

    int lastIndex = productLines.size() - 1;
    StockMoveLine lastProductLine = productLines.get(lastIndex);
    List<StockMoveLine> productLinesSubList = productLines.subList(0, lastIndex);

    for (StockMoveLine currentLine : productLinesSubList) {
      productResponses.add(
          createConsumedProductResponse(
              manufOrder,
              currentLine,
              currentLine
                  .getQty()
                  .min(getGlobalConsumedPlannedQty(product, manufOrder).subtract(consumedQty))));
      consumedQty = consumedQty.add(currentLine.getQty());
    }

    productResponses.add(
        createConsumedProductResponse(
            manufOrder,
            lastProductLine,
            getGlobalConsumedPlannedQty(product, manufOrder).subtract(consumedQty)));
  }

  public List<ConsumedProductResponse> getPlannedConsumedProductList(
      ManufOrder manufOrder, List<Product> checkProducts) throws AxelorException {
    List<ConsumedProductResponse> result = new ArrayList<>();

    for (ProdProduct prodProduct : manufOrder.getToConsumeProdProductList()) {
      Product product = prodProduct.getProduct();
      if (isProductNotChecked(checkProducts, product)) {
        List<StockMoveLine> productLines =
            getStockMoveLinesOfProduct(manufOrder.getConsumedStockMoveLineList(), product);

        if (productLines.size() == 1) {
          result.add(
              createConsumedProductResponse(manufOrder, productLines.get(0), prodProduct.getQty()));
        } else {
          getAllConsumedProductResponsesOfProduct(manufOrder, product, productLines, result);
        }
        checkProducts.add(product);
      }
    }
    return result;
  }

  public List<ConsumedProductResponse> getAdditionalConsumedProductList(
      ManufOrder manufOrder, List<Product> checkProducts) throws AxelorException {
    List<ConsumedProductResponse> result = new ArrayList<>();

    for (StockMoveLine line : manufOrder.getConsumedStockMoveLineList()) {
      Product product = line.getProduct();
      if (isProductNotChecked(checkProducts, product)) {
        result.add(createConsumedProductResponse(manufOrder, line, BigDecimal.ZERO));
        checkProducts.add(product);
      }
    }

    return result;
  }

  public BigDecimal getGlobalProducedPlannedQty(Product product, ManufOrder manufOrder) {
    List<ProdProduct> prodProducts =
        getProdProductsOfProduct(manufOrder.getToProduceProdProductList(), product);

    BigDecimal plannedQty = BigDecimal.ZERO;

    if (prodProducts == null || prodProducts.isEmpty()) {
      return BigDecimal.ZERO;
    }

    for (ProdProduct prodProduct : prodProducts) {
      plannedQty = plannedQty.add(prodProduct.getQty());
    }

    return plannedQty;
  }

  public ProducedProductResponse createProducedProductResponse(
      StockMoveLine stockMoveLine, BigDecimal plannedQty) {
    return new ProducedProductResponse(
        stockMoveLine.getProduct(),
        BigDecimal.ZERO.max(plannedQty),
        stockMoveLine.getQty(),
        stockMoveLine.getTrackingNumber(),
        stockMoveLine.getUnit());
  }

  @Override
  public List<ProducedProductResponse> getProducedProductList(ManufOrder manufOrder) {
    List<Product> checkProducts = new ArrayList<>();
    List<ProducedProductResponse> result =
        new ArrayList<>(getPlannedProducedProductList(manufOrder, checkProducts));
    result.addAll(getAdditionalProducedProductList(manufOrder, checkProducts));
    return result;
  }

  public void getAllProducedProductResponsesOfProduct(
      ManufOrder manufOrder,
      Product product,
      List<StockMoveLine> productLines,
      List<ProducedProductResponse> productResponses) {
    BigDecimal producedQty = BigDecimal.ZERO;
    int lastIndex = productLines.size() - 1;
    StockMoveLine lastProductLine = productLines.get(lastIndex);
    List<StockMoveLine> productLinesSubList = productLines.subList(0, lastIndex);

    for (StockMoveLine currentLine : productLinesSubList) {
      productResponses.add(
          createProducedProductResponse(
              currentLine,
              currentLine
                  .getQty()
                  .min(getGlobalProducedPlannedQty(product, manufOrder).subtract(producedQty))));
      producedQty = producedQty.add(currentLine.getQty());
    }

    productResponses.add(
        createProducedProductResponse(
            lastProductLine,
            getGlobalProducedPlannedQty(product, manufOrder).subtract(producedQty)));
  }

  public List<ProducedProductResponse> getPlannedProducedProductList(
      ManufOrder manufOrder, List<Product> checkProducts) {
    List<ProducedProductResponse> result = new ArrayList<>();

    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {
      Product product = prodProduct.getProduct();
      if (isProductNotChecked(checkProducts, product)) {
        List<StockMoveLine> productLines =
            getStockMoveLinesOfProduct(manufOrder.getProducedStockMoveLineList(), product);

        if (productLines.size() == 1) {
          result.add(createProducedProductResponse(productLines.get(0), prodProduct.getQty()));
        } else {
          getAllProducedProductResponsesOfProduct(manufOrder, product, productLines, result);
        }
        checkProducts.add(product);
      }
    }
    return result;
  }

  public List<ProducedProductResponse> getAdditionalProducedProductList(
      ManufOrder manufOrder, List<Product> checkProducts) {
    List<ProducedProductResponse> result = new ArrayList<>();

    for (StockMoveLine line : manufOrder.getProducedStockMoveLineList()) {
      Product product = line.getProduct();
      if (isProductNotChecked(checkProducts, product)) {
        result.add(createProducedProductResponse(line, BigDecimal.ZERO));
        checkProducts.add(product);
      }
    }

    return result;
  }
}
