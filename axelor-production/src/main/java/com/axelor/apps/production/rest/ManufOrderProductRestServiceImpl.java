package com.axelor.apps.production.rest;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ManufOrderProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  public BigDecimal getGlobalPlannedQty(Product product, List<ProdProduct> globalProdProductList) {
    List<ProdProduct> prodProducts = getProdProductsOfProduct(globalProdProductList, product);

    if (prodProducts == null || prodProducts.isEmpty()) {
      return BigDecimal.ZERO;
    }

    BigDecimal plannedQty = BigDecimal.ZERO;
    for (ProdProduct prodProduct : prodProducts) {
      plannedQty = plannedQty.add(prodProduct.getQty());
    }

    return plannedQty;
  }

  public ManufOrderProductResponse createProductResponse(
      ManufOrder manufOrder, StockMoveLine stockMoveLine, BigDecimal plannedQty)
      throws AxelorException {
    Product product = stockMoveLine.getProduct();
    BigDecimal availableQty = null;
    BigDecimal missingQty = null;

    if (Objects.isNull(stockMoveLine.getProducedManufOrder())) {
      Map<String, Object> mapIndicators =
          productStockLocationService.computeIndicators(
              product.getId(), manufOrder.getCompany().getId(), 0L);

      availableQty = (BigDecimal) mapIndicators.get("$availableQty");
      missingQty = BigDecimal.ZERO.max(plannedQty.subtract(availableQty));
    }

    return new ManufOrderProductResponse(
        product,
        stockMoveLine,
        BigDecimal.ZERO.max(plannedQty),
        stockMoveLine.getQty(),
        missingQty,
        availableQty,
        stockMoveLine.getTrackingNumber(),
        stockMoveLine.getUnit());
  }

  @Override
  public List<ManufOrderProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException {
    List<Product> checkProducts = new ArrayList<>();
    List<ManufOrderProductResponse> result =
        new ArrayList<>(
            getPlannedProductList(
                manufOrder,
                checkProducts,
                manufOrder.getToConsumeProdProductList(),
                manufOrder.getConsumedStockMoveLineList()));
    result.addAll(
        getAdditionalProductList(
            manufOrder, checkProducts, manufOrder.getConsumedStockMoveLineList()));
    return result;
  }

  @Override
  public List<ManufOrderProductResponse> getProducedProductList(ManufOrder manufOrder)
      throws AxelorException {
    List<Product> checkProducts = new ArrayList<>();
    List<ManufOrderProductResponse> result =
        getPlannedProductList(
            manufOrder,
            checkProducts,
            manufOrder.getToProduceProdProductList(),
            manufOrder.getProducedStockMoveLineList());
    result.addAll(
        getAdditionalProductList(
            manufOrder, checkProducts, manufOrder.getProducedStockMoveLineList()));
    return result;
  }

  public List<ManufOrderProductResponse> getPlannedProductList(
      ManufOrder manufOrder,
      List<Product> checkProducts,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLines)
      throws AxelorException {
    List<ManufOrderProductResponse> result = new ArrayList<>();

    for (ProdProduct prodProduct : prodProductList) {
      Product product = prodProduct.getProduct();
      if (isProductNotChecked(checkProducts, product)) {
        List<StockMoveLine> productLines = getStockMoveLinesOfProduct(stockMoveLines, product);

        if (productLines.size() == 1) {
          result.add(createProductResponse(manufOrder, productLines.get(0), prodProduct.getQty()));
        } else {
          getAllProductResponsesOfProduct(
              manufOrder, product, prodProductList, productLines, result);
        }
        checkProducts.add(product);
      }
    }
    return result;
  }

  public void getAllProductResponsesOfProduct(
      ManufOrder manufOrder,
      Product product,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> productLines,
      List<ManufOrderProductResponse> productResponses)
      throws AxelorException {
    BigDecimal realQty = BigDecimal.ZERO;

    int lastIndex = productLines.size() - 1;
    StockMoveLine lastProductLine = productLines.get(lastIndex);
    List<StockMoveLine> productLinesSubList = productLines.subList(0, lastIndex);

    for (StockMoveLine currentLine : productLinesSubList) {
      productResponses.add(
          createProductResponse(
              manufOrder,
              currentLine,
              currentLine
                  .getQty()
                  .min(getGlobalPlannedQty(product, prodProductList).subtract(realQty))));
      realQty = realQty.add(currentLine.getQty());
    }

    productResponses.add(
        createProductResponse(
            manufOrder,
            lastProductLine,
            getGlobalPlannedQty(product, prodProductList).subtract(realQty)));
  }

  public List<ManufOrderProductResponse> getAdditionalProductList(
      ManufOrder manufOrder, List<Product> checkProducts, List<StockMoveLine> stockMoveLines)
      throws AxelorException {
    List<ManufOrderProductResponse> result = new ArrayList<>();

    for (StockMoveLine line : stockMoveLines) {
      Product product = line.getProduct();
      if (isProductNotChecked(checkProducts, product)) {
        result.add(createProductResponse(manufOrder, line, BigDecimal.ZERO));
        checkProducts.add(product);
      }
    }

    return result;
  }
}
