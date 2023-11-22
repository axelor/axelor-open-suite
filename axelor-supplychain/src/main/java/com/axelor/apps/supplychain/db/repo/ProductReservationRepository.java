package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ProductReservationRepository extends AbstractProductReservationRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      Long id = (Long) json.get("id");
      ProductReservation productReservation = find(id);
      BigDecimal availableQty = null;
      try {
        availableQty =
            Beans.get(ProductReservationService.class).getAvailableQty(productReservation);
      } catch (AxelorException e) {
        throw new RuntimeException(e);
      }
      json.put(
          "$availableQty", availableQty); // specifications says  not a field, but computed on load
    }
    return super.populate(json, context);
  }

  public BigDecimal getReservedQty(Product product, StockLocation stockLocation) {
    if (stockLocation == null) {
      return sumQtyOrZero(
          findByProductReservationTypeAndStatusAndProduct(
                  AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
                  AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS,
                  product)
              .fetchStream());
    }
    return sumQtyOrZero(
        findByProductReservationTypeAndStatusAndStockLocationAndProduct(
                AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
                AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS,
                stockLocation,
                product)
            .fetchStream());
  }

  protected BigDecimal sumQtyOrZero(Stream<ProductReservation> productReservationStream) {
    return productReservationStream
        .filter(
            productReservation ->
                productReservation.getQty() != null
                    && !Objects.equals(productReservation.getQty(), BigDecimal.ZERO))
        .map(ProductReservation::getQty)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  public static final Map<String, String> MAP_ORIGIN_FIELD_NAME_BY_CLASS_NAME =
      Map.of(
          "com.axelor.apps.production.db.SaleOrderLine",
          "originSaleOrderLine",
          "com.axelor.apps.production.db.ManufOrder",
          "originManufOrder");

  public Query<ProductReservation> findByOriginAndProductReservationType(
      Integer productReservationType, Model instanceOriginModel, Long productId) {
    Long id = instanceOriginModel.getId();
    String originFieldName =
        MAP_ORIGIN_FIELD_NAME_BY_CLASS_NAME.get(instanceOriginModel.getClass().getName());
    return Query.of(ProductReservation.class)
        .filter(
            "self.productReservationType = :productReservationType AND self.product.id = :productId AND self."
                + originFieldName
                + ".id = :originModelId")
        .bind("productReservationType", productReservationType)
        .bind("productId", productId)
        .bind("originModelId", id)
        .cacheable();
  }
}
